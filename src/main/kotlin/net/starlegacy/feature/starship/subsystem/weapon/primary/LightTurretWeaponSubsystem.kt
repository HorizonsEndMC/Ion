package net.starlegacy.feature.starship.subsystem.weapon.primary

import net.starlegacy.feature.multiblock.starshipweapon.turret.LightTurretMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.TurretWeaponSubsystem
import net.starlegacy.util.Vec3i
import org.bukkit.block.BlockFace

class LightTurretWeaponSubsystem(
	ship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace,
	override val multiblock: LightTurretMultiblock
) : TurretWeaponSubsystem(ship, pos, face) {
	override val powerUsage: Int = 6000
	override val inaccuracyRadians: Double = Math.toRadians(2.0)
}
