package net.horizonsend.ion.server.features.multiblock.linkage

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.linkages.MultiblockLinkage
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import java.util.function.Supplier
import kotlin.reflect.KClass

class MultiblockLinkageHolder(
	val entity: MultiblockEntity,
	private val offsetRight: Int,
	private val offsetUp: Int,
	private val offsetForward: Int,
	private val allowedEntities: Array<out KClass<out MultiblockEntity>>,
	private val linkageDirection: RelativeFace
) : Supplier<MultiblockEntity?> {
	val location: BlockKey get() = toBlockKey(entity.manager.getLocalCoordinate(getRelative(entity.globalVec3i, entity.structureDirection, offsetRight, offsetUp, offsetForward)))

	fun register() {
		val manager = entity.manager

		val new = MultiblockLinkage(
			entity,
			allowedEntities,
			location,
			linkageDirection
		)

		manager.getLinkageManager().registerLinkage(location, new)
	}

	fun deRegister() {
		val manager = entity.manager

		manager.getLinkageManager().deRegisterLinkage(location)
	}

	override fun get(): MultiblockEntity? {
		val manager = entity.manager
		val linkage = manager
			.getLinkageManager()
			.getLinkages(location)
			.firstOrNull { it.owner == this.entity }

		return linkage?.getOtherEnd(manager.getLinkageManager())
	}
}
