package net.horizonsend.ion.server.features.multiblock.entity.linkages

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import org.bukkit.block.BlockFace
import kotlin.reflect.KClass

open class MultiblockLinkage(
	val owner: MultiblockEntity,
	val allowedLinkages: Array<KClass<out MultiblockEntity>>,
	private var location: BlockKey,
	private var linkDirection: BlockFace
) {
	fun getLinkLocation(): BlockKey = getRelative(location, linkDirection)

	fun displace(movement: StarshipMovement) {
		location = movement.displaceKey(location)
		linkDirection = movement.displaceFace(linkDirection)
	}

	open fun canLink(to: MultiblockEntity): Boolean {
		return allowedLinkages.any { it.isInstance(to) }
	}

	fun getOtherEnd(manager: MultiblockLinkageManager): MultiblockEntity? {
		val linkages = manager.getLinkages(getLinkLocation())
		return linkages.firstOrNull { canLink(it.owner) }?.owner
	}
}
