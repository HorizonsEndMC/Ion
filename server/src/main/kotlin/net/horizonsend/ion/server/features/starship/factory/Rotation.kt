package net.horizonsend.ion.server.features.starship.factory

import net.kyori.adventure.text.Component

enum class Rotation(thetaDegrees: Double, val displayName: Component) {
	NONE(0.0, Component.text("None")),
	CLOCKWISE_90(90.0, Component.text("Clockwise 90 Degrees")),
	CLOCKWISE_180(180.0, Component.text("180 Degrees")),
	COUNTERCLOCKWISE_90(270.0, Component.text("Counterclockwise 90 Degrees"));

	val radians = Math.toRadians(thetaDegrees)
}
