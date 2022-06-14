package net.horizonsend.ion.server.listeners

import kotlin.random.Random
import net.horizonsend.ion.common.utilities.Position
import net.horizonsend.ion.server.Ion
import net.horizonsend.ion.server.ores.Ore
import net.horizonsend.ion.server.ores.OrePlacementConfig
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.data.BlockData
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.persistence.PersistentDataType

/*
 TODO: Ore logic should be separated from the Listener, and the Async code should avoid using the scheduler, as well
       as well as being its own class.
*/

class ChunkLoadListener(private val plugin: Ion) : Listener {
	private val oreCheckNamespace = NamespacedKey(plugin, "oreCheck")

	@EventHandler(priority = EventPriority.MONITOR)
	@Suppress("Unused")
	fun onChunkLoad(event: ChunkLoadEvent) {
		val placementConfiguration = try { OrePlacementConfig.valueOf(event.world.name) } catch (_: IllegalArgumentException) { return }
		val chunkOreVersion = event.chunk.persistentDataContainer.get(oreCheckNamespace, PersistentDataType.INTEGER)

		if (chunkOreVersion == placementConfiguration.currentOreVersion) return

		Bukkit.getScheduler().runTaskAsynchronously(
			plugin,
			Runnable {
				val chunkSnapshot = event.chunk.getChunkSnapshot(true, false, false)
				val random = Random(event.chunk.chunkKey)

				// These are kept separate as ores need to be written to a file,
				// reversing ores does not need to be written to a file.
				val placedBlocks = mutableMapOf<Position<Int>, BlockData>() // Everything
				val placedOres = mutableMapOf<Position<Int>, Ore>() // Everything that needs to be written to a file.

				val file =
					plugin.dataFolder.resolve("ores/${chunkSnapshot.worldName}/${chunkSnapshot.x}_${chunkSnapshot.z}.ores.csv")

				if (file.exists()) file.readText().split("\n").forEach { oreLine ->
					if (oreLine.isEmpty()) return@forEach

					val oreData = oreLine.split(",")

					if (oreData.size != 5)
						throw IllegalArgumentException("${file.absolutePath} ore data line $oreLine is not valid.")

					val x = oreData[0].toInt()
					val y = oreData[1].toInt()
					val z = oreData[2].toInt()
					val original = Material.valueOf(oreData[3])
					val placedOre = Ore.valueOf(oreData[4])

					if (chunkSnapshot.getBlockData(x, y, z) == placedOre.blockData)
						placedBlocks[Position(x, y, z)] = original.createBlockData()
				}

				for (x in 0..15) for (z in 0..15) {
					val minBlockY = event.chunk.world.minHeight
					val maxBlockY = chunkSnapshot.getHighestBlockYAt(x, z)

					for (y in minBlockY..maxBlockY) {
						val blockData = chunkSnapshot.getBlockData(x, y, z)

						if (!placementConfiguration.groundMaterial.contains(blockData.material)) continue

						if (x < 15) if (chunkSnapshot.getBlockType(x + 1, y, z).isAir) continue
						if (z < 15) if (chunkSnapshot.getBlockType(x, y, z + 1).isAir) continue

						if (x > 0) if (chunkSnapshot.getBlockType(x - 1, y, z).isAir) continue
						if (z > 0) if (chunkSnapshot.getBlockType(x, y, z - 1).isAir) continue

						if (y < maxBlockY) if (chunkSnapshot.getBlockType(x, y + 1, z).isAir) continue
						if (y > minBlockY) if (chunkSnapshot.getBlockType(x, y - 1, z).isAir) continue

						placementConfiguration.options.forEach { (ore, chance) ->
							if (random.nextFloat() < .002f * chance) placedOres[Position(x, y, z)] = ore
						}
					}
				}

				placedBlocks.putAll(placedOres.mapValues { it.value.blockData })

				Bukkit.getScheduler().runTask(
					plugin,
					Runnable {
						placedBlocks.forEach { (position, blockData) ->
							event.chunk.getBlock(position.x, position.y, position.z).setBlockData(blockData, false)
						}

						println("Updated ores in ${event.chunk.x} ${event.chunk.z} @ ${event.world.name} to version ${placementConfiguration.currentOreVersion} from $chunkOreVersion, ${placedOres.size} ores placed.")

						event.chunk.persistentDataContainer.set(oreCheckNamespace, PersistentDataType.INTEGER, placementConfiguration.currentOreVersion)
					}
				)

				// TODO: I am disappointed with myself for writing this dumb file format.
				plugin.dataFolder.resolve("ores/${chunkSnapshot.worldName}")
					.apply { mkdirs() }
					.resolve("${chunkSnapshot.x}_${chunkSnapshot.z}.ores.csv")
					.writeText(
						placedOres.map {
							"${it.key.x},${it.key.y},${it.key.z},${chunkSnapshot.getBlockType(it.key.x, it.key.y, it.key.z)},${it.value}"
						}.joinToString("\n", "", "")
					)
			}
		)
	}
}