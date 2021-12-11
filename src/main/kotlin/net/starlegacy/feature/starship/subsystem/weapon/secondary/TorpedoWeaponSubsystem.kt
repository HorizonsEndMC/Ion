package net.starlegacy.feature.starship.subsystem.weapon.secondary

import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.TargetTrackingCannonWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.projectile.TorpedoProjectile
import net.starlegacy.util.Vec3i
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class TorpedoWeaponSubsystem(
    starship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace
) : TargetTrackingCannonWeaponSubsystem(starship, pos, face),
    HeavyWeaponSubsystem {
    override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(10L)

    override fun isForwardOnly(): Boolean = true

    override val length: Int = 3
    override val powerUsage: Int get() = 10000
    override val extraDistance: Int = 1
    override val aimDistance: Int = 3

    override fun getMaxPerShot() = 2

    override fun fire(loc: Location, dir: Vector, shooter: Player, target: Vector?) {
        TorpedoProjectile(starship, loc, dir, shooter, checkNotNull(target), aimDistance).fire()
    }
}
