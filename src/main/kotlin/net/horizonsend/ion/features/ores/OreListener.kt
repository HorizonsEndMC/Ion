package net.horizonsend.ion.features.ores

import kotlin.math.pow
import kotlin.random.Random
import net.horizonsend.ion.Ion
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.data.BlockData
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.persistence.PersistentDataType

internal class OreListener(private val plugin: Ion) : Listener {
	private val currentOreVersion = 6

	private val oreCheckNamespace = NamespacedKey(plugin, "oreCheck")

	private class BlockLocation(var x: Int, var y: Int, var z: Int)

	@EventHandler
	fun onChunkLoad(event: ChunkLoadEvent) {
		val chunkOreVersion = event.chunk.persistentDataContainer.get(oreCheckNamespace, PersistentDataType.INTEGER)

		if (chunkOreVersion == currentOreVersion) return

		Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
			val chunkSnapshot = event.chunk.getChunkSnapshot(true, false, false)
			val placementConfiguration =
				OrePlacementConfig.values().find { it.name == chunkSnapshot.worldName } ?: return@Runnable
			val random = Random(event.chunk.chunkKey)

			// These are kept separate as ores need to be written to a file,
			// reversing ores does not need to be written to a file.
			val placedBlocks = mutableMapOf<BlockLocation, BlockData>() // Everything
			val placedOres = mutableMapOf<BlockLocation, Ore>() // Everything that needs to be written to a file.

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
					placedBlocks[BlockLocation(x, y, z)] = original.createBlockData()
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
						if (random.nextFloat() < .1f.pow(1.5f / chance)) placedOres[BlockLocation(x, y, z)] = ore
					}
				}
			}

			placedBlocks.putAll(placedOres.mapValues { it.value.blockData })

			Bukkit.getScheduler().runTask(plugin, Runnable {
				placedBlocks.forEach { (position, blockData) ->
					event.chunk.getBlock(position.x, position.y, position.z).setBlockData(blockData, false)
				}

				println("Updated ores in ${event.chunk.x} ${event.chunk.z} @ ${event.world.name} to the latest version, it was previously $chunkOreVersion, ${placedOres.size} ores placed.")

				event.chunk.persistentDataContainer.set(oreCheckNamespace, PersistentDataType.INTEGER, currentOreVersion)
			})

			plugin.dataFolder.resolve("ores/${chunkSnapshot.worldName}")
				.apply { mkdirs() }
				.resolve("${chunkSnapshot.x}_${chunkSnapshot.z}.ores.csv")
				.writeText(placedOres.map {
					"${it.key.x},${it.key.y},${it.key.z},${chunkSnapshot.getBlockType(it.key.x, it.key.y, it.key.z)},${it.value}"
				}.joinToString("\n", "", ""))
		})
	}
}