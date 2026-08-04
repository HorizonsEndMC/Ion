package net.horizonsend.ion.server.features.starship.subsystem.balancing

import net.horizonsend.ion.server.configuration.starship.StarshipCommandBurstBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipWeaponBalancing
import net.horizonsend.ion.server.features.starship.subsystem.command_burst.AbstractCommandBurstSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.BalancedWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.SimpleProjectile
import java.util.function.Supplier
import kotlin.reflect.KClass

abstract class StarshipWeaponBalancingManager {
	abstract fun <Z: StarshipWeaponBalancing<*>, T: BalancedWeaponSubsystem<out Z>> getWeapon(clazz: KClass<T>): Z

	abstract fun <Z: StarshipProjectileBalancing, T: SimpleProjectile<Z>> getProjectile(clazz: KClass<T>): Z

	fun <Z: StarshipWeaponBalancing<*>, T: BalancedWeaponSubsystem<Z>> getWeaponSupplier(clazz: KClass<T>): Supplier<Z> {
		return Supplier { getWeapon(clazz) }
	}

	abstract fun <Z: StarshipCommandBurstBalancing, T: AbstractCommandBurstSubsystem<out Z>> getCommandBurst(clazz: KClass<T>): Z

	fun <Z: StarshipCommandBurstBalancing, T: AbstractCommandBurstSubsystem<Z>> getCommandBurstSupplier(clazz: KClass<T>): Supplier<Z> {
		return Supplier { getCommandBurst(clazz) }
	}
}
