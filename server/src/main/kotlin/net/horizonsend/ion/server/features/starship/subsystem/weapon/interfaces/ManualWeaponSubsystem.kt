package net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces

import net.horizonsend.ion.server.features.starship.controllers.Controller
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

interface ManualWeaponSubsystem {
	/**
	 * Does the actualStyle firing of the weapon, does not use power or check for collision
	 */
	fun manualFire(shooter: Controller, dir: Vector, target: Vector)

	fun isAcceptableDirection(face: BlockFace) = true
}
