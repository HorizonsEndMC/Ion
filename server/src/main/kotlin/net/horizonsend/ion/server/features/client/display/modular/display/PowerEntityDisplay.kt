package net.horizonsend.ion.server.features.client.display.modular.display

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.machine.PowerMachines.prefixComponent
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor

class PowerEntityDisplay(
	private val multiblockEntity: PoweredMultiblockEntity,
	offsetLeft: Double,
	offsetUp: Double,
	offsetBack: Double,
	scale: Float,
	val title: Component? = null
): Display(offsetLeft, offsetUp, offsetBack, scale) {
	private val updateHandler: (PowerStorage) -> Unit = {
		display()
	}

	override fun register() {
//		multiblockEntity.storage.registerUpdateHandler(updateHandler)
	}

	override fun deRegister() {
//		multiblockEntity.storage.removeUpdateHandler(updateHandler)
	}

	override fun getText(): Component {
		return title?.let { ofChildren(it, newline(), formatPower()) } ?: formatPower()
	}

	private fun formatPower(): Component = ofChildren(prefixComponent, text(multiblockEntity.powerStorage.getPower(), NamedTextColor.GREEN))
}
