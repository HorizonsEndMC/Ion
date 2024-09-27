package net.horizonsend.ion.server.features.client.display.modular.display

import net.horizonsend.ion.server.features.transport.node.type.general.FlowMeter
import net.kyori.adventure.text.Component

class FlowMeterDisplay(
	private val meter: FlowMeter,
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
}
