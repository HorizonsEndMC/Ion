package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.util.Vector

class CycleTurretProjectile(
    ship: ActiveStarship?,
    loc: Location,
    dir: Vector,
    override var speed: Double,
    override val color: Color,
    override val range: Double,
    override val particleThickness: Double,
    override val explosionPower: Float,
    override val starshipShieldDamageMultiplier: Double,
    override val areaShieldDamageMultiplier: Double,
    override val soundName: String,
    override val balancing: StarshipWeapons.ProjectileBalancing = ship?.balancing?.weapons?.cycleTurret ?: IonServer.starshipBalancing.nonStarshipFired.cycleTurret,
    shooter: Damager,
    private val delayMillis: Long
) : LaserProjectile(ship, loc, dir, shooter) {

    override val volume: Int = (range / 16).toInt()

    override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
        super.moveVisually(oldLocation, newLocation, travel)

        if (System.nanoTime() - this.firedAtNanos > delayMillis) {
            this.speed = balancing.speed
        }
    }

    override fun fire() {
        super.fire()

        this.speed = 1.0
    }
}