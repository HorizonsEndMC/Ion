package net.horizonsend.ion.server.projectiles

import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity
import org.bukkit.entity.Flying
import org.bukkit.entity.Player

class Projectile(
	private val location: Location,
	private val dustOptions: DustOptions?,
	private val iterationsPerTick: Int,
	distancePerIteration: Double,
	val particle: Particle,
	val shooter: Entity,
	val damage: Double,
	private val shouldPassThroughEntities: Boolean,
	private val size: Double
) {
	private var directionVector = location.direction.multiply(distancePerIteration)

	fun tick(): Boolean {
		repeat(iterationsPerTick) {
			location.add(directionVector)
			if (!location.isChunkLoaded) return true
			if (dustOptions != null) {
				location.world.spawnParticle(particle, location, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, true)
			} else location.world.spawnParticle(particle, location, 1, 0.0, 0.0, 0.0, 0.0, null, true)

		}
		return false
	}

	fun rayCastTick() : Boolean {
		val rayTraceResult = location.world.rayTrace(location, location.direction, location.world.viewDistance.toDouble() , FluidCollisionMode.NEVER, true, size, null)
		val rayFlyingTraceResult = location.world.rayTrace(location, location.direction, location.world.viewDistance.toDouble() , FluidCollisionMode.NEVER, true, 2.0, null)
		if (rayTraceResult?.hitBlock != null || rayFlyingTraceResult?.hitBlock != null){
			location.world.playSound(location, rayTraceResult?.hitBlock!!.blockSoundGroup.breakSound, 0.5f, 1f)
			return true
		}
		if (rayFlyingTraceResult?.hitEntity != null && rayFlyingTraceResult.hitEntity is Flying){
			(rayFlyingTraceResult.hitEntity as? Damageable)?.damage(damage, shooter)
			if (!shouldPassThroughEntities){
				return true
			}
			return false
		}
		if (rayTraceResult?.hitEntity != null) {
			val playerHit = rayTraceResult.hitEntity
			val hitLocation = rayTraceResult.hitPosition.toLocation(rayTraceResult.hitEntity!!.world)

			val raycast = rayTraceResult.hitPosition
			val playereye = (playerHit as? Player)?.eyeLocation?.toVector()
			if (playereye != null && raycast.distance(playereye) < 0.5){
					(rayTraceResult.hitEntity as? Damageable)?.damage(damage * 1.5, shooter)
					hitLocation.createExplosion(0.01f)
					hitLocation.world.playSound(hitLocation, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f)
					return true
			}
			(rayTraceResult.hitEntity as? Damageable)?.damage(damage, shooter)
			if (!shouldPassThroughEntities){
				return true
			}
			return false
		}
		return false
	}
}