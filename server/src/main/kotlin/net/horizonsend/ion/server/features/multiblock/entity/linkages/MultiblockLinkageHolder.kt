package net.horizonsend.ion.server.features.multiblock.entity.linkages

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import java.util.function.Supplier

class MultiblockLinkageHolder(
	val holder: MultiblockEntity,
	private val offsetRight: Int,
	private val offsetUp: Int,
	private val offsetForward: Int,
	private val multiblockFilter: (MultiblockEntity) -> Boolean,
	private val linkageDirection: RelativeFace
) : Supplier<MultiblockEntity?> {
	val location: BlockKey get() = toBlockKey(holder.manager.getLocalCoordinate(getRelative(holder.globalVec3i, holder.structureDirection, offsetRight, offsetUp, offsetForward)))

	fun register() {
		val manager = holder.manager

		val new = MultiblockLinkage(
			holder,
			multiblockFilter,
			location,
			linkageDirection
		)

		manager.getLinkageManager().registerLinkage(location, new)
	}

	fun deRegister() {
		val manager = holder.manager

		manager.getLinkageManager().deRegisterLinkage(location)
	}

	override fun get(): MultiblockEntity? {
		val manager = holder.manager
		val linkage = manager
			.getLinkageManager()
			.getLinkages(location)
			.firstOrNull { it.owner == this.holder }

		return linkage?.getOtherEnd(manager.getLinkageManager())
	}
}
