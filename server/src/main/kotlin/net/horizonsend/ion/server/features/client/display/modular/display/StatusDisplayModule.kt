package net.horizonsend.ion.server.features.client.display.modular.display

import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.kyori.adventure.text.Component

class StatusDisplayModule(
	handler: TextDisplayHandler,
    private val statusSupplier: StatusMultiblockEntity.StatusManager,
    offsetLeft: Double,
    offsetUp: Double,
    offsetBack: Double,
    scale: Float,
) : DisplayModule(handler, offsetLeft, offsetUp, offsetBack, scale) {
	private val updateHandler: Runnable = Runnable { display() }

	override fun register() {
		statusSupplier.updateManager.add(updateHandler)
	}

	override fun deRegister() {
		statusSupplier.updateManager.remove(updateHandler)
	}

	override fun getText(): Component {
		return statusSupplier.status
	}
}
