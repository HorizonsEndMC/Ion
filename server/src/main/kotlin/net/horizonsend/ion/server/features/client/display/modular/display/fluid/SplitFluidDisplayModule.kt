package net.horizonsend.ion.server.features.client.display.modular.display.fluid

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.StorageContainer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline

class SplitFluidDisplayModule(
	handler: TextDisplayHandler,
	storage: StorageContainer,
	offsetLeft: Double,
	offsetUp: Double,
	offsetBack: Double,
	scale: Float
) : FluidDisplayModule(handler, storage, offsetLeft, offsetUp, offsetBack, scale) {
	override fun buildText(): Component {
		return ofChildren(container.internalStorage.getFluidType().displayName, newline(), formatFluid())
	}
}
