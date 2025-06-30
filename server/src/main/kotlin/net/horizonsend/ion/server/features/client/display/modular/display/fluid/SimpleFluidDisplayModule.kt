package net.horizonsend.ion.server.features.client.display.modular.display.fluid

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.space

class SimpleFluidDisplayModule(
	handler: TextDisplayHandler,
	storage: FluidStorageContainer,
	offsetLeft: Double,
	offsetUp: Double,
	offsetBack: Double,
	scale: Float
) : FluidDisplayModule(handler, storage, offsetLeft, offsetUp, offsetBack, scale) {
	override fun buildText(): Component {
		return ofChildren(formatFluid(), space(), container.getContents().type.displayName)
	}
}
