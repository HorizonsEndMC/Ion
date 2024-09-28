package net.horizonsend.ion.server.features.client.display.modular.display.fluid

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.StorageContainer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

class SimpleFluidDisplay(
	storage: StorageContainer,
	offsetLeft: Double,
	offsetUp: Double,
	offsetBack: Double,
	scale: Float
) : FluidDisplay(storage, offsetLeft, offsetUp, offsetBack, scale) {
	override fun getText(): Component {
		return ofChildren(formatFluid(), space(), container.internalStorage.getStoredFluid()?.displayName ?: empty)
	}

	companion object {
		val empty = text("Empty", NamedTextColor.GRAY, TextDecoration.ITALIC)
	}
}
