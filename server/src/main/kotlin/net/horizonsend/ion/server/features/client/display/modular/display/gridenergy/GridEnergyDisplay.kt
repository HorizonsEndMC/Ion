package net.horizonsend.ion.server.features.client.display.modular.display.gridenergy

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.DisplayModule
import net.horizonsend.ion.server.features.multiblock.entity.type.gridenergy.GridEnergyMultiblock
import net.horizonsend.ion.server.features.starship.destruction.SinkAnimation
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit.getPlayer
import java.text.DecimalFormat
import kotlin.math.roundToInt

class GridEnergyDisplay(
    handler: TextDisplayHandler,
    val multiblock: GridEnergyMultiblock,
    offsetLeft: Double,
    offsetUp: Double,
    offsetBack: Double,
    scale: Float,
    relativeFace: RelativeFace = RelativeFace.FORWARD,
) : DisplayModule(handler, offsetLeft, offsetUp, offsetBack, scale, relativeFace, updateRateProvider = provider@{
	val nearbyViewers = (getPossibleViewers(true) ?: return@provider 1000L).mapNotNull(::getPlayer).any { player -> player.location.distance(it) < 10 }
	if (nearbyViewers) 100L else 1000L
}) {
	private val updateHandler: (GridEnergyMultiblock) -> Unit = {
		runUpdates()
	}

	override fun register() {
		multiblock.gridEnergyManager.registerUpdateListener(updateHandler)
	}

	override fun deRegister() {
		multiblock.gridEnergyManager.deregisterUpdateListener(updateHandler)
	}

	companion object {
		val format = DecimalFormat("##.##")
		private const val BOLT_CHARACTER = '⚡'
		private val BOLT_TEXT = text(BOLT_CHARACTER, NamedTextColor.YELLOW)
		private val CONSUMPTION = text('-', NamedTextColor.RED)
		private val PRODUCTION = text('+', NamedTextColor.GREEN)
	}

	private fun formatPower(): Component {
		var consumptionAmount = multiblock.getTotalGridEnergyConsumption()
		val produced = multiblock.getGridEnergyOutput()

		if (consumptionAmount == 0.0 && produced == 0.0) {
			return ofChildren(BOLT_TEXT, Component.space(), text(format.format(0.0), NamedTextColor.DARK_GRAY), text("W", NamedTextColor.WHITE))
		}

		if (consumptionAmount < produced) return formatOutput(produced)

		var unit = "W"
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

		return ofChildren(CONSUMPTION, BOLT_TEXT, Component.space(), text(format.format(consumptionAmount), color), text(unit, NamedTextColor.WHITE))
	}

	fun formatOutput(amount: Double): Component {
		var amount = amount

		var unit = "W"

		if (amount > 1000.0) {
			amount /= 1000.0
			unit = "kW"
		}

		return ofChildren(PRODUCTION, BOLT_TEXT, Component.space(), text(format.format(amount), NamedTextColor.GREEN), text(unit, NamedTextColor.WHITE))
	}

	override fun buildText(): Component {
		return formatPower()
	}
}
