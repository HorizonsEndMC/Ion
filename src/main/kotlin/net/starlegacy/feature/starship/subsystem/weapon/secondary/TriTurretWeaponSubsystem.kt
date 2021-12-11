package net.starlegacy.feature.starship.subsystem.weapon.secondary

import net.starlegacy.feature.multiblock.starshipweapon.turret.TriTurretMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.TurretWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.starlegacy.util.Vec3i
import org.bukkit.block.BlockFace
import org.litote.kmongo.mul

class TriTurretWeaponSubsystem(
    ship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace,
    override val multiblock: TriTurretMultiblock
) : TurretWeaponSubsystem(ship, pos, face),
    HeavyWeaponSubsystem {
    override val inaccuracyRadians: Double = Math.toRadians(3.0)
    override val powerUsage: Int = 45_000
    override val boostChargeNanos: Long = multiblock.cooldownNanos
}
