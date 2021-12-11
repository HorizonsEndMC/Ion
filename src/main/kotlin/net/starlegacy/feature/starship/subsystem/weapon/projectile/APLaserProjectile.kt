package net.starlegacy.feature.starship.subsystem.weapon.projectile

import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.util.mcName
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class APLaserProjectile(
    starship: ActiveStarship?,
    loc: Location,
    dir: Vector,
    override val range: Double,
    shooter: Player?
) : LaserProjectile(starship, loc, dir, shooter) {
    override val speed: Double = 500.0
    override val shieldDamageMultiplier: Int = 1
    override val color: Color = Color.ORANGE
    override val thickness: Double = 0.2
    override val particleThickness: Double = 0.35
    override val explosionPower: Float = 0.0f
    override val volume: Int = 5
    override val pitch: Float = 2.0f
    override val soundName: String = Sound.BLOCK_CONDUIT_DEACTIVATE.mcName
}
