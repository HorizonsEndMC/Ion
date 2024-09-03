package net.horizonsend.ion.server.features.client.elsed.display

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.machine.PowerMachines.prefixComponent
import net.horizonsend.ion.server.features.multiblock.entity.type.power.UpdatedPowerDisplayEntity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.block.BlockFace

class PowerDisplay(
	private val multiblockEntity: UpdatedPowerDisplayEntity,
	offsetLeft: Double,
	offsetUp: Double,
	offsetBack: Double,
	face: BlockFace,
	scale: Float,
	val title: Component? = null
): Display(offsetLeft, offsetUp, offsetBack, face, scale) {
	private val updateHandler: (UpdatedPowerDisplayEntity) -> Unit = {
		display()
	}

	override fun register() {
		multiblockEntity.displayUpdates.add(updateHandler)
	}

	override fun deRegister() {
		multiblockEntity.displayUpdates.remove(updateHandler)
	}

	override fun getText(): Component {
		return title?.let { ofChildren(it, newline(), formatPower()) } ?: formatPower()
	}

	private fun formatPower(): Component = ofChildren(prefixComponent, text(multiblockEntity.getPower(), NamedTextColor.GREEN))
}
