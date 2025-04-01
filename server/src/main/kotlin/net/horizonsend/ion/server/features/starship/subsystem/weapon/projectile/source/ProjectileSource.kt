package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.SimpleProjectile
import net.kyori.adventure.audience.ForwardingAudience
import org.bukkit.World
import kotlin.reflect.KClass

abstract class ProjectileSource : ForwardingAudience {
	abstract fun <B : StarshipWeapons.StarshipProjectileBalancing, T : SimpleProjectile<B>> getBalancing(clazz: KClass<T>): B

	inline fun <B : StarshipWeapons.StarshipProjectileBalancing, reified T : SimpleProjectile<B>> getBalancing(): B {
		return getBalancing(T::class)
	}

	abstract fun getWorld(): World
}
