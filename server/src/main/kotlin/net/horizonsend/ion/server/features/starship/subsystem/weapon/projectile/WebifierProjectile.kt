package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.WebifierBalancing.WebifierProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipSounds.SoundInfo
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.alongVector
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.circlePoints
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector

class WebifierProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager
) : ParticleProjectile<WebifierProjectileBalancing>(source, name, loc, dir, shooter, DamageType.GENERIC) {
	override var speed: Double = balancing.speed; get() = balancing.speed
	var tick: Int = -1
	val firePos = loc

	private val blueParticleData = Particle.DustTransition(
		Color.AQUA,
		Color.PURPLE,
		2.0f
	)



	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		val origin = Location(location.world, x, y, z)
			//for (point in origin.circlePoints(0.3, 12, direction)) {
			location.world.spawnParticle(
				Particle.DUST_COLOR_TRANSITION,
				origin.x,
				origin.y,
				origin.z,
				1,
				0.0,
				0.0,
				0.0,
				0.0,
				blueParticleData,
				force
			)
				//}
	}


/*	override fun impact(newLoc: Location, block: Block?, entity: Entity?) {
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
	} */

	override fun onImpactStarship(starship: ActiveStarship, impactLocation: Location) {
		super.onImpactStarship(starship, impactLocation)
	}

	override fun playCustomSound(loc: Location, nearSound: SoundInfo, farSound: SoundInfo) { /* Do nothing */ }
}
