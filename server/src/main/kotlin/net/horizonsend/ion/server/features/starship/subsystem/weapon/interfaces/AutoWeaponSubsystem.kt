package net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces

import net.horizonsend.ion.server.configuration.StarshipWeapons.StarshipAutoWeaponBalancing
import net.horizonsend.ion.server.features.starship.AutoTurretTargeting.AutoTurretTarget
import org.bukkit.entity.Player
import org.bukkit.util.Vector

interface AutoWeaponSubsystem {
	val balancing: StarshipAutoWeaponBalancing<*>
	val range: Double

	fun autoFire(target: AutoTurretTarget<*>, dir: Vector)

	fun shouldTargetRandomBlock(target: Player): Boolean = true
}
