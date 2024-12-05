package net.horizonsend.ion.server.features.client.display.modular.display.fluid

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.StorageContainer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftTextDisplay

class SimpleFluidDisplay(
	storage: StorageContainer,
	offsetLeft: Double,
	offsetUp: Double,
	offsetBack: Double,
	scale: Float
) : FluidDisplay(storage, offsetLeft, offsetUp, offsetBack, scale) {
	override fun getText(): Component {
		return ofChildren(formatFluid(), space(), container.internalStorage.getFluidType()?.displayName ?: empty)
	}

	companion object {
		val empty = text("Empty", NamedTextColor.GRAY, TextDecoration.ITALIC)
	}

	override fun createEntity(parent: TextDisplayHandler): CraftTextDisplay {
		Throwable().printStackTrace()

		println("Create entity $this")
		return super.createEntity(parent)
	}
}
