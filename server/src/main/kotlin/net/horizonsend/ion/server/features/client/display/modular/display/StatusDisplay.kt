package net.horizonsend.ion.server.features.client.display.modular.display

import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblock
import net.kyori.adventure.text.Component

class StatusDisplay(
	private val statusSupplier: StatusMultiblock.StatusManager,
	offsetLeft: Double,
	offsetUp: Double,
	offsetBack: Double,
	scale: Float,
) : Display(offsetLeft, offsetUp, offsetBack, scale) {
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
