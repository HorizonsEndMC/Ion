package net.starlegacy.feature.starship.subsystem.weapon.primary

import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.CannonWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.projectile.CannonLaserProjectile
import net.starlegacy.util.Vec3i
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class LaserCannonWeaponSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace) :
    CannonWeaponSubsystem(starship, pos, face) {
    override val powerUsage: Int = 1600
    override val length: Int = 2
    override val angleRadians: Double = Math.toRadians(15.0)
    override val convergeDist: Double = 20.0

    override fun fire(loc: Location, dir: Vector, shooter: Player, target: Vector?) {
        CannonLaserProjectile(starship, loc, dir, shooter).fire()
    }

    override val extraDistance: Int = 2
}
