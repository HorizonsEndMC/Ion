package net.horizonsend.ion.server.features.starship.status_effects

import net.horizonsend.ion.server.features.starship.Starship

data class StarshipStatusEffect(
	val type: StarshipStatusEffectType,
	val strength: Double,
	var durationMillis: Long,
	val applier: Starship?,
) {
	var isActive = false
}
