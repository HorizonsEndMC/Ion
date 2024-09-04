package net.horizonsend.ion.server.features.client.display.modular.display.fluid

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.modular.display.Display
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.InternalStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.StorageContainer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.block.BlockFace

abstract class FluidDisplay(
	val storage: StorageContainer,
	offsetLeft: Double,
	offsetUp: Double,
	offsetBack: Double,
	face: BlockFace,
	scale: Float
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

	protected fun formatFluid(): Component {
		val amount = storage.storage.getAmount()
		return ofChildren(text(amount, NamedTextColor.GOLD), text("L", NamedTextColor.DARK_GRAY))
	}
}
