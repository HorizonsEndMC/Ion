package net.horizonsend.ion.server.features.custom.items.type.weapon.blaster

import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.BlasterWeapons.ProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipSounds
import net.horizonsend.ion.server.core.registration.keys.ItemModKeys
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.RocketBoostingMod.glideDisabledPlayers
import net.horizonsend.ion.server.features.custom.items.type.weapon.sword.EnergySword
import net.horizonsend.ion.server.features.starship.damager.addToDamagers
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.DamageEvent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.alongVector
import net.horizonsend.ion.server.miscellaneous.utils.get
import net.kyori.adventure.key.Key.key
import net.kyori.adventure.sound.Sound.Source
import net.kyori.adventure.sound.Sound.sound
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.damage.DamageType
import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.math.pow
import kotlin.math.roundToInt

class RayTracedParticleProjectile(
	val location: Location,
	val shooter: Entity?,
	val balancing: ProjectileBalancing,
	val particle: Particle,
	private val explosiveShot: Boolean,
	private val dustOptions: DustOptions?,
	private val soundWhizz: StarshipSounds.SoundInfo
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
			balancing.shotSize * 4
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
					) {
						it.lastWeaponName = text("Blaster Cannon")
					}
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
					//hitEntity.damage(damage * 1.5, shooter)
					tryDamageEntity(hitEntity, damage * 1.5, shooter, hasHeadshot)

					hitLocation.world.spawnParticle(Particle.CRIT, hitLocation, 10)
					shooter?.playSound(sound(key("horizonsend:blaster.hitmarker.standard"), Source.PLAYER, 20f, 0.5f))
					shooter?.sendActionBar(text("Headshot!", NamedTextColor.RED))
					if (!balancing.shouldPassThroughEntities) return true
				}
			}

			if (!hasHeadshot) {
				//hitEntity.damage(damage, shooter)
				tryDamageEntity(hitEntity, damage, shooter, hasHeadshot)
				shooter?.playSound(sound(key("horizonsend:blaster.hitmarker.standard"), Source.PLAYER, 10f, 1f))
				if (!balancing.shouldPassThroughEntities) return true
			}

			hitEntities.add(hitEntity)
		}

		// Flying Entity Check
		val flyingHitEntity = flyingRayTraceResult?.hitEntity
		if (flyingHitEntity != null && flyingHitEntity is Damageable) {
			tryDamageEntity(flyingHitEntity, damage, shooter, false)
			if (flyingHitEntity is Player) {
				if (!glideDisabledPlayers.containsKey(flyingHitEntity.uniqueId)) {
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
				if (!isSameNation || flyingHitEntity.world.hasFlag(WorldFlag.ARENA)) {
					glideDisabledPlayers[flyingHitEntity.uniqueId] =
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
				if (it.world.ion.hasFlag(WorldFlag.SPACE_WORLD)) pitchFactor = 0.5f

				it.playSound(soundWhizz.copy(pitch = pitchFactor).sound, location.x, location.y, location.z)
				nearMissPlayers.add(it)
			}
		}

		ticks += 1
		location.add(directionVector)

		return false
	}

	fun shouldRebound(hitEntity: Entity, vector: Vector) : Boolean{
		val hitPlayer = hitEntity as? Player ?: return false
		if (!hitPlayer.isBlocking) return false
		val item1 = hitPlayer.inventory.itemInMainHand
		val item2 = hitPlayer.inventory.itemInOffHand
		if (item1.type == Material.AIR && item2.type == Material.AIR) return false
		val customItem1 = item1.customItem
		val customItem2 = item2.customItem
		if(customItem1 == null&&customItem2 == null) return false
		if (customItem1 is EnergySword) return isBlockedByShield(item1, customItem1, vector, hitPlayer)
		if (customItem2 is EnergySword) return isBlockedByShield(item1, customItem2, vector, hitPlayer)
		return false
	}

	private fun isBlockedByShield(item: ItemStack, sword: EnergySword, vector: Vector, hitPlayer: Player) : Boolean{
		val currentBlock = sword.blockComponent.getBlock(item, hitPlayer)
		if (currentBlock == 0) return false
		val xzVector = Vector(vector.x, 0.0, vector.z)
		val xzPlayerDirection = Vector(hitPlayer.eyeLocation.x, 0.0, hitPlayer.eyeLocation.z)
		val angle = xzVector.angle(xzPlayerDirection)
		return angle <= 105 //if the hit is infront of the player we count that as being blocked
	}

	private fun tryDamageEntity(entity: Damageable, damage: Double, shooter: Entity?, headshot: Boolean){
		val lastParryTime = EnergySword.peopleToParryTime[entity] ?: 0
		//check if it should be parried
		if (shouldRebound(entity, this.directionVector) && entity is Player &&
			(System.currentTimeMillis() - lastParryTime <=750)
		) {
			val item1 = entity.inventory.itemInMainHand
			val item2 = entity.inventory.itemInOffHand
			val sword1 = item1.customItem as? EnergySword
			val sword2 = item2.customItem as? EnergySword
			if (sword1 == null&& sword2 == null) return //should never happen
			if (sword1 != null) deflectProjectile(entity, true); entity.setCooldown(item1.type, 0)
			if (sword2 != null) deflectProjectile(entity, true); entity.setCooldown(item2.type, 0)
			return
		}
		//check if it should rebound
		if(shouldRebound(entity, this.directionVector) && entity is Player){
			val item1 = entity.inventory.itemInMainHand
			val item2 = entity.inventory.itemInOffHand
			val sword1 = item1.customItem as? EnergySword
			val sword2 = item2.customItem as? EnergySword
			if (sword1 == null&& sword2 == null) return //should never happen
			if (sword1 != null){
				val block=sword1.blockComponent.getBlock(item1, entity)
				sword1.blockComponent.setBlock(item1, block.minus(balancing.blockbreakAmount).roundToInt(), entity)
				if (block <=0.0) damageEntity(damage-block, entity, shooter)
				else deflectProjectile(entity)
			} else {
				val block = sword2?.blockComponent?.getBlock(item2) ?: 0
				sword2?.blockComponent?.setBlock(item2, block.minus(balancing.blockbreakAmount.roundToInt()), entity)
				if (block <=0.0) damageEntity(damage-block, entity, shooter)
				else deflectProjectile(entity)
			}
			return
		}
		else{
			//Check if the shot entity is a player and whether they have the anti headshot mod
			if (entity is Player) {
				for (item in entity.inventory.armorContents) {
					val customItem = item?.customItem ?: continue

					if (!customItem.hasComponent(CustomComponentTypes.MOD_MANAGER)) continue
					val mods = customItem.getComponent(CustomComponentTypes.MOD_MANAGER).getModKeys(item)
					if (!mods.contains(ItemModKeys.CRANIAL_PLATING)) continue
					return damageEntity(damage/1.5, entity, shooter)
				}
			}
			damageEntity(damage, entity, shooter)
		}
	}

	@Suppress("UnstableApiUsage")
	private fun damageEntity(damage: Double, entity: Damageable, damager: Entity?) {
		if (damager == null) return
		DamageEvent.doDamageEvent(
			damage,
			DamageCause.PROJECTILE,
			entity,
			damager,
			DamageType.PLAYER_ATTACK,
			entity.location,
			false,
			false
		)

		if (entity is Player && damager is Player) {
			for (item in damager.inventory.armorContents) {
				val customItem = item?.customItem ?: continue

				if (!customItem.hasComponent(CustomComponentTypes.MOD_MANAGER)) continue
				val mods = customItem.getComponent(CustomComponentTypes.MOD_MANAGER).getModKeys(item)
				if (!mods.contains(ItemModKeys.ILLUMINATION)) continue
				val glowing = PotionEffect(PotionEffectType.GLOWING, 200, 2)
				entity.addPotionEffect(glowing)
			}
		}
	}

	private fun deflectProjectile(newShooter: Entity, trueRebound: Boolean = false) {
		val newLocation = (newShooter as? Player)?.eyeLocation ?: return
		val d = .35 //dispersion
		val offsetX = randomDouble(-1 * d, d)
		val offsetY = randomDouble(-1 * d, d)
		val offsetZ = randomDouble(-1 * d, d)
		newLocation.direction = (newShooter as? Player)?.eyeLocation?.direction ?: return
		if (!trueRebound) {
			newLocation.direction = newLocation.direction.clone().add(Vector(offsetX, offsetY, offsetZ)).normalize()
		}
		RayTracedParticleProjectile(
			newLocation, newShooter, balancing, particle, explosiveShot, dustOptions, soundWhizz).fire()
	}
}
