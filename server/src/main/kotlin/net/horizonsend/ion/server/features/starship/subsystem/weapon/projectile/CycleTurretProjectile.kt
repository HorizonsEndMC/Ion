package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.common.extensions.informationAction
import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.server.configuration.starship.CycleTurretBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.CycleTurretMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class CycleTurretProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	override val color: Color,
	shooter: Damager,
	private val shotIndex: Int,
	private val multiblock: CycleTurretMultiblock
) : LaserProjectile<CycleTurretBalancing.CycleTurretProjectileBalancing>(source, name, loc, dir, shooter, DamageType.GENERIC) {
    override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
        super.moveVisually(oldLocation, newLocation, travel)

        if (System.nanoTime() - this.firedAtNanos > shotIndex * TimeUnit.MILLISECONDS.toNanos(balancing.delayMillis.toLong())) {
            this.speed = balancing.speed
        }
    }

	override var speed = balancing.speed

    override fun fire() {
        super.fire()

        this.speed = 1.0
    }

    override fun onImpactStarship(starship: ActiveStarship, impactLocation: Location) {
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
