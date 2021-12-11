package net.starlegacy.feature.starship.subsystem.weapon.primary

import net.starlegacy.feature.multiblock.starshipweapon.turret.HeavyTurretMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.TurretWeaponSubsystem
import net.starlegacy.util.Vec3i
import org.bukkit.block.BlockFace

class HeavyTurretWeaponSubsystem(
    ship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace,
    override val multiblock: HeavyTurretMultiblock
) : TurretWeaponSubsystem(ship, pos, face) {
    override val inaccuracyRadians: Double = Math.toRadians(2.5)
    override val powerUsage: Int = 8000
}
