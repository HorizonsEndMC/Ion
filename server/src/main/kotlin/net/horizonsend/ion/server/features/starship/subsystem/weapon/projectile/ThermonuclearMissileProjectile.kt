package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.common.utils.miscellaneous.randomInt
import net.horizonsend.ion.server.configuration.starship.StarshipTrackingProjectileBalancing
import net.horizonsend.ion.server.features.client.display.modular.ItemDisplayContainer
import net.horizonsend.ion.server.features.client.display.teleportDuration
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.circlePoints
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.lerp
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.spherePoints
import net.kyori.adventure.text.Component
import org.bukkit.damage.DamageType
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.util.Vector

class ThermonuclearMissileProjectile<B : StarshipTrackingProjectileBalancing>(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	val dir: Vector,
	val initialDir: Vector,
	override val balancing: B,
	shooter: Damager,
	var face: BlockFace, //Up = true, down = false
	originalTarget: Vector,
	baseAimDistance: Int
) : TrackingLaserProjectile<B>(source, name, loc, dir, shooter, originalTarget, baseAimDistance, DamageType.GENERIC) {
	var flightPath1Completed = false
	var flightPath2Completed = false
	var age = 0
	private val explosionParticleData = Particle.DustTransition(
		Color.AQUA,
		Color.WHITE,
		balancing.particleThickness.toFloat()
	)


	val item = ItemFactory.unStackableCustomItem("projectile/activated_thermonuclear_missile").construct()
	override val color: Color = Color.ORANGE

	init {
		track = false
	}

	private val container = ItemDisplayContainer(
		source.getWorld(),
		6.0F,
		loc.toVector(),
		dir,
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

			if (distance > 30) {
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
		container.heading = direction.clone().multiply(-1)
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
		val task = Tasks.syncRepeatTask(0L, 2L) {
			for (point in location.circlePoints(3.0, 10, direction)) {
				point.world.spawnParticle(
					Particle.DUST_COLOR_TRANSITION,
					point.x,
					point.y,
					point.z,
					1,
					0.0,
					0.0,
					0.0,
					2.0,
					explosionParticleData,
					true
				)
			}
			val endLocation = location.toCenterLocation()

			for (startPoint in location.circlePoints(5.0, 20, direction)) {
				location.world.spawnParticle(
					Particle.TRAIL,
					startPoint,
					1,
					0.5,
					0.5,
					0.5,
					0.0,
					Particle.Trail(endLocation, Color.AQUA, randomInt(19, 21)),
					true
				)
			}
		}

		Tasks.syncDelay(40L) {
			task.cancel()
		}

		Tasks.syncDelay(40L) {
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
					Particle.SOUL_FIRE_FLAME,
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
	}
}
