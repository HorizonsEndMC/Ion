package net.horizonsend.ion.server.features.client.display.modular.display.fluid

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.DisplayModule
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.InternalStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.StorageContainer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor

abstract class FluidDisplayModule(
	handler: TextDisplayHandler,
	val container: StorageContainer,
	offsetLeft: Double,
	offsetUp: Double,
	offsetBack: Double,
	scale: Float
) : DisplayModule(handler, offsetLeft, offsetUp, offsetBack, scale) {
	private val updateHandler: (InternalStorage) -> Unit = {
		runUpdates()
	}

	override fun register() {
		container.internalStorage.registerUpdateHandler(updateHandler)
	}

	override fun deRegister() {
		container.internalStorage.removeUpdateHandler(updateHandler)
	}

	protected fun formatFluid(): Component {
		val amount = container.internalStorage.getAmount()
		return ofChildren(text(amount, NamedTextColor.GOLD), text("L", NamedTextColor.DARK_GRAY))
	}
}
