package net.horizonsend.ion.server.projectiles

import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity
import org.bukkit.entity.Flying
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

class RayTracedParticleProjectile(
	override val location: Location,
	override val iterationsPerTick: Int,
	override val distancePerIteration: Double,
	override val shooter: Entity,
	override val damage: Double,
	override val shouldPassThroughEntities: Boolean,
	override val size: Double,
	override val shouldBypassHitTicks: Boolean,
	val particle: Particle,
	val dustOptions: DustOptions?
): Projectile() {
	private var directionVector = location.direction.clone().multiply(distancePerIteration)

	override fun tick(): Boolean {
		/**
		 * Every tick, this function will repeat the code below
		 * for however many iterationsPerTick there are, which is
		 * It will delete the projecile if it goes out of loaded chunks
		 */
		repeat(iterationsPerTick) {
			location.add(directionVector)

			if (!location.isChunkLoaded) return true
			if (dustOptions != null) {
				location.world.spawnParticle(particle, location, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, true)
			} else location.world.spawnParticle(particle, location, 1, 0.0, 0.0, 0.0, 0.0, null, true)

		}

		return false
	}

	fun rayCastTick(): Boolean {
		val rayTraceResult = location.world.rayTrace(
			location,
			location.direction,
			location.world.viewDistance.toDouble(),
			FluidCollisionMode.NEVER,
			true,
			size,
			null
		)

		val rayFlyingTraceResult = location.world.rayTrace(
			location,
			location.direction,
			location.world.viewDistance.toDouble(),
			FluidCollisionMode.NEVER,
			true,
			2.0,
			null
		)

		if (rayTraceResult?.hitBlock != null || rayFlyingTraceResult?.hitBlock != null) {
			rayTraceResult?.hitBlock?.blockSoundGroup?.breakSound?.let {
				location.world.playSound(location,
					it, 0.5f, 1f)
			}

			return true
		}

		if (rayFlyingTraceResult?.hitEntity != null && rayFlyingTraceResult.hitEntity is Flying) {
			(rayFlyingTraceResult.hitEntity as? Damageable)?.damage(damage, shooter)

			if (!shouldPassThroughEntities) {
				return true
			}

			return false
		}

		if (rayTraceResult?.hitEntity != null) {
			val playerHit = rayTraceResult.hitEntity
			val hitLocation = rayTraceResult.hitPosition.toLocation(rayTraceResult.hitEntity!!.world)

			val rayHitPosition = rayTraceResult.hitPosition
			val playerEye = (playerHit as? Player)?.eyeLocation?.toVector()

			if (playerEye != null && rayHitPosition.distance(playerEye) < 0.5) {
				if (shouldBypassHitTicks) (rayTraceResult.hitEntity as? LivingEntity)?.noDamageTicks = 0
				(rayTraceResult.hitEntity as? Damageable)?.damage(damage * 1.5, shooter)
				hitLocation.createExplosion(0.01f)
				hitLocation.world.playSound(hitLocation, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f)
				return true
			}

			if (shouldBypassHitTicks) (rayTraceResult.hitEntity as? LivingEntity)?.noDamageTicks = 0
			(rayTraceResult.hitEntity as? Damageable)?.damage(damage, shooter)

			if (!shouldPassThroughEntities) {
				return true
			}

			return false
		}
		return false
	}
}