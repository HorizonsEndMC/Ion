package net.horizonsend.ion.server.features.starship.subsystem.balancing

import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.starship.StarshipWeaponBalancing
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import kotlin.reflect.KClass

class CustomOverridesManager(private val weaponOverrides: List<StarshipWeaponBalancing<*>>) : StarshipWeaponBalancingManager() {
	private val weaponsRaw = ConfigurationFiles.starshipBalancing().weaponDefaults.weapons
		.associateByTo(mutableMapOf()) { it.clazz }
		.apply { putAll(weaponOverrides.associateBy { it.clazz }) }

	override fun <Z : StarshipWeaponBalancing<*>, T : WeaponSubsystem<Z>> getWeapon(clazz: KClass<T>): Z {
		@Suppress("UNCHECKED_CAST")
		return weaponsRaw[clazz] as Z
	}
}
