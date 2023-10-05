package net.horizonsend.ion.server.features.starship.control.controllers.ai.utils

import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.CombatAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.TemporaryAIController
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

enum class AggressivenessLevel(
	val engagementDistance: Double,
	val disengageMultiplier: Double,
	val shotDeviation: Double,
	val color: TextColor,
	val displayName: Component,
) {
	NONE(
		0.0,
		10.0,
		0.15,
		NamedTextColor.BLUE,
		text()
			.append(text("[", NamedTextColor.GRAY))
			.append(text("❤", NamedTextColor.BLUE))
			.append(text("]", NamedTextColor.GRAY))
			.build()
	),
	LOW(
		500.0,
		5.0,
		0.1,
		NamedTextColor.GREEN,
		text()
			.append(text("[", NamedTextColor.GRAY))
			.append(text("✔", NamedTextColor.GREEN))
			.append(text("]", NamedTextColor.GRAY))
			.build()
	),
	MEDIUM(
		1000.0,
		2.5,
		0.05,
		NamedTextColor.GOLD,
		text()
			.append(text("[", NamedTextColor.GRAY))
			.append(text("⚠", NamedTextColor.GOLD))
			.append(text("]", NamedTextColor.GRAY))
			.build()
	),
	HIGH(
		2500.0,
		2.5,
		0.025,
		NamedTextColor.RED,
		text()
			.append(text("[", NamedTextColor.GRAY))
			.append(text("✖", NamedTextColor.RED))
			.append(text("]", NamedTextColor.GRAY))
			.build()
	),
	EXTREME(
		5000.0,
		1.0,
		0.0,
		NamedTextColor.DARK_RED,
		text()
			.append(text("[", NamedTextColor.GRAY))
			.append(text("☠", NamedTextColor.DARK_RED))
			.append(text("]", NamedTextColor.GRAY))
			.build()
	) {
		override fun disengage(controller: AIController) {
			if (controller !is CombatAIController) return

			// Populate the target value of the controller with the next target
			findNextTarget(controller)

			// If none found, return
			if (controller.target == null) controller.returnToPreviousController()
		}
	}

	;

	open fun disengage(controller: AIController) {
		if (controller !is TemporaryAIController) return

		controller.returnToPreviousController()
	}

	fun findNextTarget(controller: AIController) {
		if (controller !is CombatAIController) return

		val nearbyShips = controller.getNearbyShips(0.0, engagementDistance) { starship, _ ->
			starship.controller !is AIController
		}

		controller.target = nearbyShips.firstOrNull()
	}

	open fun onDamaged(damager: Damager) {}
}
