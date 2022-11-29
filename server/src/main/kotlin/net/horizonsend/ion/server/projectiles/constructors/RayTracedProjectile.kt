package net.horizonsend.ion.server.projectiles.constructors

import kotlin.math.pow
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.FluidCollisionMode
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Damageable
import org.bukkit.entity.Flying
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

abstract class RayTracedProjectile : Projectile() {
	abstract var damage: Double
	abstract val damageFalloffMultiplier: Double
	abstract val shouldBypassHitTicks: Boolean
	abstract val speed: Double
	abstract val range: Double

	fun calculateDamage() {
		val distance = ticks * speed

		val newDamage = if (damageFalloffMultiplier == 0.0) { // Falloff of 0 reserved for a linear falloff
			(damage / -range) * (distance - range)
		} else {
			val a = (damageFalloffMultiplier / (damage + 1.0)).pow(1.0 / range)

			((damage + damageFalloffMultiplier) * a.pow(distance)) - damageFalloffMultiplier
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

		if (rayFlyingTraceResult?.hitEntity != null && rayFlyingTraceResult.hitEntity is Flying && rayFlyingTraceResult.hitEntity != shooter) {
			(rayFlyingTraceResult.hitEntity as? Damageable)?.damage(damage, shooter)

			if (!shouldPassThroughEntities) {
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
				if (shouldBypassHitTicks) (entityHit as? LivingEntity)?.noDamageTicks = 0
				(entityHit as? Damageable)?.damage(damage * 1.5, shooter)
				hitLocation.world.spawnParticle(Particle.EXPLOSION_NORMAL, hitLocation, 2)
				hitLocation.world.playSound(hitLocation, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f)
				hitLocation.world.playSound(hitLocation, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f)
				shooter.sendActionBar(MiniMessage.miniMessage().deserialize("<red><bold>Bullseye!"))
				return true
			}
			//no damage ticks is for hitting multiple times in 1 damage tick
			if (shouldBypassHitTicks) (entityHit as? LivingEntity)?.noDamageTicks = 0
			(entityHit as? Damageable)?.damage(damage, shooter)

			if (!shouldPassThroughEntities) {
				return true
			}

			return false
		}
		return false
	}
}