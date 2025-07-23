package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.StarshipSounds
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.PhaserStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
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

class PhaserProjectile(
	starship: ActiveStarship?,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager
) : ParticleProjectile(starship, name, loc, dir, shooter, PhaserStarshipWeaponMultiblock.damageType) {
	override val balancing: StarshipWeapons.ProjectileBalancing = starship?.balancing?.weapons?.phaser ?: ConfigurationFiles.starshipBalancing().nonStarshipFired.phaser
	override val range: Double = balancing.range
	override var speed: Double = balancing.speed
	override val starshipShieldDamageMultiplier = balancing.starshipShieldDamageMultiplier
	override val areaShieldDamageMultiplier: Double = balancing.areaShieldDamageMultiplier
	override val explosionPower: Float = balancing.explosionPower
	override val volume: Int = balancing.volume
	override val pitch: Float = balancing.pitch
	override val soundName: String = balancing.soundName
	override val nearSound: StarshipSounds.SoundInfo = balancing.soundFireNear
	override val farSound: StarshipSounds.SoundInfo = balancing.soundFireFar

	private val blueParticleData = Particle.DustTransition(
		Color.fromARGB(255, 0, 255, 255),
		Color.WHITE,
		2.0f
	)

	private val generations = 3
	private val maxOffset = 0.5

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		val origin = Location(loc.world, x, y, z)

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

		val rayEnds = newLoc.spherePoints(1.0, 2)
		for (rayEnd in rayEnds) {
			val lightningPoints = lightning(newLoc, rayEnd, 3, 0.5, 0.7)
			for (lightningPoint in lightningPoints) {
				lightningPoint.world.spawnParticle(Particle.SOUL_FIRE_FLAME, lightningPoint.x, lightningPoint.y, lightningPoint.z, 1, 0.0, 0.0, 0.0, 0.0, null, true)
			}
		}

		for (point in newLoc.spherePoints(1.5, 5)) {
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

	override fun playCustomSound(loc: Location, nearSound: StarshipSounds.SoundInfo, farSound: StarshipSounds.SoundInfo) { /* Do nothing */ }
}
