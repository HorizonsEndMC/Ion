package net.horizonsend.ion.server.features.custom.items.blasters

import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.ProjectileBalancing
import net.horizonsend.ion.server.features.gear.powerarmor.PowerArmorManager
import net.horizonsend.ion.server.features.space.SpaceWorlds
import net.horizonsend.ion.server.features.starship.damager.addToDamagers
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.alongVector
import net.horizonsend.ion.server.miscellaneous.utils.get
import net.kyori.adventure.key.Key.key
import net.kyori.adventure.sound.Sound.Source
import net.kyori.adventure.sound.Sound.sound
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.pow
import kotlin.math.roundToInt

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

	private var directionVector = location.direction.clone().multiply(balancing.speed)
	var ticks: Int = 0
	private val hitEntities: MutableList<Entity> = mutableListOf()
	private val nearMissPlayers: MutableList<Player?> = mutableListOf(shooter as? Player)

	fun fire() {
		object : BukkitRunnable() {
			override fun run() {
				if (tick())
					cancel()
			}
		}.runTaskTimer(IonServer, 0L, 1L)
	}

	fun tick(): Boolean {
		if (ticks * balancing.speed > balancing.range) return true // Out of range
		if (!location.isChunkLoaded) return true // Unloaded chunks

		for (loc in location.alongVector(directionVector, balancing.speed.roundToInt())) {
			location.world.spawnParticle(particle, loc, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, true)
		}

		// 2 ray traces are used, one for flying, one for ground
		val rayTraceResult = location.world.rayTrace(
			location,
			location.direction.clone().multiply(balancing.speed).normalize(),
			location.world.viewDistance.toDouble(),
			FluidCollisionMode.NEVER,
			true,
			balancing.shotSize
		) { it != shooter && (it as? Player)?.isGliding != true }

		val flyingRayTraceResult = location.world.rayTrace(
			location,
			location.direction.clone().multiply(balancing.speed),
			location.world.viewDistance.toDouble(),
			FluidCollisionMode.NEVER,
			true,
			balancing.shotSize * 2
		) { it != shooter && (it as? Player)?.isGliding == true }

		// Block Check
		val hitBlock = rayTraceResult?.hitBlock
		if (hitBlock != null) {
			location.world.playSound(location, "horizonsend:blaster.impact.standard", 1f, 1f)
			location.world.playSound(location, hitBlock.blockSoundGroup.breakSound, SoundCategory.BLOCKS, .5f, 1f)

			if (explosiveShot)	{
				location.world.createExplosion(hitBlock.location, 4.0f)
				location.world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, .5f, 1.4f)

				if (shooter != null) {
					addToDamagers(
						location.world,
						hitBlock,
						shooter
					)
				}
			}

			return true
		}

		// Entity Check
		val hitEntity = rayTraceResult?.hitEntity
		if (hitEntity != null && hitEntity is Damageable && hitEntity !in hitEntities) {
			val hitLocation = rayTraceResult.hitPosition.toLocation(hitEntity.world)
			val hitPosition = rayTraceResult.hitPosition
			var hasHeadshot = false

			if (explosiveShot) {
				location.world.createExplosion(hitEntity.location, balancing.explosionPower)
			}

			if (hitEntity is LivingEntity) {
				if (balancing.shouldBypassHitTicks) hitEntity.noDamageTicks = 0
				if (hitEntity !is Player) damage *= balancing.mobDamageMultiplier

				// Headshots
				if (balancing.shouldHeadshot && (hitEntity.eyeLocation.y - hitPosition.y) < (.3 * balancing.shotSize)) {
					hasHeadshot = true
					hitEntity.damage(damage * 1.5, shooter)

					hitLocation.world.spawnParticle(Particle.CRIT, hitLocation, 10)
					shooter?.playSound(sound(key("horizonsend:blaster.hitmarker.standard"), Source.PLAYER, 20f, 0.5f))
					shooter?.sendActionBar(text("Headshot!", NamedTextColor.RED))
					if (!balancing.shouldPassThroughEntities) return true
				}
			}

			if (!hasHeadshot) {
				hitEntity.damage(damage, shooter)
				shooter?.playSound(sound(key("horizonsend:blaster.hitmarker.standard"), Source.PLAYER, 10f, 1f))
				if (!balancing.shouldPassThroughEntities) return true
			}

			hitEntities.add(hitEntity)
		}

		// Flying Entity Check
		val flyingHitEntity = flyingRayTraceResult?.hitEntity
		if (flyingHitEntity != null && flyingHitEntity is Damageable) {
			flyingHitEntity.damage(damage, shooter)

			if (flyingHitEntity is Player) {
				if (!PowerArmorManager.glideDisabledPlayers.containsKey(flyingHitEntity.uniqueId)) {
					Tasks.syncDelay(60) { // after 3 seconds
						flyingHitEntity.information("Your rocket boots have rebooted.")
					}
				} // Send this first to prevent duplicate messages when shot multiple times
				val hitNation = SLPlayer[flyingHitEntity.uniqueId]?.nation
				val shooterNation = SLPlayer[shooter as Player].nation
				val isSameNation = shooterNation?.let { shootNation ->
					hitNation?.let { hitNation1 ->
						RelationCache[
							hitNation1, shootNation
						].ordinal < 5
					}
				} ?: false

				// Ignore nation if in arena
				if (!isSameNation || flyingHitEntity.world.name.lowercase().contains("arena")) {
					PowerArmorManager.glideDisabledPlayers[flyingHitEntity.uniqueId] =
						System.currentTimeMillis() + 3000 // 3 second glide disable
					flyingHitEntity.alert("Taking fire! Rocket boots powering down!")
				}
			}

			shooter?.playSound(sound(key("horizonsend:blaster.hitmarker.standard"), Source.PLAYER, 10f, 1f))
			if (!balancing.shouldPassThroughEntities) return true
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
				it.playSound(sound(key(soundWhizz), Source.PLAYER, 1.0f, pitchFactor))
				nearMissPlayers.add(it)
			}
		}

		ticks += 1
		location.add(directionVector)

		return false
	}
}
