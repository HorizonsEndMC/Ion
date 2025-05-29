package net.horizonsend.ion.server.features.multiblock.linkage

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.linkages.MultiblockLinkage
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import java.util.function.Supplier
import kotlin.reflect.KClass

class MultiblockLinkageHolder(
	val entity: MultiblockEntity,
	val offsetRight: Int,
	val offsetUp: Int,
	val offsetForward: Int,
	val allowedEntities: Array<out KClass<out MultiblockEntity>>,
	val linkageDirection: RelativeFace
) : Supplier<MultiblockEntity?> {
	val location: BlockKey get() = toBlockKey(entity.getPosRelative(offsetRight, offsetUp, offsetForward))

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

	companion object {
		fun MultiblockEntity.createLinkage(
			offsetRight: Int,
			offsetUp: Int,
			offsetForward: Int,
			linkageDirection: RelativeFace,
			vararg allowedEntities: KClass<out MultiblockEntity>
		): MultiblockLinkageHolder {
			val holder = MultiblockLinkageHolder(
				this,
				offsetRight,
				offsetUp,
				offsetForward,
				allowedEntities,
				linkageDirection
			)

			linkages.add(holder)

			holder.register()
			return holder
		}
	}
}
