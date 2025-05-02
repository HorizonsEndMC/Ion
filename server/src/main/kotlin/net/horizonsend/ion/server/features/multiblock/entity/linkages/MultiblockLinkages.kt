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
}
