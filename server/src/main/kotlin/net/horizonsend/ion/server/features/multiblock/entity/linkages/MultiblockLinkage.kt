package net.horizonsend.ion.server.features.multiblock.entity.linkages

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i

open class MultiblockLinkage(
	val owner: MultiblockEntity,
	private val multiblockFilter: (MultiblockEntity) -> Boolean,
	val location: BlockKey,
	val linkDirection: RelativeFace
) {
	fun getLinkLocation(): BlockKey = toBlockKey(owner.manager.getLocalCoordinate(owner.manager.getGlobalCoordinate(toVec3i(location)).getRelative(linkDirection[owner.structureDirection])))

	open fun canLink(to: MultiblockEntity): Boolean {
		return multiblockFilter.invoke(to)
	}

	fun getOtherEnd(manager: MultiblockLinkageManager): MultiblockEntity? {
		val linkages = manager.getLinkages(getLinkLocation())
		return linkages.firstOrNull { canLink(it.owner) && it.linkDirection[it.owner.structureDirection] == linkDirection[owner.structureDirection].oppositeFace }?.owner
	}
}
