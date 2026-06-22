package net.horizonsend.ion.server.features.starship.status_effects

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_BLUE
import net.horizonsend.ion.server.core.registration.keys.StarshipStatusEffectTypeKeys
import net.kyori.adventure.text.Component

object StarshipStatusEffectTypes {
	// 1 + strength = speed increase, default 0
	val CRUISE_SPEED = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.CRUISE_SPEED,
		displayName = Component.text("Cruise Speed Boost", HE_LIGHT_BLUE),
		description = Component.text("Increases cruise speed", HE_LIGHT_BLUE)
	)

	// 1 - strength = speed reduction, default 0
	val CRUISE_SLOW = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.CRUISE_SLOW,
		displayName = Component.text("Cruise Slow", HE_LIGHT_BLUE),
		description = Component.text("Decreases cruise speed", HE_LIGHT_BLUE)
	)

	// 1 + strength = speed increase, default 0
	val DIRECT_CONTROL_SPEED = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.DIRECT_CONTROL_SPEED,
		displayName = Component.text("Direct Control Speed Boost", HE_LIGHT_BLUE),
		description = Component.text("Increases direct control speed", HE_LIGHT_BLUE)
	)

	// 1 - strength = speed reduction, default 0
	val DIRECT_CONTROL_SLOW = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.DIRECT_CONTROL_SLOW,
		displayName = Component.text("Direct Control Slow", HE_LIGHT_BLUE),
		description = Component.text("Decreases direct control speed", HE_LIGHT_BLUE)
	)

	// base regen rate * (1 + strength) = buffed shield regen, default 0
	val SHIELD_REGENERATION_SPEED = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.SHIELD_REGENERATION_SLOW,
		displayName = Component.text("Shield Regeneration Speed Boost", HE_LIGHT_BLUE),
		description = Component.text("Increases shield regeneration rate", HE_LIGHT_BLUE)
	)

	// base regen rate * (1 - strength) = shield regen reduction, default 0
	val SHIELD_REGENERATION_SLOW = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.SHIELD_REGENERATION_SPEED,
		displayName = Component.text("Shield Regeneration Speed Reduction", HE_LIGHT_BLUE),
		description = Component.text("Decreases shield regeneration rate", HE_LIGHT_BLUE)
	)

	// 1 - strength = damage mitigation on damage, default 0
	val SHIELD_RESISTANCE = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.SHIELD_RESISTANCE,
		displayName = Component.text("Shield Resistance", HE_LIGHT_BLUE),
		description = Component.text("Increases shield resistance", HE_LIGHT_BLUE)
	)

	// 1 - strength = damage mitigation on damage, default 0
	val SHIELD_WEAKNESS = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.SHIELD_WEAKNESS,
		displayName = Component.text("Shield Weakness", HE_LIGHT_BLUE),
		description = Component.text("Decreases shield resistance", HE_LIGHT_BLUE)
	)

	// Boolean effect; either the player has it or does not
	val JAMMED = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.JAMMED,
		displayName = Component.text("Jammed", HE_LIGHT_BLUE),
		description = Component.text("Obfuscates other starships' height and distance", HE_LIGHT_BLUE)
	)

	// Boolean effect; either the player has it or does not (might change to numerical if we're adding strength)
	// Stackable effect; can be stacked with other effects of the same type
	val WARP_DISRUPTED = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.WARP_DISRUPTED,
		displayName = Component.text("Warp Disrupted", HE_LIGHT_BLUE),
		description = Component.text("Prevents a starship from jumping to hyperspace", HE_LIGHT_BLUE)
	)
}
