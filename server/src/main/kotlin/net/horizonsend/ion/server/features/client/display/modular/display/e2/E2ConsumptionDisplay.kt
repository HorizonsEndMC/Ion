package net.horizonsend.ion.server.features.client.display.modular.display.e2

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.DisplayModule
import net.horizonsend.ion.server.features.multiblock.entity.type.e2.E2Multiblock
import net.horizonsend.ion.server.features.starship.destruction.SinkAnimation
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit.getPlayer
import java.text.DecimalFormat
import kotlin.math.roundToInt

class E2ConsumptionDisplay(
	handler: TextDisplayHandler,
	val multiblock: E2Multiblock,
	offsetLeft: Double,
	offsetUp: Double,
	offsetBack: Double,
	scale: Float,
	relativeFace: RelativeFace = RelativeFace.FORWARD,
) : DisplayModule(handler, offsetLeft, offsetUp, offsetBack, scale, relativeFace, updateRateProvider = provider@{
	val nearbyViewers = (getPossibleViewers(true) ?: return@provider 1000L).mapNotNull(::getPlayer).any { player -> player.location.distance(it) < 10 }
	if (nearbyViewers) 100L else 1000L
}) {
	private val updateHandler: (E2Multiblock) -> Unit = {
		runUpdates()
	}

	override fun register() {
		multiblock.e2Manager.registerUpdateListener(updateHandler)
	}

	override fun deRegister() {
		multiblock.e2Manager.deregisterUpdateListener(updateHandler)
	}

	companion object {
		val format = DecimalFormat("##.##")
		private const val BOLT_CHARACTER = '⚡'
		private val BOLT_TEXT = text(BOLT_CHARACTER, NamedTextColor.GOLD)
	}

	private fun formatPower(): Component {
		var amount = multiblock.getTotalE2Consumption()

		var unit = "W"

		if (amount > 1000.0) {
			amount /= 1000.0
			unit = "kW"
		}

		var color: TextColor = NamedTextColor.GREEN
		val availablePower = multiblock.getAvailablePowerPercentage()
		if (availablePower < 1.0) color = NamedTextColor.RED
		else if (availablePower == 1.0) color = NamedTextColor.YELLOW
		else {
			val phase: Double = minOf(availablePower / 10.0, 1.0)

			val r = SinkAnimation.blend(NamedTextColor.YELLOW.red(), NamedTextColor.DARK_GREEN.red(), phase).roundToInt().coerceAtMost(255)
			val g = SinkAnimation.blend(NamedTextColor.YELLOW.green(), NamedTextColor.DARK_GREEN.green(), phase).roundToInt().coerceAtMost(255)
			val b = SinkAnimation.blend(NamedTextColor.YELLOW.blue(), NamedTextColor.DARK_GREEN.blue(), phase).roundToInt().coerceAtMost(255)
			color = TextColor.color(r, g, b)
		}

		return ofChildren(BOLT_TEXT, Component.space(), text(format.format(amount), color), text(unit, NamedTextColor.WHITE))
	}

	override fun buildText(): Component {
		return formatPower()
	}
}
