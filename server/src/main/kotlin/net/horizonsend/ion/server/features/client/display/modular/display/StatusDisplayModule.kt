package net.horizonsend.ion.server.features.client.display.modular.display

import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.kyori.adventure.text.Component

class StatusDisplayModule(
	handler: TextDisplayHandler,
    private val statusSupplier: StatusMultiblockEntity.StatusManager,
    offsetLeft: Double = 0.0,
    offsetUp: Double = STATUS_TEXT_LINE,
    offsetBack: Double = 0.0,
    scale: Float = MATCH_SIGN_FONT_SIZE,
) : DisplayModule(handler, offsetLeft, offsetUp, offsetBack, scale) {
	private val updateHandler: Runnable = Runnable { runUpdates() }

	override fun register() {
		statusSupplier.updateManager.add(updateHandler)
	}

	override fun deRegister() {
		statusSupplier.updateManager.remove(updateHandler)
	}

	override fun buildText(): Component {
		return statusSupplier.status
	}
}
