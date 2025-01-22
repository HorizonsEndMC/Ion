package net.horizonsend.ion.server.features.transport.manager.extractors

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.manager.ShipTransportManager
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorData
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.chunkKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe

class ShipExtractorManager(val manager: ShipTransportManager) : ExtractorManager() {
	var extractors = Long2ObjectOpenHashMap<ExtractorData>()

	override fun getExtractors(): Collection<ExtractorData> {
		return extractors.values
	}

	override fun isExtractorPresent(key: BlockKey): Boolean {
		return extractors.contains(key)
	}

	/** Returns true if an extractor was registered */
	override fun registerExtractor(x: Int, y: Int, z: Int, ensureExtractor: Boolean): Boolean {
		val blockData = getBlockDataSafe(manager.starship.world, x, y, z) ?: return false

		// Store extractors via local coordinates
		val key = toBlockKey(manager.getLocalCoordinate(Vec3i(x, y, z)))

		val data = getExtractorData(blockData, key)
		if (data == null) return false

		extractors[key] = data

		return true
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
			if (registerExtractor( x, y, z, true)) {
				NewTransport.removeExtractor(manager.starship.world, x, y, z)
			}
		}
	}

	fun releaseExtractors() {
		// We can disregard the extractor data
		// Its more so a convenience thing
		val byChunk = extractors.keys.groupBy { key ->
			chunkKey((getX(key) + manager.starship.centerOfMass.x).shr(4), (getZ(key) + manager.starship.centerOfMass.z).shr(4))
		}

		for ((chunkKey, entries) in byChunk) {
			val ionChunk = IonChunk[manager.starship.world, chunkKey] ?: continue

			for (entry in entries) {
				val worldCoord = toBlockKey(manager.getGlobalCoordinate(toVec3i(entry)))
				ionChunk.transportNetwork.extractorManager.registerExtractor(worldCoord, ensureExtractor = true)
			}
		}
	}
}
