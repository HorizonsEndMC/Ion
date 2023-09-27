package net.horizonsend.ion.server.features.starship.control.controllers.ai.util

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
			.append(text("Non-Aggressive", NamedTextColor.BLUE))
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
			.append(text("Low Aggressiveness", NamedTextColor.GREEN))
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
			.append(text("Medium Aggressiveness", NamedTextColor.GOLD))
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
			.append(text("HIGHLY AGGRESSIVE", NamedTextColor.RED))
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
			.append(text("EXTREMELY AGGRESSIVE", NamedTextColor.DARK_RED))
			.append(text("]", NamedTextColor.GRAY))
			.build()
	)
}
