package net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces

import net.horizonsend.ion.server.features.starship.AutoTurretTargeting.AutoTurretTarget
import net.horizonsend.ion.server.features.starship.damager.Damager
import org.bukkit.entity.Player
import org.bukkit.util.Vector

interface AutoWeaponSubsystem {
	val range: Double

	fun autoFire(target: AutoTurretTarget<*>, dir: Vector)

	fun shouldTargetRandomBlock(target: Player): Boolean = true
}
