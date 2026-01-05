package net.horizonsend.ion.server.core.registration.registries

import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys
import net.horizonsend.ion.server.core.registration.keys.StarshipStatusEffectTypeKeys
import net.horizonsend.ion.server.features.starship.status_effects.StarshipStatusEffectType
import net.horizonsend.ion.server.features.starship.status_effects.StarshipStatusEffectTypes
import net.kyori.adventure.text.Component

class StarshipStatusEffectTypeRegistry : Registry<StarshipStatusEffectType>(RegistryKeys.STARSHIP_STATUS_EFFECT_PROPERTY_TYPE) {
	override fun getKeySet(): KeyRegistry<StarshipStatusEffectType> = StarshipStatusEffectTypeKeys

	override fun boostrap() {
		register(StarshipStatusEffectTypeKeys.CRUISE_SPEED, StarshipStatusEffectTypes.CRUISE_SPEED)
		register(StarshipStatusEffectTypeKeys.CRUISE_SLOW, StarshipStatusEffectTypes.CRUISE_SLOW)
		register(StarshipStatusEffectTypeKeys.DIRECT_CONTROL_SPEED, StarshipStatusEffectTypes.DIRECT_CONTROL_SPEED)
		register(StarshipStatusEffectTypeKeys.DIRECT_CONTROL_SLOW, StarshipStatusEffectTypes.DIRECT_CONTROL_SLOW)
		register(StarshipStatusEffectTypeKeys.SHIELD_HEALTH_BOOST, StarshipStatusEffectTypes.SHIELD_HEALTH_BOOST)
	}
}
