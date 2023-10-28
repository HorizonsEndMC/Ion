package net.horizonsend.ion.server.features.ores

import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.Region
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.registrations.OrePlacementConfig
import net.horizonsend.ion.server.miscellaneous.utils.Position
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.persistence.PersistentDataType
import kotlin.random.Random

/*
TODO: Ore logic should be separated from the Listener, and the Async code should avoid using the scheduler, as well
	as well as being its own class.
*/

@Suppress("Unused")
object CustomOrePlacement : SLEventListener() {
	@EventHandler(priority = EventPriority.MONITOR)
	fun onChunkLoad(event: ChunkLoadEvent) {
		placeOres(event.chunk)
	}

	fun placeOres(chunk: Chunk) {
		val placementConfiguration = try {
			OrePlacementConfig.valueOf(chunk.world.name)
		} catch (_: IllegalArgumentException) {
			return
		}

		val chunkOreVersion = chunk.persistentDataContainer.get(NamespacedKeys.ORE_CHECK, PersistentDataType.INTEGER)

		if (chunkOreVersion == placementConfiguration.currentOreVersion) return

		Bukkit.getScheduler().runTaskAsynchronously(
			IonServer,
			Runnable {
				val chunkSnapshot = chunk.getChunkSnapshot(true, false, false)
				val random = Random(chunk.chunkKey)

				// These are kept separate as ores need to be written to a file,
				// reversing ores does not need to be written to a file.
				val placedBlocks = mutableMapOf<Position<Int>, BlockData>() // Everything
				val placedOres = mutableMapOf<Position<Int>, Ore>() // Everything that needs to be written to a file.

				val file =
					IonServer.dataFolder.resolve("ores/${chunkSnapshot.worldName}/${chunkSnapshot.x}_${chunkSnapshot.z}.ores.csv")

				if (file.exists()) {
					file.readText().split("\n").forEach { oreLine ->
						if (oreLine.isEmpty()) return@forEach

						val oreData = oreLine.split(",")

						if (oreData.size != 5) {
							throw IllegalArgumentException("${file.absolutePath} ore data line $oreLine is not valid.")
						}

						val x = oreData[0].toInt()
						val y = oreData[1].toInt()
						val z = oreData[2].toInt()
						val original = Material.valueOf(oreData[3])
						val placedOre = Ore.valueOf(oreData[4])

						if (chunkSnapshot.getBlockData(x, y, z) == placedOre.blockData) {
							placedBlocks[Position(x, y, z)] = original.createBlockData()
						}
					}
				}

				for (x in 0..15) for (z in 0..15) {
					val minBlockY = chunk.world.minHeight
					val maxBlockY = chunkSnapshot.getHighestBlockYAt(x, z)

					for (y in minBlockY..maxBlockY) {
						val blockData = chunkSnapshot.getBlockData(x, y, z)

						if (!placementConfiguration.groundMaterial.contains(blockData.material)) continue

						if (y < maxBlockY) if (chunkSnapshot.getBlockType(x, y + 1, z).isAir) continue
						if (y > minBlockY) if (chunkSnapshot.getBlockType(x, y - 1, z).isAir) continue

						placementConfiguration.options.forEach { (ore, chance) ->
							if (random.nextFloat() < .002f * chance) placedOres[Position(x, y, z)] = ore
						}
					}
				}

				placedBlocks.putAll(placedOres.mapValues { it.value.blockData })

				Bukkit.getScheduler().runTask(
					IonServer,
					Runnable {
						placedBlocks.forEach { (position, blockData) ->
							chunk.getBlock(position.x, position.y, position.z).setBlockData(blockData, false)
						}

						IonServer.slF4JLogger.info("Updated ores in ${chunk.x} ${chunk.z} @ ${chunk.world.name} to version ${placementConfiguration.currentOreVersion} from $chunkOreVersion, ${placedOres.size} ores placed.")

						chunk.persistentDataContainer.set(
							NamespacedKeys.ORE_CHECK,
							PersistentDataType.INTEGER,
							placementConfiguration.currentOreVersion
						)
					}
				)

				// TODO: I am disappointed with myself for writing this dumb file format.
				IonServer.dataFolder.resolve("ores/${chunkSnapshot.worldName}")
					.apply { mkdirs() }
					.resolve("${chunkSnapshot.x}_${chunkSnapshot.z}.ores.csv")
					.writeText(
						placedOres.map {
							"${it.key.x},${it.key.y},${it.key.z},${
								chunkSnapshot.getBlockType(
									it.key.x,
									it.key.y,
									it.key.z
								)
							},${it.value}"
						}.joinToString("\n", "", "")
					)
			}
		)
	}

	fun placeOresFromStored(audience: Audience?, chunk: Chunk, region: Region? = null) = Tasks.async {
		val chunkSnapshot = chunk.getChunkSnapshot(true, false, false)
		val file = IonServer.dataFolder.resolve("ores/${chunkSnapshot.worldName}/${chunkSnapshot.x}_${chunkSnapshot.z}.ores.csv")
		val chunkStartX = chunk.x.shl(4)
		val chunkStartZ = chunk.z.shl(4)

		val toPlace = mutableMapOf<Vec3i, Ore>()

		file.readText().split("\n").forEach { oreLine ->
			if (oreLine.isEmpty()) return@forEach

			val oreData = oreLine.split(",")

			if (oreData.size != 5) {
				throw IllegalArgumentException("${file.absolutePath} ore data line $oreLine is not valid.")
			}

			val x = oreData[0].toInt()
			val y = oreData[1].toInt()
			val z = oreData[2].toInt()

			if (region?.contains(BlockVector3.at(x + chunkStartX, y, z + chunkStartZ)) == false) return@forEach

			val placedOre = Ore.valueOf(oreData[4])

			toPlace[Vec3i(x + chunkStartX, y, z + chunkStartZ)] = placedOre
		}

		Tasks.sync {
			for ((location, ore) in toPlace) {
				val (x, y, z) = location

				chunk.world.setBlockData(x, y, z, ore.blockData)
			}

			audience?.information("Placed ${toPlace.size} ore blocks for chunk ${chunk.x} ${chunk.z}.")
		}
	}
}
