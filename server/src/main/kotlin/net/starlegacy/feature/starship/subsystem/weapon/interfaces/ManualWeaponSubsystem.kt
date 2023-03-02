package net.starlegacy.feature.starship.subsystem.weapon.interfaces

import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.util.Vector

interface ManualWeaponSubsystem {
	/**
	 * Does the actual firing of the weapon, does not use power or check for collision
	 */
	fun manualFire(shooter: Player, dir: Vector, target: Vector)

	fun isAcceptableDirection(face: BlockFace) = true
}
