package net.horizonsend.ion.server.features.client.display.modular.display.fluid

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.StorageContainer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import org.bukkit.block.BlockFace

class ComplexFluidDisplay(
	storage: StorageContainer,
	val title: Component,
	offsetLeft: Double,
	offsetUp: Double,
	offsetBack: Double,
	face: BlockFace,
	scale: Float
) : FluidDisplay(storage, offsetLeft, offsetUp, offsetBack, face, scale) {

	override fun getText(): Component {
		return ofChildren(title, newline(), formatFluid())
	}
}
