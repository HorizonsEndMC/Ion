package net.horizonsend.ion.server.features.starship.status_effects

import net.horizonsend.ion.server.core.registration.keys.StarshipStatusEffectTypeKeys
import net.kyori.adventure.text.Component

object StarshipStatusEffectTypes {
	val CRUISE_SPEED = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.CRUISE_SPEED,
		displayName = Component.text("Cruise Speed Boost"),
		description = Component.empty()
	)

	val CRUISE_SLOW = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.CRUISE_SLOW,
		displayName = Component.text("Cruise Slow"),
		description = Component.empty()
	)

	val DIRECT_CONTROL_SPEED = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.DIRECT_CONTROL_SPEED,
		displayName = Component.text("Direct Control Speed Boost"),
		description = Component.empty()
	)

	val DIRECT_CONTROL_SLOW = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.DIRECT_CONTROL_SLOW,
		displayName = Component.text("Direct Control Slow"),
		description = Component.empty()
	)

	val SHIELD_REGENERATION_SPEED = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.SHIELD_REGENERATION_SPEED,
		displayName = Component.text("Shield Regeneration Speed Boost"),
		description = Component.empty()
	)
}
