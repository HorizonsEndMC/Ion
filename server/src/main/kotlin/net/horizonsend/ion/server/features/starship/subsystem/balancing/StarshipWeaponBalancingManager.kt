package net.horizonsend.ion.server.features.starship.subsystem.balancing

import net.horizonsend.ion.server.configuration.starship.StarshipWeaponBalancing
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import java.util.function.Supplier
import kotlin.reflect.KClass

abstract class StarshipWeaponBalancingManager {
	abstract fun <Z: StarshipWeaponBalancing<*>, T: WeaponSubsystem<Z>> getWeapon(clazz: KClass<T>): Z

	fun <Z: StarshipWeaponBalancing<*>, T: WeaponSubsystem<Z>> getWeaponSupplier(clazz: KClass<T>): Supplier<Z> {
		return Supplier { getWeapon(clazz) }
	}

	inline fun <Z: StarshipWeaponBalancing<*>, reified T: WeaponSubsystem<Z>> getWeapon(): Z {
		return getWeapon(T::class)
	}

	inline fun <Z: StarshipWeaponBalancing<*>, reified T: WeaponSubsystem<Z>> getWeaponSupplier(): Supplier<Z> {
		return Supplier { getWeapon(T::class) }
	}
}
