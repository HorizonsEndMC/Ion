package net.horizonsend.ion.server.features.client.elsed

import net.horizonsend.ion.server.features.client.elsed.display.Display
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import org.bukkit.block.Sign

object DisplayHandlers {
	fun newSignOverlay(sign: Sign) {

	}

	fun newMultiblockSignOverlay(entity: MultiblockEntity, vararg display: Display): TextDisplayHandler {
		val signDirection = entity.structureDirection.oppositeFace
		val signBlock = MultiblockEntity.getSignFromOrigin(entity.getOrigin(), entity.structureDirection)

		val offset = signDirection.direction.multiply(0.39)

		return TextDisplayHandler(
			entity.world,
			signBlock.x.toDouble() + 0.5 - offset.x,
			signBlock.y.toDouble() + 0.4,
			signBlock.z.toDouble() + 0.5 - offset.z,
			*display
		)
	}

	fun newMultiText() {

	}

	fun newSingleText() {

	}
}
