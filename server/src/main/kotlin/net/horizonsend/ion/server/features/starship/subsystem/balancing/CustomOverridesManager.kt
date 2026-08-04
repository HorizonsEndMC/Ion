package net.horizonsend.ion.server.features.starship.subsystem.balancing

import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.starship.StarshipCommandBurstBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipWeaponBalancing
import net.horizonsend.ion.server.features.starship.subsystem.command_burst.AbstractCommandBurstSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.BalancedWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.SimpleProjectile
import kotlin.reflect.KClass

class CustomOverridesManager(private val weaponOverrides: List<StarshipWeaponBalancing<*>>, private val commandBurstOverrides: List<StarshipCommandBurstBalancing>) : StarshipWeaponBalancingManager() {
	private val weaponsRaw = ConfigurationFiles.starshipBalancing().weaponDefaults.weapons
		.associateByTo(mutableMapOf()) { it.clazz }
		.apply { putAll(weaponOverrides.associateBy { it.clazz }) }

	val projectiles = ConfigurationFiles.starshipBalancing().weaponDefaults.weapons.map { balancing -> balancing.projectile }
		.associateByTo(mutableMapOf()) { it.clazz }
		.apply { putAll(weaponOverrides.map { balancing -> balancing.projectile }.associateBy { it.clazz }) }

	val commandBursts = ConfigurationFiles.starshipBalancing().commandBurstDefaults.commandBursts
		.associateByTo(mutableMapOf()) { it.clazz }
		.apply { putAll(commandBurstOverrides.associateBy { it.clazz }) }

	override fun <Z : StarshipWeaponBalancing<*>, T : BalancedWeaponSubsystem<out Z>> getWeapon(clazz: KClass<T>): Z {
		@Suppress("UNCHECKED_CAST")
		return weaponsRaw[clazz] as Z
	}

	override fun <Z : StarshipProjectileBalancing, T : SimpleProjectile<Z>> getProjectile(clazz: KClass<T>): Z {
		@Suppress("UNCHECKED_CAST")
		return projectiles[clazz] as Z
	}

	override fun <Z : StarshipCommandBurstBalancing, T : AbstractCommandBurstSubsystem<out Z>> getCommandBurst(clazz: KClass<T>): Z {
		@Suppress("UNCHECKED_CAST")
		return commandBursts[clazz] as Z
	}
}
