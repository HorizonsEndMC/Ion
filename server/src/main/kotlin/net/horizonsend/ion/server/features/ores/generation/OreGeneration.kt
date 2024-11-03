package net.horizonsend.ion.server.features.ores.generation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.ores.OldOreData
import net.horizonsend.ion.server.features.ores.generation.PlanetOreSettings.Companion.STAR_BALANCE
import net.horizonsend.ion.server.features.ores.storage.OreData
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.ORE_DATA
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import org.bukkit.Chunk
import org.bukkit.ChunkSnapshot
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.persistence.PersistentDataType
import kotlin.random.Random

object OreGeneration : IonServerComponent() {
	// Scope for ore generation / migration tasks
	private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

	@EventHandler(priority = EventPriority.MONITOR)
	fun onChunkLoad(event: ChunkLoadEvent) {
		scope.launch {
			// Only handle planets with defined generation settings
			val oreSettings = PlanetOreSettings[event.world] ?: return@launch

			var oreData = runCatching { event.chunk.persistentDataContainer.get(ORE_DATA, OreData) }.getOrNull()
			val chunkSnapshot = event.chunk.getChunkSnapshot(true, false, false)

			@Suppress("DEPRECATION")
			val oldChunkVersion = event.chunk.persistentDataContainer.getOrDefault(NamespacedKeys.ORE_CHECK, PersistentDataType.INTEGER, 0)

			// Ore data likely in old format if not present and world has generation settings
			if (oldChunkVersion != 0 && oreData == null) {
				oreData = migrateFormats(event.chunk, chunkSnapshot, oldChunkVersion)
			}

			// Data should be migrated by now
			val chunkOreVersion = oreData?.dataVersion ?: 0
			val currentWorldVersion = oreSettings.dataVersion

			// Up to date
			if (chunkOreVersion == currentWorldVersion) return@launch

			upgrade(event.chunk, chunkOreVersion, chunkSnapshot, oreSettings, oreData)
		}
	}

	/**
	 * Migrate from the old CSV format to the PDC
	 *
	 * If there is no data, add an empty PDC to mark no ores generated
	 **/
	private fun migrateFormats(chunk: Chunk, chunkSnapshot: ChunkSnapshot, version: Int): OreData {
		val file = IonServer.dataFolder.resolve("ores/${chunkSnapshot.worldName}/${chunkSnapshot.x}_${chunkSnapshot.z}.ores.csv")

		val oreData = OreData(version)

		if (file.exists()) {
			file.readText().split("\n").forEach { oreLine ->
				if (oreLine.isEmpty()) return@forEach

				val rawOreData = oreLine.split(",")

				if (rawOreData.size != 5) {
					throw IllegalArgumentException("${file.absolutePath} ore data line $oreLine is not valid.")
				}

				val x = rawOreData[0].toInt()
				val y = rawOreData[1].toInt()
				val z = rawOreData[2].toInt()
				val original = Material.valueOf(rawOreData[3])
				val placedOre = OldOreData.valueOf(rawOreData[4])

				oreData.addPosition(x, y, z, placedOre.new, original)
			}

			file.delete()
		}

		chunk.persistentDataContainer.set(ORE_DATA, OreData, oreData)
		log.info("Updated ores in ${chunk.x} ${chunk.z} @ ${chunk.world.name} to new storage method.")

		return oreData
	}

	private fun upgrade(chunk: Chunk, currentVersion: Int, snapshot: ChunkSnapshot, config: PlanetOreSettings, data: OreData?) {
		val blockUpdates: MutableMap<Long, BlockData> = mutableMapOf()

		data?.let { runCatching {
			clearOres(data, snapshot, blockUpdates)
		}.onFailure {
			log.warn("Error clearing old ores for chunk ${chunk.x}, ${chunk.z} @ ${chunk.world.name}")
			it.printStackTrace()
		} }

		val oreData = placeOres(chunk, snapshot, config, blockUpdates)

		Tasks.sync {
			executeChanges(chunk, oreData, blockUpdates)
			log.info("Updated ores in ${chunk.x} ${chunk.z} @ ${chunk.world.name} to version ${config.dataVersion} from $currentVersion. ${oreData.positions.size} ores placed.")
		}
	}

	/**
	 * Clear old ores
	 **/
	private fun clearOres(data: OreData, snapshot: ChunkSnapshot, blockUpdates: MutableMap<Long, BlockData>) {
		for (index in 0..data.positions.lastIndex) {
			val position = data.positions[index]

			val oreIndex = data.oreIndexes[index].toInt()
			val ore = data.orePalette[oreIndex]

			val replacementIndex = data.replacedIndexes[index].toInt()
			val replaced = data.replacedPalette[replacementIndex]

			val x = getX(position)
			val y = getY(position)
			val z = getZ(position)

			if (snapshot.getBlockData(x, y, z) == ore.blockData) {
				blockUpdates[position] = replaced.createBlockData()
			}
		}
	}

	/**
	 * Generate placements for new ores
	 **/
	private fun placeOres(
		chunk: Chunk,
		chunkSnapshot: ChunkSnapshot,
		config: PlanetOreSettings,
		blockUpdates: MutableMap<Long, BlockData>
	): OreData {
		val random = Random(chunk.chunkKey)

		val minBlockY = chunk.world.minHeight

		val oreData = OreData(config.dataVersion)

		for (x in 0..15) for (z in 0..15) {
			val maxBlockY = chunkSnapshot.getHighestBlockYAt(x, z)

			for (y in minBlockY..maxBlockY) {
				val blockData = chunkSnapshot.getBlockData(x, y, z)

				if (!config.groundMaterials.contains(blockData.material)) continue
				if (y < maxBlockY) if (chunkSnapshot.getBlockType(x, y + 1, z).isAir) continue
				if (y > minBlockY) if (chunkSnapshot.getBlockType(x, y - 1, z).isAir) continue

				val placedOre = config.ores.firstOrNull { oreSetting ->
					random.nextFloat() < STAR_BALANCE * oreSetting.stars
				}?.ore ?: continue

				val key = oreData.addPosition(x, y, z, placedOre, blockData.material)
				blockUpdates[key] = placedOre.getReplacementType(blockData)
			}
		}

		return oreData
	}

	private fun executeChanges(chunk: Chunk, newData: OreData, blockUpdates: MutableMap<Long, BlockData>) {
		chunk.persistentDataContainer.set(ORE_DATA, OreData, newData)
		val minY = chunk.world.minHeight
		val maxY = chunk.world.maxHeight

		blockUpdates.forEach { (position, data) ->
			val x = getX(position)
			val y = getY(position)
			val z = getZ(position)

			if (y > maxY || y < minY) {
				log.warn("Attempted to place ore outside of build limit! Did the world height change?")
				return@forEach
			}

			chunk.getBlock(x, y, z).setBlockData(data, false)
		}
	}
}
