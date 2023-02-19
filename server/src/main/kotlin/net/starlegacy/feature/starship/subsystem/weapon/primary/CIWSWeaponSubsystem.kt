package net.starlegacy.feature.starship.subsystem.weapon.primary

import net.starlegacy.feature.multiblock.starshipweapon.turret.CIWSMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.TurretWeaponSubsystem
import net.starlegacy.util.Vec3i
import org.bukkit.block.BlockFace

class CIWSWeaponSubsystem(
	ship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace,
	override val multiblock: CIWSMultiblock
) : TurretWeaponSubsystem(ship, pos, face) {
	override val inaccuracyRadians: Double = 0.0
	override val powerUsage: Int = 0
	override fun getMaxPerShot(): Int? {
		return super.getMaxPerShot()
	}
}
