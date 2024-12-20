package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.common.extensions.informationAction
import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.CycleTurretMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class CycleTurretProjectile(
	ship: ActiveStarship?,
	name: Component,
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
	override val balancing: StarshipWeapons.ProjectileBalancing = ship?.balancing?.weapons?.cycleTurret ?: ConfigurationFiles.starshipBalancing().nonStarshipFired.cycleTurret,
	shooter: Damager,
	private val shotIndex: Int,
	private val multiblock: CycleTurretMultiblock
) : LaserProjectile(ship, name, loc, dir, shooter) {

    override val volume: Int = (range / 16).toInt()

    override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
        super.moveVisually(oldLocation, newLocation, travel)

        if (System.nanoTime() - this.firedAtNanos > shotIndex * TimeUnit.MILLISECONDS.toNanos(balancing.delayMillis!!.toLong())) {
            this.speed = balancing.speed
        }
    }

    override fun fire() {
        super.fire()

        this.speed = 1.0
    }

    override fun onImpactStarship(starship: ActiveStarship, impactLocation: Location) {
        if (starship is ActiveControlledStarship) {
            multiblock.slowedStarships[starship] = System.currentTimeMillis() + 3000
            starship.userErrorAction("Direct Control speed slowed by 80%!")
            starship.directControlCooldown = starship.initialDirectControlCooldown * 5

            Tasks.syncDelay(3 * 20L) {
                val unslowTime = multiblock.slowedStarships[starship]?.minus(100) ?: 0 // 100 buffer
                if (ActiveStarships.isActive(starship) && unslowTime < System.currentTimeMillis()) {
                    multiblock.slowedStarships[starship]?.let { multiblock.slowedStarships.remove(starship) }
                    starship.directControlCooldown = starship.initialDirectControlCooldown
                    starship.informationAction("Direct Control speed restored")
                }
            }
        }
    }
}
