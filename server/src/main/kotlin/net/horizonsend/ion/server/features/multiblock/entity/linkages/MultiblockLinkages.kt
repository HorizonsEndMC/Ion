package net.horizonsend.ion.server.features.multiblock.entity.linkages

import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import java.util.concurrent.ConcurrentHashMap

abstract class MultiblockLinkageManager {
	protected open val linkages = ConcurrentHashMap<BlockKey, LinkageHolder>()

	fun registerLinkage(location: BlockKey, linkage: MultiblockLinkage) {
		when (val present: LinkageHolder? = linkages[location]) {
			is SingleMultiblockLinkage -> linkages[location] = SharedMultiblockLinkage.of(present.linkage, linkage)
			is SharedMultiblockLinkage -> present.add(linkage)
			null -> linkages[location] = SingleMultiblockLinkage(linkage)
		}
	}

	fun deRegisterLinkage(location: BlockKey) {
		linkages.remove(location)
	}

	fun getLinkages(location: BlockKey): Set<MultiblockLinkage> {
		return linkages[location]?.getLinkages() ?: setOf()
	}

	fun transferTo(other: MultiblockLinkageManager) {
		for ((key, holder) in linkages) {
			val linkages = holder.getLinkages()
			for (linkage in linkages) other.registerLinkage(key, linkage)
		}
	}

	fun transferTo(keys: Iterable<BlockKey>, other: MultiblockLinkageManager) {
		val intersect = linkages.keys.intersect(keys.toSet())

		for (key in intersect) {
			val holder = linkages[key] ?: continue
			val linkages = holder.getLinkages()
			for (linkage in linkages) other.registerLinkage(key, linkage)
		}
	}
}
