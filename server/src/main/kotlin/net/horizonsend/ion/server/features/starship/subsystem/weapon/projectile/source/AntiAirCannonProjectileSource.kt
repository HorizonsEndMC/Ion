package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source

import net.horizonsend.ion.server.configuration.starship.AntiAirProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipProjectileBalancing
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.SimpleProjectile
import net.kyori.adventure.audience.Audience
import org.bukkit.World
import org.bukkit.entity.Player
import kotlin.collections.Iterable
import kotlin.reflect.KClass

class AntiAirCannonProjectileSource(val shooter: Player) : ProjectileSource() {

	@Suppress("UNCHECKED_CAST")
	override fun <B : StarshipProjectileBalancing, T : SimpleProjectile<B>> getBalancing(
		clazz: KClass<T>
	): B {
		return AntiAirProjectileBalancing() as B
	}

	override fun getWorld(): World {
		return shooter.world
	}

	override fun audiences(): Iterable<Audience?> = listOf(shooter)
}
