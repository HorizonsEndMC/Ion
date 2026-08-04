package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.server.configuration.starship.EMPMissileBalancing
import net.horizonsend.ion.server.features.client.display.modular.ItemDisplayContainer
import net.horizonsend.ion.server.features.client.display.teleportDuration
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.status_effects.StarshipStatusEffect
import net.horizonsend.ion.server.features.starship.status_effects.StarshipStatusEffectTypes
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.circlePoints
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.lerp
import net.kyori.adventure.text.Component
import org.bukkit.damage.DamageType
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class EMPMissileProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	val dir: Vector,
	val initialDir: Vector,
	shooter: Damager,
	var face: BlockFace, //Up = true, down = false
	originalTarget: Vector,
	baseAimDistance: Int
) : TrackingLaserProjectile<EMPMissileBalancing.EMPMissileProjectileBalancing>(source, name, loc, dir, shooter, originalTarget, baseAimDistance, DamageType.GENERIC) {
	var flightPath1Completed = false
	var flightPath2Completed = false
	var age = 0

	val item = ItemFactory.unStackableCustomItem("projectile/activated_emp_missile").construct()
	override val color: Color = Color.ORANGE

	init {
		track = false
	}

	private val container = ItemDisplayContainer(
		source.getWorld(),
		1.0F,
		loc.toVector(),
		dir.clone().multiply(-1),
		item,
		interpolation = 2
	).apply {
		getEntity().transformationInterpolationDuration = 2
		getEntity().teleportDuration = 2
	}

	override fun tick() {

		if (!flightPath1Completed) {
			// Initial launch - fly straight out of the multiblock
			direction =

				initialDir.clone().multiply(1) // make the projectile launch parallel from the launcher, and slower

			if (distance > 10) {
				distance = 0.0
				flightPath1Completed = true
				track = true
			}

			super.tick()
			return

		} else if (!flightPath2Completed) {

			// Transitioning to free flight - linearly interpolate between initial launch and free flight
			direction = direction.clone().lerp(dir, 0.2)

			super.tick()
			return
		}

		// Free flight
		direction = dir

	}

	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		container.position = location.toVector()
		container.heading = direction.clone().multiply(1)
		container.update()

		/*for (lineLoc in oldLocation.alongVector(newLocation.toVector().subtract(oldLocation.toVector()), 5)) {
            lineLoc.world.spawnParticle(Particle.DUST, lineLoc.x, lineLoc.y, lineLoc.z, 1, 0.0, 0.0, 0.0, 0.0, Particle.DustOptions(color, 2f), true)
        }*/

		(0 until 2).forEach { _ ->
			val angle = Math.PI / 24
			val opposite = direction.clone().multiply(-1)
				.rotateAroundX(randomDouble(-angle, angle))
				.rotateAroundY(randomDouble(-angle, angle))
				.rotateAroundZ(randomDouble(-angle, angle))
			location.world.spawnParticle(
				Particle.LARGE_SMOKE,
				location,
				0,
				opposite.x,
				opposite.y,
				opposite.z,
				1.0,
				null,
				true
			)
			location.world.spawnParticle(
				Particle.FLAME,
				location,
				0,
				opposite.x,
				opposite.y,
				opposite.z,
				0.25,
				null,
				true
			)
		}
	}

	override fun onDespawn() {
		container.remove()
	}

	private fun lerpAmount(distance: Double): Double {
		val distanceRatio = distance / 67
		return when {
			distanceRatio <= 0.0 -> 1.0 // projectile is basically inside the target
			distanceRatio >= 1.0 -> 0.0 // projectile is further than the proximity range
			else -> /*1.00262 * 0.0511657.pow(distance / proximityRange)*/ 1 - distanceRatio
		}
	}

	override fun onImpact() {
		for (loc in location.circlePoints(2.0, 30, direction)) {
			val radialVector = loc.toVector().subtract(location.toVector()).normalize()
			loc.world.spawnParticle(
				Particle.SOUL_FIRE_FLAME,
				location,
				0,
				radialVector.x,
				radialVector.y,
				radialVector.z,
				1.0,
				null,
				true
			)
		}

		(0 until 20).forEach { _ ->
			val angle = Math.PI / 12
			val opposite = direction.clone().multiply(-1)
				.rotateAroundX(randomDouble(-angle, angle))
				.rotateAroundY(randomDouble(-angle, angle))
				.rotateAroundZ(randomDouble(-angle, angle))
			location.world.spawnParticle(
				Particle.SOUL_FIRE_FLAME,
				location,
				0,
				opposite.x,
				opposite.y,
				opposite.z,
				1.0,
				null,
				true
			)
		}

		(0 until 40).forEach { _ ->
			val angle = Math.PI / 6
			val opposite = direction.clone().multiply(-1)
				.rotateAroundX(randomDouble(-angle, angle))
				.rotateAroundY(randomDouble(-angle, angle))
				.rotateAroundZ(randomDouble(-angle, angle))
			location.world.spawnParticle(
				Particle.LARGE_SMOKE,
				location,
				0,
				opposite.x,
				opposite.y,
				opposite.z,
				2.0,
				null,
				true
			)
		}
	}

	override fun onImpactStarship(starship: ActiveStarship, impactLocation: Location) {
		if (starship.initialBlockCount > 4500) return
		val shieldPenalty = balancing.effectStrength

		starship.addStatusEffect(
			StarshipStatusEffect(
				StarshipStatusEffectTypes.SHIELD_WEAKNESS,
				shieldPenalty,
				balancing.effectDurationMillis,
				shooter.starship
			)
		)
		starship.userErrorAction("Shields weakened by ${(shieldPenalty * 100).toInt()}%!")
	}
}

