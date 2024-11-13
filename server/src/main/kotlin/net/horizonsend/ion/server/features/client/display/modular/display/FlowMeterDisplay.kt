package net.horizonsend.ion.server.features.client.display.modular.display

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.YELLOW

class FlowMeterDisplay(
	private val meter: PowerNode.PowerFlowMeter,
	offsetLeft: Double,
	offsetUp: Double,
	offsetBack: Double,
	scale: Float
): Display(offsetLeft, offsetUp, offsetBack, scale) {
	override fun register() {}
	override fun deRegister() {}

	override fun getText(): Component {
		return meter.formatFlow()
	}

	companion object {
		val firstLine = text("E: ", YELLOW)
		val secondLine = ofChildren(newline(), text("E ", YELLOW), text("/ ", HE_MEDIUM_GRAY), text("Sec", GREEN))
	}
}
