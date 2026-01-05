package net.horizonsend.ion.server.core.registration.registries

import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys
import net.horizonsend.ion.server.core.registration.keys.StarshipStatusEffectTypeKeys
import net.horizonsend.ion.server.features.starship.status_effects.StarshipStatusEffectType
import net.kyori.adventure.text.Component

class StarshipStatusEffectTypeRegistry : Registry<StarshipStatusEffectType>(RegistryKeys.STARSHIP_STATUS_EFFECT_PROPERTY_TYPE) {
	override fun getKeySet(): KeyRegistry<StarshipStatusEffectType> = StarshipStatusEffectTypeKeys

	override fun boostrap() {
		register(StarshipStatusEffectTypeKeys.CRUISE_SPEED, StarshipStatusEffectType(
			key = StarshipStatusEffectTypeKeys.CRUISE_SPEED,
			displayName = Component.text("Cruise Speed Boost"),
			description = Component.empty()
		))
		register(StarshipStatusEffectTypeKeys.CRUISE_SLOW, StarshipStatusEffectType(
			key = StarshipStatusEffectTypeKeys.CRUISE_SLOW,
			displayName = Component.text("Cruise Slow"),
			description = Component.empty()
		))
		register(StarshipStatusEffectTypeKeys.DIRECT_CONTROL_SPEED, StarshipStatusEffectType(
			key = StarshipStatusEffectTypeKeys.DIRECT_CONTROL_SPEED,
			displayName = Component.text("Direct Control Speed Boost"),
			description = Component.empty()
		))
		register(StarshipStatusEffectTypeKeys.DIRECT_CONTROL_SLOW, StarshipStatusEffectType(
			key = StarshipStatusEffectTypeKeys.DIRECT_CONTROL_SLOW,
			displayName = Component.text("Direct Control Slow"),
			description = Component.empty()
		))
		register(StarshipStatusEffectTypeKeys.SHIELD_HEALTH_BOOST, StarshipStatusEffectType(
			key = StarshipStatusEffectTypeKeys.SHIELD_HEALTH_BOOST,
			displayName = Component.text("Shield Health Boost"),
			description = Component.empty()
		))
	}
}
