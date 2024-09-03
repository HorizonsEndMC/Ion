package net.horizonsend.ion.server.features.client.elsed.display.fluid

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.elsed.TextDisplayHandler
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.StorageContainer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import org.bukkit.Color
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftTextDisplay

class ComplexFluidDisplay(
	storage: StorageContainer,
	val title: Component,
	offsetLeft: Double,
	offsetUp: Double,
	offsetBack: Double,
	face: BlockFace,
	scale: Float
) : FluidDisplay(storage, offsetLeft, offsetUp, offsetBack, face, scale) {
	override fun createEntity(parent: TextDisplayHandler): CraftTextDisplay {
		return super.createEntity(parent).apply {
			backgroundColor = Color.fromARGB(0x00BBBBBB)
		}
	}

	override fun getText(): Component {
		return ofChildren(title, newline(), formatFluid())
	}
}
