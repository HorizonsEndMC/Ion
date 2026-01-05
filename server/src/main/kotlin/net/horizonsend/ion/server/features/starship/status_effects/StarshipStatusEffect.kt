package net.horizonsend.ion.server.features.starship.status_effects

data class StarshipStatusEffect(
	val type: StarshipStatusEffectType,
	val strength: Double,
	var durationSeconds: Long
) {
	var isActive = false
}
