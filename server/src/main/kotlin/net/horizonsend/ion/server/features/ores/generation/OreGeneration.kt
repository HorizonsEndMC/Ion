package net.horizonsend.ion.server.features.ores.generation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.ores.OldOreData
import net.horizonsend.ion.server.features.ores.storage.Ore
import net.horizonsend.ion.server.features.ores.storage.OreData
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.ORE_DATA
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.minecraft.core.BlockPos
import org.bukkit.Chunk
import org.bukkit.ChunkSnapshot
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.persistence.PersistentDataType

object OreGeneration : IonServerComponent() {
	// Scope for ore generation / migration tasks
	private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

	@EventHandler(priority = EventPriority.MONITOR)
	fun onChunkLoad(event: ChunkLoadEvent) = scope.launch {
		// Only handle planets with defined generation settings
		val oreSettings = PlanetOreSettings[event.world] ?: return@launch

		var oreData = event.chunk.persistentDataContainer.get(ORE_DATA, OreData)
		val chunkSnapshot = event.chunk.getChunkSnapshot(true, false, false)

		// Ore data likely in old format if not present and world has generation settings
		if (oreData == null) {
			oreData = migrateFormats(event.chunk, chunkSnapshot)
		}

		// Data should be migrated by now
		val chunkOreVersion = oreData.dataVersion
		val currentWorldVersion = oreSettings.dataVersion

		// Up to date
		if (chunkOreVersion == currentWorldVersion) return@launch

		upgrade(event.chunk, chunkSnapshot, oreSettings, oreData)
	}

	/**
	 * Migrate from the old CSV format to the PDC
	 *
	 * If there is no data, add an empty PDC to mark no ores generated
	 **/
	private fun migrateFormats(chunk: Chunk, chunkSnapshot: ChunkSnapshot): OreData {
		val chunkOreVersion = chunk.persistentDataContainer.getOrDefault(NamespacedKeys.ORE_CHECK, PersistentDataType.INTEGER, 0)

		val file = IonServer.dataFolder.resolve("ores/${chunkSnapshot.worldName}/${chunkSnapshot.x}_${chunkSnapshot.z}.ores.csv")

		val locations = mutableListOf<Long>()
		val oreIndexes = mutableListOf<Byte>()
		val oreTypes = mutableListOf<Ore>()
		val replacementIndexes = mutableListOf<Byte>()
		val replacementTypes = mutableListOf<Material>()

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

				// The packed position
				locations.add(BlockPos.asLong(x, y, z))

				// Get the index of the ore in the ore index
				var oreIndex = oreTypes.indexOf(placedOre.new)
				if (oreIndex == -1) {
					oreTypes.add(placedOre.new)
					oreIndex = oreTypes.indexOf(placedOre.new)
				}

				require(oreIndex != -1)
				oreIndexes.add(oreIndex.toByte())

				// Get the index of the ore in the ore index
				var replacementIndex = replacementTypes.indexOf(original)
				if (replacementIndex == -1) {
					replacementTypes.add(original)
					replacementIndex = replacementTypes.indexOf(original)
				}

				require(replacementIndex != -1)
				replacementIndexes.add(replacementIndex.toByte())
			}
		}

		val data = OreData(
			dataVersion = chunkOreVersion,
			positions = locations.toLongArray(),
			oreIndexes = oreIndexes.toByteArray(),
			ores = oreTypes.toTypedArray(),
			replacedIndexes = replacementIndexes.toByteArray(),
			replaced = replacementTypes.toTypedArray(),
		)

		chunk.persistentDataContainer.set(ORE_DATA, OreData, data)

		return data
	}

	private fun upgrade(chunk: Chunk, snapshot: ChunkSnapshot, config: PlanetOreSettings, data: OreData) {
		val blockUpdates: MutableMap<Long, BlockData> = mutableMapOf()

		clearOres(data, blockUpdates)

		val oreData = placeOres(chunk, snapshot, config, blockUpdates)

		Tasks.sync {
			executeChanges(chunk, oreData, blockUpdates)
			log.info("Updated ores in ${chunk.x} ${chunk.z} @ ${chunk.world.name} to version ${config.dataVersion}. ${oreData.ores.size} ores placed.")
		}
	}

	/**
	 * Clear old ores
	 **/
	private fun clearOres(data: OreData, blockUpdates: MutableMap<Long, BlockData>) {
		for (index in 0..data.positions.lastIndex) {
			val position = data.positions[index]
			val replaced = data.replaced[data.replacedIndexes[index].toInt()]

			blockUpdates[position] = replaced.createBlockData()
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
		val blobPlacements = generateBlobs(chunk, config.ores)
		val chunkOriginX = chunk.x.shl(4)
		val chunkOriginZ = chunk.z.shl(4)

		val locations = mutableListOf<Long>()
		val oreIndexes = mutableListOf<Byte>()
		val oreTypes = mutableListOf<Ore>()
		val replacementIndexes = mutableListOf<Byte>()
		val replacementTypes = mutableListOf<Material>()

		for (x in 0..15) for (z in 0..15) {
			val minBlockY = chunk.world.minHeight
			val maxBlockY = chunkSnapshot.getHighestBlockYAt(x, z)

			for (y in minBlockY..maxBlockY) {
				val blockData = chunkSnapshot.getBlockData(x, y, z)

				if (!config.groundMaterials.contains(blockData.material)) continue

				if (y < maxBlockY) if (chunkSnapshot.getBlockType(x, y + 1, z).isAir) continue
				if (y > minBlockY) if (chunkSnapshot.getBlockType(x, y - 1, z).isAir) continue

				val realX = chunkOriginX + x
				val realZ = chunkOriginZ + z

				val blob = blobPlacements.firstOrNull { it.contains(chunkOriginX + x, y, chunkOriginZ + z) } ?: continue

				val placedOre = blob.ore
				blockUpdates[BlockPos.asLong(realX, y, realZ)] = placedOre.getReplacementType(blockData)

				// The packed position
				locations.add(BlockPos.asLong(x, y, z))

				// Get the index of the ore in the ore index
				var oreIndex = oreTypes.indexOf(placedOre)
				if (oreIndex == -1) {
					oreTypes.add(placedOre)
					oreIndex = oreTypes.indexOf(placedOre)
				}

				require(oreIndex != -1)
				oreIndexes.add(oreIndex.toByte())

				val original = blockData.material

				// Get the index of the ore in the ore index
				var replacementIndex = replacementTypes.indexOf(original)
				if (replacementIndex == -1) {
					replacementTypes.add(original)
					replacementIndex = replacementTypes.indexOf(original)
				}

				require(replacementIndex != -1)
				replacementIndexes.add(replacementIndex.toByte())
			}
		}

		return OreData(
			dataVersion = config.dataVersion,
			positions = locations.toLongArray(),
			oreIndexes = oreIndexes.toByteArray(),
			ores = oreTypes.toTypedArray(),
			replacedIndexes = replacementIndexes.toByteArray(),
			replaced = replacementTypes.toTypedArray(),
		)
	}

	private fun executeChanges(chunk: Chunk, newData: OreData, blockUpdates: MutableMap<Long, BlockData>) {
		chunk.persistentDataContainer.set(ORE_DATA, OreData, newData)

		blockUpdates.forEach { (postion, data) ->
			val x = BlockPos.getX(postion)
			val y = BlockPos.getY(postion)
			val z = BlockPos.getZ(postion)

 			chunk.world.setBlockData(x, y, z, data)
		}
	}
}
