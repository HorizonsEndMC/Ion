package net.horizonsend.ion.server.features.starship.subsystem.balancing

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import java.util.function.Supplier
import kotlin.reflect.KClass

abstract class BalancingManager() {
	abstract fun <Z: StarshipWeapons.StarshipWeaponBalancing<*>, T: WeaponSubsystem<Z>> get(clazz: KClass<T>): Z

	fun <Z: StarshipWeapons.StarshipWeaponBalancing<*>, T: WeaponSubsystem<Z>> getSupplier(clazz: KClass<T>): Supplier<Z> {
		return Supplier { get(clazz) }
	}

	inline fun <Z: StarshipWeapons.StarshipWeaponBalancing<*>, reified T: WeaponSubsystem<Z>> get(): Z {
		return get(T::class)
	}

	inline fun <Z: StarshipWeapons.StarshipWeaponBalancing<*>, reified T: WeaponSubsystem<Z>> getSupplier(): Supplier<Z> {
		return Supplier { get(T::class) }
	}
}
