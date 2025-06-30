package net.horizonsend.ion.server.features.client.display.modular.display.fluid

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.DisplayModule
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor

abstract class FluidDisplayModule(
    handler: TextDisplayHandler,
    val container: FluidStorageContainer,
    offsetLeft: Double,
    offsetUp: Double,
    offsetBack: Double,
    scale: Float
) : DisplayModule(handler, offsetLeft, offsetUp, offsetBack, scale) {
	private val updateHandler: (FluidStorageContainer) -> Unit = {
		runUpdates()
	}

	override fun register() {
		container.registerUpdateListener(updateHandler)
	}

	override fun deRegister() {
		container.registerUpdateListener(updateHandler)
	}

	protected fun formatFluid(): Component {
		val amount = container.getContents().amount
		return ofChildren(text(amount, NamedTextColor.GOLD), text("L", NamedTextColor.DARK_GRAY))
	}
}
