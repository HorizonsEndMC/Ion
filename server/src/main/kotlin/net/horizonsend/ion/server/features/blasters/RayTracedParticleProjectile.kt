package net.horizonsend.ion.server.features.blasters

import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.configuration.BalancingConfiguration.EnergyWeapon.ProjectileBalancing
import net.horizonsend.ion.server.features.blasters.boundingbox.BoundingBoxManager
import net.horizonsend.ion.server.features.blasters.boundingbox.utils.BlockCollisionUtil
import net.kyori.adventure.key.Key.key
import net.kyori.adventure.sound.Sound.Source
import net.kyori.adventure.sound.Sound.sound
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.starlegacy.feature.gear.powerarmor.PowerArmorManager
import net.starlegacy.feature.space.SpaceWorlds
import net.starlegacy.util.Tasks
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import kotlin.math.pow

class RayTracedParticleProjectile(
	val location: Location,
	val shooter: Entity?,
	val balancing: ProjectileBalancing,
	val particle: Particle,
	private val explosiveShot: Boolean,
	private val dustOptions: DustOptions?,
	private val soundWhizz: String
) {
	var damage = balancing.damage

	private var directionVector = location.direction.clone().multiply(0.10)
	var ticks: Int = 0
	private val hitEntities: MutableList<Entity> = mutableListOf()
	private val nearMissPlayers: MutableList<Player?> = mutableListOf(shooter as? Player)

	fun tick(): Boolean {
		if (ticks * (0.10 * balancing.speed.toInt() * 5) > balancing.range) return true // Out of range
		if (!location.isChunkLoaded) return true // Unloaded chunks

		// Credits: QualityArmory
		val maxDistance = location.world.viewDistance
		val entities = location.world.getNearbyEntities(
			location.clone().add(directionVector.clone().multiply(maxDistance / 2)),
			(maxDistance / 2).toDouble(), (maxDistance / 2).toDouble(), (maxDistance / 2).toDouble()
		).filterNot { it == shooter || it == shooter?.vehicle || shooter?.passengers?.contains(it) ?: false }

		repeat(balancing.speed.toInt() * 5) {
			location.add(directionVector)
			location.world.spawnParticle(particle, location, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, true)

			val hitBlock = BlockCollisionUtil.isSolidAt(location.block, location)
			val hitEntity = entities.find {
				val bb = BoundingBoxManager.getBoundingBox(it) ?: return@find false
				bb.intersects(shooter!!, location, it)
			}

			val entityBB = hitEntity?.let { BoundingBoxManager.getBoundingBox(it) }

			if (hitBlock) {
				location.world.playSound(location, "blaster.impact.standard", 1f, 1f)
				location.world.playSound(location, location.block.blockSoundGroup.breakSound, SoundCategory.BLOCKS, .5f, 1f)

				if (explosiveShot) {
					location.world.createExplosion(location.block.location, 4.0f)
					location.world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, .5f, 1.4f)
				}

				return true
			}

			if (hitEntity != null && hitEntity is Damageable && hitEntity !in hitEntities) {
				var hasHeadshot = false

				if (explosiveShot) {
					location.world.createExplosion(hitEntity.location, balancing.explosionPower)
				}

				if (hitEntity is LivingEntity) {
					if (balancing.shouldBypassHitTicks) hitEntity.noDamageTicks = 0
					if (hitEntity !is Player) damage *= balancing.mobDamageMultiplier

					// Headshots
					if (balancing.shouldHeadshot && entityBB?.intersectsHead(location, hitEntity) == true) {
						hasHeadshot = true
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
				}

				if (!hasHeadshot) {
					hitEntity.damage(damage, shooter)
					shooter?.playSound(sound(key("minecraft:blaster.hitmarker.standard"), Source.PLAYER, 10f, 1f))
					if (!balancing.shouldPassThroughEntities) return true
				}

				hitEntities.add(hitEntity)
			}
		}

		val distance = ticks * balancing.speed

		val newDamage = if (balancing.damageFalloffMultiplier == 0.0) { // Falloff of 0 reserved for a linear falloff
			(damage / -balancing.range) * (distance - balancing.range)
		} else {
			val a = (balancing.damageFalloffMultiplier / (damage + 1.0)).pow(1.0 / balancing.range)
			((damage + balancing.damageFalloffMultiplier) * a.pow(distance)) - balancing.damageFalloffMultiplier
		}

		damage = newDamage

		// Whizz sound
		val whizzDistance = 5
		location.world.players.forEach {
			if ((it !in nearMissPlayers) && (location.distance(it.location) < whizzDistance)) {
				var pitchFactor = 1.0f
				if (SpaceWorlds.contains(it.world)) pitchFactor = 0.5f
				it.playSound(sound(key("minecraft:$soundWhizz"), Source.PLAYER, 1.0f, pitchFactor))
				nearMissPlayers.add(it)
			}
		}

		ticks += 1

		return false
	}
}
