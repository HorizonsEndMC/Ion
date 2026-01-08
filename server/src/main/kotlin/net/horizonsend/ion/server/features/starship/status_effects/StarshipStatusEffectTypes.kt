package net.horizonsend.ion.server.features.starship.status_effects

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_BLUE
import net.horizonsend.ion.server.core.registration.keys.StarshipStatusEffectTypeKeys
import net.kyori.adventure.text.Component

object StarshipStatusEffectTypes {
	val CRUISE_SPEED = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.CRUISE_SPEED,
		displayName = Component.text("Cruise Speed Boost", HE_LIGHT_BLUE),
		description = Component.text("Increases cruise speed", HE_LIGHT_BLUE)
	)

	val CRUISE_SLOW = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.CRUISE_SLOW,
		displayName = Component.text("Cruise Slow", HE_LIGHT_BLUE),
		description = Component.text("Decreases cruise speed", HE_LIGHT_BLUE)
	)

	val DIRECT_CONTROL_SPEED = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.DIRECT_CONTROL_SPEED,
		displayName = Component.text("Direct Control Speed Boost", HE_LIGHT_BLUE),
		description = Component.text("Increases direct control speed", HE_LIGHT_BLUE)
	)

	val DIRECT_CONTROL_SLOW = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.DIRECT_CONTROL_SLOW,
		displayName = Component.text("Direct Control Slow", HE_LIGHT_BLUE),
		description = Component.text("Decreases direct control speed", HE_LIGHT_BLUE)
	)

	val SHIELD_REGENERATION_SPEED = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.SHIELD_REGENERATION_SPEED,
		displayName = Component.text("Shield Regeneration Speed Boost", HE_LIGHT_BLUE),
		description = Component.text("Increases shield regeneration rate", HE_LIGHT_BLUE)
	)
}
