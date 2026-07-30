package net.horizonsend.ion.server.features.starship.status_effects

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_BLUE
import net.horizonsend.ion.server.core.registration.keys.StarshipStatusEffectTypeKeys
import net.horizonsend.ion.server.features.starship.status_effects.StarshipStatusEffectType.DisplayType
import net.kyori.adventure.text.Component

object StarshipStatusEffectTypes {
	/**
	 * Increases the cruise speed of a starship.
	 * Calculated by multiplying the base cruise speed by (1 + strength).
	 * If strength is 0.15 and base cruise speed is 100 BPS, the final cruise speed is 115 BPS
	 */
	val CRUISE_SPEED = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.CRUISE_SPEED,
		displayName = Component.text("Cruise Speed Boost", HE_LIGHT_BLUE),
		description = Component.text("Increases cruise speed", HE_LIGHT_BLUE),
		displayType = DisplayType.PERCENT,
		notifyWhenExpired = true,
		stackable = false,
		oneApplicationPerStarship = true,
	)

	/**
	 * Decreases the cruise speed of a starship.
	 * Calculated by multiplying the base cruise speed by (1 - strength).
	 * If strength is 0.40 and base cruise speed is 100 BPS, the final cruise speed is 60 BPS
	 */
	val CRUISE_SLOW = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.CRUISE_SLOW,
		displayName = Component.text("Cruise Slow", HE_LIGHT_BLUE),
		description = Component.text("Decreases cruise speed", HE_LIGHT_BLUE),
		displayType = DisplayType.PERCENT,
		notifyWhenExpired = true,
		stackable = false,
		oneApplicationPerStarship = true,
	)

	/**
	 * Increases the direct control speed of a starship.
	 * Calculated by multiplying the base direct control speed by (1 + strength).
	 * If strength is 0.5 and base direct control speed is 20 BPS, the final direct control speed is 30 BPS
	 */
	val DIRECT_CONTROL_SPEED = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.DIRECT_CONTROL_SPEED,
		displayName = Component.text("Direct Control Speed Boost", HE_LIGHT_BLUE),
		description = Component.text("Increases direct control speed", HE_LIGHT_BLUE),
		displayType = DisplayType.PERCENT,
		notifyWhenExpired = true,
		stackable = false,
		oneApplicationPerStarship = true,
	)

	/**
	 * Decreases the direct control speed of a starship.
	 * Calculated by multiplying the base direct control speed by (1 - strength).
	 * If strength is 0.25 and base direct control speed is 20 BPS, the final direct control speed is 15 BPS
	 */
	val DIRECT_CONTROL_SLOW = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.DIRECT_CONTROL_SLOW,
		displayName = Component.text("Direct Control Slow", HE_LIGHT_BLUE),
		description = Component.text("Decreases direct control speed", HE_LIGHT_BLUE),
		displayType = DisplayType.PERCENT,
		notifyWhenExpired = true,
		stackable = false,
		oneApplicationPerStarship = true,
	)

	/**
	 * Increases the rate at which a starship's shields regenerate hitpoints.
	 * Calculated by multiplying the base hitpoint regeneration speed by (1 + strength).
	 * If strength is 0.6 and base hitpoint regeneration speed is 50,000, the final hitpoint regeneration speed is 80,000
	 */
	val SHIELD_REGENERATION_SPEED = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.SHIELD_REGENERATION_SLOW,
		displayName = Component.text("Shield Regeneration Speed Boost", HE_LIGHT_BLUE),
		description = Component.text("Increases shield regeneration rate", HE_LIGHT_BLUE),
		displayType = DisplayType.PERCENT,
		notifyWhenExpired = true,
		stackable = false,
		oneApplicationPerStarship = true,
	)

	/**
	 * Decreases the rate at which a starship's shields regenerate hitpoints.
	 * Calculated by multiplying the base hitpoint regeneration speed by (1 - strength).
	 * If strength is 0.35 and base hitpoint regeneration speed is 50,000, the final hitpoint regeneration speed is 32,500
	 */
	val SHIELD_REGENERATION_SLOW = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.SHIELD_REGENERATION_SPEED,
		displayName = Component.text("Shield Regeneration Speed Reduction", HE_LIGHT_BLUE),
		description = Component.text("Decreases shield regeneration rate", HE_LIGHT_BLUE),
		displayType = DisplayType.PERCENT,
		notifyWhenExpired = true,
		stackable = false,
		oneApplicationPerStarship = true,
	)

	/**
	 * Decreases the number of hitpoints that incoming damage removes.
	 * Calculated by multiplying the base hitpoint damage by (1 - strength).
	 * If strength is 0.4 and base damage is 10,000, the final hitpoint damage is 6,000
	 */
	val SHIELD_RESISTANCE = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.SHIELD_RESISTANCE,
		displayName = Component.text("Shield Resistance", HE_LIGHT_BLUE),
		description = Component.text("Increases shield resistance", HE_LIGHT_BLUE),
		displayType = DisplayType.PERCENT,
		notifyWhenExpired = true,
		stackable = false,
		oneApplicationPerStarship = true,
	)

	/**
	 * Increase the number of hitpoints that incoming damage removes.
	 * Calculated by multiplying the base hitpoint damage by (1 + strength).
	 * If strength is 0.3 and base damage is 10,000, the final hitpoint damage is 13,000
	 */
	val SHIELD_WEAKNESS = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.SHIELD_WEAKNESS,
		displayName = Component.text("Shield Weakness", HE_LIGHT_BLUE),
		description = Component.text("Decreases shield resistance", HE_LIGHT_BLUE),
		displayType = DisplayType.PERCENT,
		notifyWhenExpired = true,
		stackable = false,
		oneApplicationPerStarship = true,
	)

	/**
	 * Scrambles the affected starship pilot's contacts.
	 * This effect does not have strength and is either active, or not
	 */
	val JAMMED = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.JAMMED,
		displayName = Component.text("Jammed", HE_LIGHT_BLUE),
		description = Component.text("Obfuscates other starships' height and distance", HE_LIGHT_BLUE),
		displayType = DisplayType.PERCENT,
		notifyWhenExpired = true,
		stackable = false,
		oneApplicationPerStarship = true,
	)

	/**
	 * Prevents the affected starship from jumping into hyperspace.
	 * This effect is stackable; additional applications of this effect will increase the total strength of the
	 * effect on the starship. (The well strength is equal to the sum of the strengths of all disrupt effects)
	 */
	val WARP_DISRUPTED = StarshipStatusEffectType(
		key = StarshipStatusEffectTypeKeys.WARP_DISRUPTED,
		displayName = Component.text("Warp Disrupted", HE_LIGHT_BLUE),
		description = Component.text("Prevents a starship from jumping to hyperspace", HE_LIGHT_BLUE),
		displayType = DisplayType.WHOLE_NUMBER,
		notifyWhenExpired = false,
		stackable = true,
		oneApplicationPerStarship = true,
	)
}
