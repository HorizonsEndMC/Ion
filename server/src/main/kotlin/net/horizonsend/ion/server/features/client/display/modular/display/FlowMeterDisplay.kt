package net.horizonsend.ion.server.features.client.display.modular.display

import net.horizonsend.ion.server.features.transport.cache.PowerTransportCache
import net.kyori.adventure.text.Component

class FlowMeterDisplay(
	private val meter: PowerTransportCache.PowerNode.PowerFlowMeter,
	offsetLeft: Double,
	offsetUp: Double,
	offsetBack: Double,
	scale: Float
): Display(offsetLeft, offsetUp, offsetBack, scale) {
	override fun register() {}
	override fun deRegister() {}

	override fun getText(): Component {
		return Component.text("0")
	}
}
