package net.horizonsend.ion.server.features.starship.subsystem.balancing

import net.horizonsend.ion.server.configuration.StarshipBalancing
import net.horizonsend.ion.server.configuration.StarshipWeapons.StarshipWeaponBalancing
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import net.horizonsend.ion.server.features.starship.type.StarshipType
import kotlin.reflect.KClass

class StarshipTypeBalancing<T: StarshipBalancing>(private val type: StarshipType<T>) : BalancingManager() {
	private val balancingRaw = listOf<StarshipWeaponBalancing<*>>().associateBy(StarshipWeaponBalancing::clazz)

	override fun <Z : StarshipWeaponBalancing<*>, T : WeaponSubsystem<Z>> get(clazz: KClass<T>): Z {
		@Suppress("UNCHECKED_CAST")
		return balancingRaw[clazz] as Z
	}
}
