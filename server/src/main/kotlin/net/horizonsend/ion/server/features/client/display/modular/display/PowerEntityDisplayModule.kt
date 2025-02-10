package net.horizonsend.ion.server.features.client.display.modular.display

import io.papermc.paper.adventure.PaperAdventure
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.machine.PowerMachines.prefixComponent
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor

class PowerEntityDisplayModule(
	handler: TextDisplayHandler,
	private val multiblockEntity: PoweredMultiblockEntity,
	offsetLeft: Double = 0.0,
	offsetUp: Double = POWER_TEXT_LINE,
	offsetBack: Double = 0.0,
	scale: Float = MATCH_SIGN_FONT_SIZE,
	val title: Component? = null
): DisplayModule(handler, offsetLeft, offsetUp, offsetBack, scale) {
	private val updateHandler: (PowerStorage) -> Unit = {
		runUpdates()
	}

	override fun register() {
		multiblockEntity.powerStorage.registerUpdateHandler(updateHandler)
	}

	override fun deRegister() {
		multiblockEntity.powerStorage.removeUpdateHandler(updateHandler)
	}

	override fun buildText(): Component {
		return title?.let { ofChildren(it, newline(), formatPower()) } ?: formatPower()
	}

	private fun formatPower(): Component = ofChildren(prefixComponent, text(multiblockEntity.powerStorage.getPower(), NamedTextColor.GREEN))

	override fun toString(): String {
		return "PowerEntityDisplayModule{TextPlain: ${PaperAdventure.asAdventure(entity.text).plainText()}}, Displaying at ${getLocation()}"
	}
}
