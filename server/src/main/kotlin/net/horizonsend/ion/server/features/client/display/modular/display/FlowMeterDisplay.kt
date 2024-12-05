package net.horizonsend.ion.server.features.client.display.modular.display

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftTextDisplay

class FlowMeterDisplay(
	private val meter: PowerNode.PowerFlowMeter,
	offsetRight: Double,
	offsetUp: Double,
	offsetForward: Double,
	scale: Float
): Display(offsetRight, offsetUp, offsetForward, scale) {
	override fun register() {}
	override fun deRegister() {}

	override fun getText(): Component {
		return meter.formatFlow()
	}

	companion object {
		val firstLine = text("E: ", YELLOW)
		val secondLine = ofChildren(newline(), text("E ", YELLOW), text("/ ", HE_MEDIUM_GRAY), text("Sec", GREEN))
	}

	override fun createEntity(parent: TextDisplayHandler): CraftTextDisplay {
		Throwable().printStackTrace()

		println("Create entity $this")
		return super.createEntity(parent)
	}
}
