package net.horizonsend.ion.server.features.transport.manager.extractors

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.manager.ShipTransportManager
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorData
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.*
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe

class ShipExtractorManager(val manager: ShipTransportManager) : ExtractorManager() {
	val extractors = Long2ObjectOpenHashMap<ExtractorData>()

	override fun getExtractors(): Collection<ExtractorData> {
		return extractors.values
	}

	override fun isExtractorPresent(key: BlockKey): Boolean {
		return extractors.contains(key)
	}

	override fun getExtractorData(key: BlockKey): ExtractorData? {
		return extractors[key]
	}

	/** Returns true if an extractor was registered */
	override fun registerExtractor(x: Int, y: Int, z: Int): ExtractorData? {
		val blockData = getBlockDataSafe(manager.starship.world, x, y, z) ?: return null

		// Store extractors via local coordinates
		val key = toBlockKey(manager.getLocalCoordinate(Vec3i(x, y, z)))

		val data = getExtractorData(blockData, key, manager.getWorld()) ?: return null

		extractors[key] = data

		return data
	}

	override fun removeExtractor(x: Int, y: Int, z: Int): ExtractorData? {
		return extractors.remove(toBlockKey(x, y, z))
	}

	override fun removeExtractor(key: BlockKey): ExtractorData? {
		return extractors.remove(key)
	}

	fun loadExtractors() {
		manager.starship.iterateBlocks { x, y, z ->
			// If an extractor is added at the starship, remove the one in the world
			if (registerExtractor( x, y, z) != null) {
				NewTransport.removeExtractor(manager.starship.world, x, y, z)
			}
		}
	}

	fun releaseExtractors() {
		// We can disregard the extractor data
		// Its more so a convenience thing
		val byChunk = extractors.keys.groupBy { key ->
			val (worldX, _, worldZ) = manager.getGlobalCoordinate(toVec3i(key))
			chunkKey(worldX.shr(4), worldZ.shr(4))
		}

		for ((chunkKey, entries) in byChunk) {
			val ionChunk = IonChunk[manager.starship.world, chunkKey] ?: continue

			for (entry in entries) {
				if (!verifyExtractor(manager.starship.world, entry)) continue

				val worldCoord = toBlockKey(manager.getGlobalCoordinate(toVec3i(entry)))

				ionChunk.transportNetwork.extractorManager.registerExtractor(worldCoord)
			}
		}
	}
}
