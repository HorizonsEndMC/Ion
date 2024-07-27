package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.shield.ShieldSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.spherePoints
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector

class LogisticTurretProjectile(
    ship: ActiveStarship?,
    loc: Location,
    dir: Vector,
    override val speed: Double,
    override val color: Color,
    override val range: Double,
    override val particleThickness: Double,
    override val explosionPower: Float,
    override val starshipShieldDamageMultiplier: Double,
    override val areaShieldDamageMultiplier: Double,
    override val soundName: String,
    override val balancing: StarshipWeapons.ProjectileBalancing?,
    shooter: Damager
) : LaserProjectile(ship, loc, dir, shooter) {

    override val volume: Int = (range / 16).toInt()

    override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
        Location(loc.world, x, y, z).spherePoints(1.0, 10).forEach {
            it.world.spawnParticle(
                Particle.VILLAGER_HAPPY,
                it.x,
                it.y,
                it.z,
                1,
                0.25,
                0.25,
                0.25,
                2.0,
                null,
                force
            )
        }
    }

    override fun onImpactStarship(starship: ActiveStarship, impactLocation: Location) {
        if (starship.controller !is AIController) return
        for (shield: ShieldSubsystem in starship.shields) {
            shield.power += balancing?.volume ?: 50000
        }
    }
}