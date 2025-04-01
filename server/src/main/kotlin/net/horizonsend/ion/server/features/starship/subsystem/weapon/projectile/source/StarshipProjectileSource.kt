package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source

import net.horizonsend.ion.server.features.starship.Starship
import net.kyori.adventure.audience.Audience

class StarshipProjectileSource(val starship: Starship) : ProjectileSource() {
	override fun audiences(): Iterable<Audience> = listOf(starship)
}
