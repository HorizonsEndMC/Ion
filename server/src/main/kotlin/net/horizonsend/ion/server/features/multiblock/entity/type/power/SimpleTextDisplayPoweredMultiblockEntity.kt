package net.horizonsend.ion.server.features.multiblock.entity.type.power

import net.horizonsend.ion.server.features.client.display.container.DisplayHandlerHolder
import net.horizonsend.ion.server.features.client.display.container.TextDisplayHandler
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i

/**
 * Interface providing a simple implementation of a visual display via text display entities.
 **/
interface SimpleTextDisplayPoweredMultiblockEntity : PoweredMultiblockEntity, DisplayHandlerHolder {
	override fun updatePowerVisually() {
		displayHandler.setText(formatPower())
	}

	override fun refresh() {
		updatePowerVisually()
	}

	companion object {
		fun createTextDisplayHandler(entity: SimpleTextDisplayPoweredMultiblockEntity): TextDisplayHandler {
			require(entity is MultiblockEntity)

			val signDirection = entity.facing.oppositeFace
			val signLoc = Vec3i(entity.x, entity.y, entity.z) + Vec3i(signDirection.modX, 0, signDirection.modZ)

			// 70% of the way through the block
			val offset = signDirection.direction.multiply(0.39)

			return TextDisplayHandler(
				entity,
				entity.world,
				signLoc.x.toDouble() + 0.5 - offset.x,
				signLoc.y.toDouble() + 0.4,
				signLoc.z.toDouble() + 0.5 - offset.z,
				0.5f,
				signDirection
			)
		}
	}
}
