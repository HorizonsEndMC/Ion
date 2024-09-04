package net.horizonsend.ion.server.features.client.display.modular.display

import net.horizonsend.ion.server.features.transport.node.power.PowerFlowMeter
import net.kyori.adventure.text.Component
import org.bukkit.block.BlockFace

class PowerFlowMeterDisplay(
	private val meter: PowerFlowMeter,
	offsetLeft: Double,
	offsetUp: Double,
	offsetBack: Double,
	face: BlockFace,
	scale: Float
): Display(offsetLeft, offsetUp, offsetBack, face, scale) {
	override fun register() {}
	override fun deRegister() {}

	override fun getText(): Component {
		return meter.formatPower()
	}
}
