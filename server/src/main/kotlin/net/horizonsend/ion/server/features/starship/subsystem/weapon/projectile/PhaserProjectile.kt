package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.PhaserBalancing.PhaserProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipSounds.SoundInfo
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.PhaserStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.iterateVector
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.lightning
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.spherePoints
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class PhaserProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager
) : ParticleProjectile<PhaserProjectileBalancing>(source, name, loc, dir, shooter, PhaserStarshipWeaponMultiblock.damageType) {
	override var speed: Double = balancing.speed; get() = balancing.speed

	private val blueParticleData = Particle.DustTransition(
		Color.fromARGB(255, 0, 255, 255),
		Color.WHITE,
		2.0f
	)

	private val generations = 3
	private val maxOffset = 0.5

	companion object {
		val speedUpTime = TimeUnit.MILLISECONDS.toNanos(500L)
		val speedUpSpeed = 1000.0
	}

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		val origin = Location(location.world, x, y, z)

		origin.spherePoints(0.25, 3).forEach {
			it.world.spawnParticle(
				Particle.DUST_COLOR_TRANSITION,
				it.x,
				it.y,
				it.z,
				1,
				0.0,
				0.0,
				0.0,
				0.5,
				blueParticleData,
				force
			)
		}

		origin.spherePoints(0.5, 5).forEach {
			it.world.spawnParticle(Particle.SOUL_FIRE_FLAME, it.x, it.y, it.z, 1, 0.1, 0.1, 0.1, 0.0, null, force)
		}
	}

	override fun impact(newLoc: Location, block: Block?, entity: Entity?) {
		super.impact(newLoc, block, entity)

		val rayEnds = newLoc.spherePoints(3.0, 3)
		for (rayEnd in rayEnds) {
			val lightningPoints = lightning(newLoc, rayEnd, generations, maxOffset, 0.7)
			for (lightningPoint in lightningPoints) {
				lightningPoint.world.spawnParticle(Particle.SOUL_FIRE_FLAME, lightningPoint.x, lightningPoint.y, lightningPoint.z, 1, 0.0, 0.0, 0.0, 0.0, null, true)
			}
		}

		for (point in newLoc.spherePoints(2.5, 5)) {
			newLoc.iterateVector(Vector(point.x - newLoc.x, point.y - newLoc.y, point.z - newLoc.z), 5) { pointAlong, _ ->
				pointAlong.world.spawnParticle(
					Particle.DUST_COLOR_TRANSITION,
					pointAlong.x,
					pointAlong.y,
					pointAlong.z,
					1,
					0.25,
					0.25,
					0.25,
					2.0,
					blueParticleData,
					true
				)
			}
		}

		newLoc.world.spawnParticle(
			Particle.GLOW,
			newLoc.x,
			newLoc.y,
			newLoc.z,
			25,
			0.5,
			0.5,
			0.5,
			0.0,
			null,
			true
		)
	}

	override fun onImpactStarship(starship: ActiveStarship, impactLocation: Location) {
		impactLocation.world.playSound(impactLocation, "minecraft:entity.firework_rocket.twinkle", 12f, 0.5f)
	}

	override fun playCustomSound(loc: Location, nearSound: SoundInfo, farSound: SoundInfo) { /* Do nothing */ }
}
