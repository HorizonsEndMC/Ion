package net.horizonsend.ion.server.features.client.display.modular.display.fluid

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.StorageContainer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.space
import org.bukkit.block.BlockFace

class SimpleFluidDisplay(
	storage: StorageContainer,
	offsetLeft: Double,
	offsetUp: Double,
	offsetBack: Double,
	face: BlockFace,
	scale: Float
) : FluidDisplay(storage, offsetLeft, offsetUp, offsetBack, face, scale) {
	override fun getText(): Component {
		return ofChildren(formatFluid(), space(), storage.storage.getStoredFluid()?.displayName ?: empty())
	}
}
