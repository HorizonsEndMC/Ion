package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source

import net.horizonsend.ion.server.configuration.starship.StarshipProjectileBalancing
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.SimpleProjectile
import net.kyori.adventure.audience.Audience
import org.bukkit.World
import kotlin.reflect.KClass

class StarshipProjectileSource(val starship: Starship) : ProjectileSource() {
	override fun <B : StarshipProjectileBalancing, T : SimpleProjectile<B>> getBalancing(clazz: KClass<T>): B {
		return starship.balancingManager.getWeapon()
	}

	override fun getWorld(): World {
		return starship.world
	}

	override fun audiences(): Iterable<Audience> = listOf(starship)
}
