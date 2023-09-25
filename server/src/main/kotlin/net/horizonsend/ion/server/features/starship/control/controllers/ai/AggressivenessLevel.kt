package net.horizonsend.ion.server.features.starship.control.controllers.ai

enum class AggressivenessLevel(
	val engagementDistance: Double,
	val disengageMultiplier: Double,
) {
	NONE(0.0, 10.0),
	LOW(500.0, 5.0),
	MEDIUM(1000.0, 2.5),
	HIGH(2500.0, 2.5),
	EXTREME(5000.0, 0.0)
}
