package net.horizonsend.ion.server.features.blasters

import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.configuration.BalancingConfiguration.EnergyWeapon.ProjectileBalancing
import net.horizonsend.ion.server.features.blasters.boundingbox.BoundingBoxManager.getBoundingBox
import net.horizonsend.ion.server.features.blasters.boundingbox.utils.BlockCollisionUtil
import net.kyori.adventure.key.Key.key
import net.kyori.adventure.sound.Sound.Source
import net.kyori.adventure.sound.Sound.sound
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.starlegacy.feature.gear.powerarmor.PowerArmorManager
import net.starlegacy.util.Tasks
import net.starlegacy.util.alongVector
import org.bukkit.*
import org.bukkit.Particle.DustOptions
import org.bukkit.block.Block
import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.checkerframework.checker.units.qual.g
import kotlin.math.pow


class RayTracedParticleProjectile(
	val test: Location,
	val shooter: Entity?,
	val balancing: ProjectileBalancing,
	val particle: Particle,
	val dir: Vector,
	private val explosiveShot: Boolean,
	private val dustOptions: DustOptions?,
	private val soundWhizz: String
) {
	var damage = balancing.damage
	private var ticks: Int = 0

	fun tick(): Boolean {
		val location = test
		if (ticks * (0.10 * balancing.speed.toInt() * 5) > balancing.range) return true // Out of range
		if (!location.isChunkLoaded) return true // Unloaded chunks

		val (hitBlock, hitEntity, hasHeadshot) = fire() ?: return false

		if (hitBlock != null) {
			location.world.playSound(location, "blaster.impact.standard", 1f, 1f)
			location.world.playSound(location, hitBlock.blockSoundGroup.breakSound, SoundCategory.BLOCKS, .5f, 1f)

			if (explosiveShot)	{
				location.world.createExplosion(hitBlock.location, 4.0f)
				location.world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, .5f, 1.4f)
			}

			return true
		}

		if (hitEntity is LivingEntity) {
			if (balancing.shouldBypassHitTicks) hitEntity.noDamageTicks = 0
			if (hitEntity !is Player) damage *= balancing.mobDamageMultiplier

			// Headshots
			if (hasHeadshot) {
				hitEntity.damage(damage * 1.5, shooter)

				location.world.spawnParticle(Particle.CRIT, location, 10)
				shooter?.playSound(sound(key("minecraft:blaster.hitmarker.standard"), Source.PLAYER, 20f, 0.5f))
				shooter?.sendActionBar(text("Headshot!", NamedTextColor.RED))
				if (!balancing.shouldPassThroughEntities) return true
			}

			if (hitEntity.isGliding) {
				if (!PowerArmorManager.glideDisabledPlayers.containsKey(hitEntity.uniqueId)) {
					Tasks.syncDelay(60) { // after 3 seconds
						hitEntity.information("Your rocket boots have rebooted.")
					}
				}

				PowerArmorManager.glideDisabledPlayers[hitEntity.uniqueId] = System.currentTimeMillis() + 3000
				hitEntity.alert("Taking fire! Rocket boots powering down!")
			}

			val distance = shooter!!.location.distance(hitEntity.location)
			val newDamage =
				if (balancing.damageFalloffMultiplier == 0.0) { // Falloff of 0 reserved for a linear falloff
					(damage / -balancing.range) * (distance - balancing.range)
				} else {
					val a = (balancing.damageFalloffMultiplier / (damage + 1.0)).pow(1.0 / balancing.range)
					((damage + balancing.damageFalloffMultiplier) * a.pow(distance)) - balancing.damageFalloffMultiplier
				}

			damage = newDamage
			hitEntity.damage(damage)

			return true
		}

		return false
	}

	private fun getTargetedSolidMaxDistance(v: Vector, start: Location, maxDistance: Double): Double {
		val test = start.clone()
		var previous: Block? = null
		var i = 0.0
		while (i < maxDistance) {
			if (test.block === previous) {
				previous = test.block
				test.add(v)
				i += v.length()
				continue
			}
			if (test.block.type != Material.AIR) {
				if (BlockCollisionUtil.isSolid(test.block, test)) return start.distance(test)
			}
			previous = test.block
			test.add(v)
			i += v.length()
		}
		return maxDistance
	}

	data class HitResult(val hitBlock: Block?, val hitEntity: Entity?, val hasHeadshot: Boolean)

	fun fire(): HitResult? {
		val maxDist: Double = getTargetedSolidMaxDistance(dir, test, balancing.range)

		val dir2: Vector = dir.clone().multiply(0.10)
		val nearby = test.world.getNearbyEntities(
			test.clone().add(dir.clone().multiply(maxDist / 2)), maxDist / 2, maxDist / 2, maxDist / 2
		)

		var distance = 0.0
		while (distance < maxDist) {
			test.add(dir2)
			test.world.spawnParticle(particle, test, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, true)

			for (e in nearby.filter { it != shooter && it != shooter?.vehicle && shooter?.passengers?.contains(it) == false }) {
				val box = getBoundingBox(e)
				if (box!!.intersects(shooter!!, test, e)) {
					return HitResult(
						null, e, if (box.allowsHeadshots()) box.intersectsHead(test, e) else false
					)
				}
			}

			if (BlockCollisionUtil.isSolidAt(test.block, test)) {
				return HitResult(
					test.block, null, false
				)
			}

			distance += 0.10
		}
		return null
	}
}
