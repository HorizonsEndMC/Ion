package net.horizonsend.ion.server.features.multiblock.entity.linkages

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import kotlin.reflect.KClass

open class MultiblockLinkage(
	val owner: MultiblockEntity,
	val allowedLinkages: Array<out KClass<out MultiblockEntity>>,
	private var location: BlockKey,
	private var linkDirection: RelativeFace
) {
	fun getLinkLocation(): BlockKey = getRelative(location, linkDirection[owner.structureDirection])


	open fun canLink(to: MultiblockEntity): Boolean {
		return allowedLinkages.any { it.isInstance(to) }
	}

	fun getOtherEnd(manager: MultiblockLinkageManager): MultiblockEntity? {
		val linkages = manager.getLinkages(getLinkLocation())
		return linkages.firstOrNull { canLink(it.owner) && it.linkDirection == linkDirection.opposite }?.owner
	}
}
