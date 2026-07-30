package net.horizonsend.ion.server.features.starship.status_effects

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.kyori.adventure.text.Component

/**
 * Describes a type of starship status effect.
 *
 * @param key Registry key for the status effect
 * @param displayName Name of the effect that shows up in a message
 * @param description Description of the effect
 * @param displayType What type of number this effect should display as (percent vs integer)
 * @param notifyWhenExpired Whether this effect should notify the user when expired
 * @param stackable Whether this effect can be applied multiple times to the same user (by default, only the
 * strongest effect is applied, similar to vanilla Minecraft status effects)
 * @param oneApplicationPerStarship Whether this effect should only apply once per starship
 */
data class StarshipStatusEffectType(
	val key: IonRegistryKey<StarshipStatusEffectType, out StarshipStatusEffectType>,
	val displayName: Component,
	val description: Component,
	val displayType: DisplayType,
	val notifyWhenExpired: Boolean,
	val stackable: Boolean,
	val oneApplicationPerStarship: Boolean,
) {
	enum class DisplayType {
		PERCENT,
		WHOLE_NUMBER,
	}
}
