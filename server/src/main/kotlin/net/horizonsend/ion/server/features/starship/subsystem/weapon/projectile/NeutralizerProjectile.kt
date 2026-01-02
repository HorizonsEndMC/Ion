package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.common.utils.miscellaneous.randomInt
import net.horizonsend.ion.server.configuration.starship.NeutralizerBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.NeutralizerStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.NeutralizerWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.circlePoints
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector

class NeutralizerProjectile(
    source: ProjectileSource,
	name: Component,
    loc: Location,
    dir: Vector,
    shooter: Damager,
	private val subsystem: NeutralizerWeaponSubsystem
) : LaserProjectile<NeutralizerBalancing.NeutralizerProjectileBalancing>(source, name, loc, dir, shooter, NeutralizerStarshipWeaponMultiblock.damageType) {
	override val color: Color = Color.ORANGE

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		super.spawnParticle(x, y, z, force)
		for (startPoint in subsystem.getFirePos().toLocation(location.world).circlePoints(2.0, 10, direction)) {
			location.world.spawnParticle(
				Particle.TRAIL,
				startPoint,
				1,
				0.25,
				0.25,
				0.25,
				0.0,
				Particle.Trail(location, color, ((1 - (distance / range)) * 10).toInt() + 10),
				true
			)
		}
	}

    override fun onImpactStarship(starship: ActiveStarship, impactLocation: Location) {
        val shooterStarship = shooter.starship ?: return
		starship.shieldRegenModifier = 0.8
		starship.userErrorAction("Ship shield regeneration disrupted!")

		val task = Tasks.syncRepeatTask(0L, 2L) {
			val endLocation = shooterStarship.centerOfMass.toLocation(shooterStarship.world)

			for (startPoint in impactLocation.circlePoints(5.0, 20, direction)) {
				shooterStarship.world.spawnParticle(
					Particle.TRAIL,
					startPoint,
					1,
					0.5,
					0.5,
					0.5,
					0.0,
					Particle.Trail(endLocation, Color.YELLOW, randomInt(19, 21)),
					true
				)
			}
		}

		Tasks.syncDelay(20L) {
			task.cancel()
		}
    }
}
