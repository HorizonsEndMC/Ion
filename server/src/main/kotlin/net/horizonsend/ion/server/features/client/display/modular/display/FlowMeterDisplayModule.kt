package net.horizonsend.ion.server.features.client.display.modular.display

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.YELLOW

class FlowMeterDisplayModule(
	handler: TextDisplayHandler,
	private val meter: PowerNode.PowerFlowMeter,
	offsetRight: Double,
	offsetUp: Double,
	offsetForward: Double,
	scale: Float
): DisplayModule(handler, offsetRight, offsetUp, offsetForward, scale) {
	override fun register() {}
	override fun deRegister() {}

	override fun buildText(): Component {
		return meter.formatFlow()
	}

	companion object {
		val firstLine = ofChildren(text("Δ", GREEN), text("E: ", YELLOW))
	}
}
