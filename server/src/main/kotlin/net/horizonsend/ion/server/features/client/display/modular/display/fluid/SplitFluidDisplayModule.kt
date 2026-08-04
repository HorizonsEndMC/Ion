package net.horizonsend.ion.server.features.client.display.modular.display.fluid

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline

class SplitFluidDisplayModule(
	handler: TextDisplayHandler,
	storage: FluidStorageContainer,
	offsetLeft: Double,
	offsetUp: Double,
	offsetBack: Double,
	scale: Float,
	relativeFace: RelativeFace = RelativeFace.FORWARD
) : FluidDisplayModule(handler, storage, offsetLeft, offsetUp, offsetBack, scale, relativeFace) {
	override fun buildText(): Component {
		return ofChildren(container.getContents().type.getValue().displayName, newline(), formatFluid())
	}
}
