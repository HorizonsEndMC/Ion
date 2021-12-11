package net.starlegacy.feature.starship.subsystem.weapon.projectile

import net.starlegacy.feature.starship.active.ActiveStarship
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class HeavyLaserProjectile(
    starship: ActiveStarship?,
    loc: Location,
    dir: Vector,
    shooter: Player?,
    originalTarget: Vector,
    baseAimDistance: Int,
    sound: String
) : TrackingLaserProjectile(starship, loc, dir, shooter, originalTarget, baseAimDistance) {
    override val shieldDamageMultiplier = 2
    override val maxDegrees: Double = 25.0
    override val range: Double = 200.0
    override val speed: Double = 50.0
    override val color: Color = Color.RED
    override val thickness: Double = 0.35
    override val particleThickness: Double = 1.0
    override val explosionPower: Float = 12.0f
    override val soundName: String = sound
}
