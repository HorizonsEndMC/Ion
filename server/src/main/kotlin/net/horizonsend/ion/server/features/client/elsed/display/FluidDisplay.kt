package net.horizonsend.ion.server.features.client.elsed.display

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.InternalStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.StorageContainer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.block.BlockFace

class FluidDisplay(
	val storage: StorageContainer,
	offsetLeft: Double,
	offsetUp: Double,
	offsetBack: Double,
	face: BlockFace,
	scale: Float,
	val title: Component? = null
) : Display(offsetLeft, offsetUp, offsetBack, face, scale) {
	private val updateHandler: (InternalStorage) -> Unit = {
		display()
	}

	override fun register() {
		storage.storage.registerUpdateHandler(updateHandler)
	}

	override fun deRegister() {
		storage.storage.removeUpdateHandler(updateHandler)
	}

	override fun getText(): Component {
		return title?.let { ofChildren(it, Component.newline(), formatFluid()) } ?: formatFluid()
	}

	private fun formatFluid(): Component {
		val amount = storage.storage.getAmount()
		return ofChildren(text(amount, NamedTextColor.GREEN), text("L", NamedTextColor.GRAY))
	}
}
