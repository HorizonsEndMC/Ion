package net.horizonsend.ion.server.projectiles

import net.horizonsend.ion.server.BalancingConfiguration.EnergyWeapon.ProjectileBalancing
import net.kyori.adventure.text.minimessage.MiniMessage
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
import kotlin.math.pow

class RayTracedParticleProjectile(
	val location: Location,
	val shooter: Entity,
	val balancing: ProjectileBalancing,
	val particle: Particle,
	private val dustOptions: DustOptions?,
) {
	var damage = balancing.damage

	private var directionVector = location.direction.clone().multiply(balancing.speed)
	var ticks: Int = 0

	fun tick(): Boolean {
		/**
		 * Every tick, this function will repeat the code below.
		 * If it returns true, the projectile manager will delete the projectile.
		 *
		 * True if:
		 * Distance > Range
		 * Location is unloaded
		 * Projectile collides
		 **/

		ticks += 1

		if (ticks * balancing.speed > balancing.range) return true

		location.add(directionVector)

		if (!location.isChunkLoaded) return true

		if (dustOptions != null) {
			location.world.spawnParticle(particle, location, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, true)
		} else location.world.spawnParticle(particle, location, 1, 0.0, 0.0, 0.0, 0.0, null, true)

		if (rayCastTick()) return true

		calculateDamage()

		return false
	}

	fun calculateDamage() {
		val distance = ticks * balancing.speed

		val newDamage = if (balancing.damageFalloffMultiplier == 0.0) { // Falloff of 0 reserved for a linear falloff
			(damage / -balancing.range) * (distance - balancing.range)
		} else {
			val a = (balancing.damageFalloffMultiplier / (damage + 1.0)).pow(1.0 / balancing.range)

			((damage + balancing.damageFalloffMultiplier) * a.pow(distance)) - balancing.damageFalloffMultiplier
		}

		damage = newDamage
	}

	fun rayCastTick(): Boolean {
		val rayTraceResult = location.world.rayTrace(
			location,
			location.direction,
			location.world.viewDistance.toDouble(),
			FluidCollisionMode.NEVER,
			true,
			balancing.shotSize,
			null
		)

		val rayFlyingTraceResult = location.world.rayTrace(
			location,
			location.direction.normalize().multiply(balancing.speed),
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

		if (rayFlyingTraceResult?.hitEntity != null && rayFlyingTraceResult.hitEntity is Flying && rayFlyingTraceResult.hitEntity != shooter) {
			(rayFlyingTraceResult.hitEntity as? Damageable)?.damage(damage, shooter)

			if (!balancing.shouldPassThroughEntities) {
				return true
			}

			return false
		}

		if (rayTraceResult?.hitEntity != null && rayTraceResult.hitEntity != shooter) {
			val entityHit = rayTraceResult.hitEntity
			val hitLocation = rayTraceResult.hitPosition.toLocation(entityHit!!.world)

			val rayHitPosition = rayTraceResult.hitPosition
			val playerEye = (entityHit as? Player)?.eyeLocation?.toVector()
			/**
			 * This code is for headshots, it only works on players for now, as I couldnt be bothered to figure out
			 * entity.location's location relative to the body
			 */
			if (playerEye != null && (playerEye.y-rayHitPosition.y) < 0.3) {
				if (balancing.shouldBypassHitTicks) (entityHit as? LivingEntity)?.noDamageTicks = -1
				(entityHit as? Damageable)?.damage(damage * 1.5, shooter)
				hitLocation.world.spawnParticle(Particle.EXPLOSION_NORMAL, hitLocation, 2)
				hitLocation.world.playSound(hitLocation, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f)
				hitLocation.world.playSound(hitLocation, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f)
				shooter.sendActionBar(MiniMessage.miniMessage().deserialize("<red><bold>Bullseye!"))
				return true
			}
			//no damage ticks is for hitting multiple times in 1 damage tick
			if (balancing.shouldBypassHitTicks) (entityHit as? LivingEntity)?.noDamageTicks = 0
			(entityHit as? Damageable)?.damage(damage, shooter)

			if (!balancing.shouldPassThroughEntities) {
				return true
			}

			return false
		}
		return false
	}
}
