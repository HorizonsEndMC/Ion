package net.horizonsend.ion.server.features.starship.status_effects

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.kyori.adventure.text.Component

data class StarshipStatusEffectType(
	val key: IonRegistryKey<StarshipStatusEffectType, out StarshipStatusEffectType>,
	val displayName: Component,
	val description: Component
)
