package net.horizonsend.ion.server.features.transport.manager.extractors

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.chunkKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.getBlockTypeSafe

class ShipExtractorManager(val starship: Starship) : ExtractorManager() {
	var extractors = Long2ObjectOpenHashMap<ExtractorData>()

	override fun getExtractors(): Collection<ExtractorData> {
		return extractors.values
	}

	override fun isExtractor(key: BlockKey): Boolean {
		return extractors.contains(key)
	}

	/** Returns true if an extractor was registered */
	override fun registerExtractor(x: Int, y: Int, z: Int, ensureExtractor: Boolean): Boolean {
		if (ensureExtractor && getBlockTypeSafe(starship.world, x, y, z) != EXTRACTOR_TYPE) return false
		val key = toBlockKey(x, y, z)
		extractors[key] = ExtractorData(key)
		return true
	}

	override fun removeExtractor(x: Int, y: Int, z: Int): ExtractorData? {
		return extractors.remove(toBlockKey(x, y, z))
	}

	override fun removeExtractor(key: BlockKey): ExtractorData? {
		return extractors.remove(key)
	}

	fun loadExtractors() {
		starship.iterateBlocks { x, y, z ->
			if (registerExtractor(x, y, z, true)) NewTransport.removeExtractor(starship.world, x, y, z)
		}
	}

	fun releaseExtractors() {
		// We can disregard the extractor data
		// Its more so a convenience thing
		val byChunk = extractors.keys.groupBy { key ->
			chunkKey(getX(key).shr(4), getZ(key).shr(4))
		}

		for ((chunkKey, entries) in byChunk) {
			val ionChunk = IonChunk[starship.world, chunkKey] ?: continue

			for (entry in entries) {
				ionChunk.transportNetwork.extractorManager.registerExtractor(entry, ensureExtractor = true)
			}
		}
	}

	fun displace(movement: StarshipMovement) {
		val new = Long2ObjectOpenHashMap<ExtractorData>()
		extractors.forEach { new[movement.displaceKey(it.key)] = it.value }

		extractors = new
	}
}
