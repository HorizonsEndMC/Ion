package net.horizonsend.ion.server.features.starship.subsystem.balancing

import net.horizonsend.ion.server.configuration.starship.StarshipProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.SubsystemBalancing
import net.horizonsend.ion.server.features.starship.subsystem.BalancedSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.SimpleProjectile
import java.util.function.Supplier
import kotlin.reflect.KClass

abstract class StarshipWeaponBalancingManager {
	abstract fun <Z: SubsystemBalancing, T: BalancedSubsystem<out Z>> getSubsystem(clazz: KClass<T>): Z

	abstract fun <Z: StarshipProjectileBalancing, T: SimpleProjectile<Z>> getProjectile(clazz: KClass<T>): Z

	fun <Z: SubsystemBalancing, T: BalancedSubsystem<Z>> getSubsystemSupplier(clazz: KClass<T>): Supplier<Z> {
		return Supplier { getSubsystem(clazz) }
	}
}
