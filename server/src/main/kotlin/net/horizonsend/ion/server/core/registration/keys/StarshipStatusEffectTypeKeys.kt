package net.horizonsend.ion.server.core.registration.keys

import net.horizonsend.ion.server.features.starship.status_effects.StarshipStatusEffectType

object StarshipStatusEffectTypeKeys : KeyRegistry<StarshipStatusEffectType>(RegistryKeys.STARSHIP_STATUS_EFFECT_PROPERTY_TYPE, StarshipStatusEffectType::class) {
	val CRUISE_SPEED = registerTypedKey<StarshipStatusEffectType>("CRUISE_SPEED")
	val CRUISE_SLOW = registerTypedKey<StarshipStatusEffectType>("CRUISE_SLOW")
	val DIRECT_CONTROL_SPEED = registerTypedKey<StarshipStatusEffectType>("DIRECT_CONTROL_SPEED")
	val DIRECT_CONTROL_SLOW = registerTypedKey<StarshipStatusEffectType>("DIRECT_CONTROL_SLOW")
	val SHIELD_REGENERATION_SPEED = registerTypedKey<StarshipStatusEffectType>("SHIELD_REGENERATION_SPEED")
	val SHIELD_REGENERATION_SLOW = registerTypedKey<StarshipStatusEffectType>("SHIELD_REGENERATION_SLOW")
	val SHIELD_RESISTANCE = registerTypedKey<StarshipStatusEffectType>("SHIELD_RESISTANCE")
	val SHIELD_WEAKNESS = registerTypedKey<StarshipStatusEffectType>("SHIELD_WEAKNESS")
	val JAMMED = registerTypedKey<StarshipStatusEffectType>("JAMMED")
	val WARP_DISRUPTED = registerTypedKey<StarshipStatusEffectType>("WARP_DISRUPTED")
}
