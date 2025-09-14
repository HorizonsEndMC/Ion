package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.server.command.admin.GracePeriod
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.configuration.starship.StarshipProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipSounds.SoundInfo
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSetting
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSettingOrThrow
import net.horizonsend.ion.server.features.machine.AreaShields
import net.horizonsend.ion.server.features.nations.utils.toPlayersInRadius
import net.horizonsend.ion.server.features.player.CombatTimer
import net.horizonsend.ion.server.features.progression.ShipKillXP
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.shield.StarshipShields
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.listener.misc.ProtectionListener
import net.horizonsend.ion.server.miscellaneous.playDirectionalStarshipSound
import net.kyori.adventure.key.Key.key
import net.kyori.adventure.sound.Sound.Source
import net.kyori.adventure.sound.Sound.sound
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.craftbukkit.util.CraftMagicNumbers
import org.bukkit.damage.DamageType
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.util.RayTraceResult
import org.bukkit.util.Vector
import java.util.Locale
import kotlin.math.roundToInt

abstract class SimpleProjectile<out B : StarshipProjectileBalancing>(
	val source: ProjectileSource,
	val name: Component,

	var location: Location,
	var direction: Vector,

	shooter: Damager,
	private val damageType: DamageType
) : Projectile(shooter) {
	protected open val balancing: B get() = source.getBalancing(this::class)

	val range: Double get() = balancing.range
	open val speed: Double get() = balancing.speed

	open val starshipShieldDamageMultiplier: Double get() = balancing.starshipShieldDamageMultiplier
	val areaShieldDamageMultiplier: Double get() = balancing.areaShieldDamageMultiplier
	open val explosionPower: Float get() = balancing.explosionPower

	protected var distance: Double = 0.0
	protected var firedAtNanos: Long = -1
	protected var lastTick: Long = -1
	protected var delta: Double = 0.0
	private var hasHit: Boolean = false

	val nearSound: SoundInfo get() = balancing.fireSoundNear
	val farSound: SoundInfo get() = balancing.fireSoundFar

	override fun fire() {
		firedAtNanos = System.nanoTime()
		lastTick = firedAtNanos

		super.fire()
		playCustomSound(location, nearSound, farSound)
	}

	protected open fun playCustomSound(loc: Location, nearSound: SoundInfo, farSound: SoundInfo) {
		toPlayersInRadius(loc, range * 20.0) { player ->
			playDirectionalStarshipSound(loc, player, nearSound, farSound, range)
		}
	}

	override fun tick() {
		delta = (System.nanoTime() - lastTick) / 1_000_000_000.0 // Convert to seconds

		val predictedNewLoc = location.clone().add(direction.clone().multiply(delta * speed))
		if (!predictedNewLoc.isChunkLoaded) {
			return onDespawn()
		}
		val result: RayTraceResult? = location.world.rayTrace(location, direction, delta * speed, FluidCollisionMode.NEVER, true, 0.1) { it.type != EntityType.ITEM_DISPLAY }
		val newLoc = result?.hitPosition?.toLocation(location.world) ?: predictedNewLoc
		val travel = location.distance(newLoc)

		moveVisually(location, newLoc, travel)

		var impacted = false

		if (result != null) {
			impacted = tryImpact(result, newLoc)
		}

		location = newLoc

		distance += travel

		if (impacted) {
			onImpact()
			return onDespawn()
		}

		if (distance >= range) {
			return onDespawn()
		}

		lastTick = System.nanoTime()
		reschedule()
	}

	protected abstract fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double)

	protected open fun onImpact() {}
	protected open fun onDespawn() {}

	protected fun tryImpact(result: RayTraceResult, newLoc: Location): Boolean {
		if (location.world.name.lowercase(Locale.getDefault()).contains("hyperspace", ignoreCase = true)) return false
		if (GracePeriod.isGracePeriod) return false

		val block: Block? = result.hitBlock
		val entity: Entity? = result.hitEntity

		if (block == null && entity == null) {
			return false
		}

		if (source is StarshipProjectileSource) {
			if (block != null && source.starship.contains(block.x, block.y, block.z)) {
				return false
			}

			if (entity != null && source.starship.isPassenger(entity.uniqueId)) {
				return false
			}
		}

		impact(newLoc, block, entity)
		return true
	}

	protected open fun impact(newLoc: Location, block: Block?, entity: Entity?) {
		if (GracePeriod.isGracePeriod) return

		val world = newLoc.world
		if (world.environment == World.Environment.NETHER && world.name.contains("hyperspace", ignoreCase=true)) {
			return
		}

		// use these so we dont use hardcoded Material values
		val armorBlastResist = CraftMagicNumbers.getBlock(Material.STONE).explosionResistance
		val impactedBlastResist = CraftMagicNumbers.getBlock(block?.type ?: Material.STONE_BRICKS).explosionResistance
		val fraction = 1.0 + (armorBlastResist - impactedBlastResist) / 20.0

		source.debug(
			"ship dmg: \n\n" +
			"armorBlastResist = $armorBlastResist, \n" +
			"impactedBlastResist = $impactedBlastResist, \n" +
			"fraction = $fraction, \n" +
			"shieldDamageMultiplier = $starshipShieldDamageMultiplier, \n" +
			"result = ${fraction * explosionPower * starshipShieldDamageMultiplier}"
		)

		var explosionOccurred = false

		StarshipShields.withExplosionPowerOverride(fraction * explosionPower * starshipShieldDamageMultiplier) {
			AreaShields.withExplosionPowerOverride(fraction * explosionPower * areaShieldDamageMultiplier) {
				if (!hasHit) {
					// shields/area shields cancel explosion damage
					explosionOccurred = world.createExplosion(newLoc, explosionPower)

					if (explosionPower > 0) {
						val base = explosionPower.coerceAtLeast(1f)

						// Send per-player so each user’s setting applies
						toPlayersInRadius(newLoc, /* visibility radius */ 500.0) { player ->
							val useAlt = player.getSetting(PlayerSettings::useAlternateShieldHitParticle) ?: return@toPlayersInRadius

							if (useAlt == false) {
								// Original behavior (large single flash)
								player.spawnParticle(
									Particle.FLASH,
									newLoc.x, newLoc.y, newLoc.z,
									base.toInt(),                 // count ~ explosionPower
									(base / 2.0),
									(base / 2.0),
									(base / 2.0),
									0.0,
									null,
									true
								)
								return@toPlayersInRadius
							}

							// Heuristic scaling to mimic FLASH’s "big" presence
							val particle = Particle.DUST
							val count = (base * 40.0).toInt()
							val spread = base * 0.5
							val particleSpeed = 0.5
							val dustOptions = Particle.DustOptions(Color.WHITE, 0.7f)
							player.spawnParticle(
								particle,
								newLoc.x, newLoc.y, newLoc.z,
								count,
								spread, spread, spread,
								particleSpeed,
								dustOptions,
								true
							)
						}
					}

					hasHit = true
				}
			}
		}

		if (block != null) addToDamagers(world, block, shooter, explosionPower.roundToInt(), explosionOccurred)

		if (entity != null && entity is LivingEntity) {
			onHitEntity(entity)
		}
	}

	protected open fun onHitEntity(entity: LivingEntity) {
		balancing.entityDamage.deal(entity, shooter, damageType)
	}

	protected fun addToDamagers(world: World, block: Block, shooter: Damager, points: Int = 1, explosionOccurred: Boolean = false) {
		val x = block.x
		val y = block.y
		val z = block.z

		for (otherStarship in ActiveStarships.getInWorld(world)) {
			if (otherStarship == (source as? StarshipProjectileSource)?.starship || !otherStarship.contains(x, y, z)) continue

			val player = shooter.starship?.playerPilot?.player

			// plays hitmarker sound if the shot did shield damage (if player setting is enabled)
			if (player != null && player.getSettingOrThrow(PlayerSettings::hitmarkerOnShield)) {
				player.playSound(sound(key("horizonsend:blaster.hitmarker.standard"), Source.PLAYER, 20f, 1.0f))
			}

			// plays hitmarker sound if the shot did hull damage (assumes the hit block was part of a starship)
			if (explosionOccurred) {
				if (player != null && player.getSettingOrThrow(PlayerSettings::hitmarkerOnHull)) {
					player.playSound(sound(key("horizonsend:blaster.hitmarker.standard"), Source.PLAYER, 20f, 0.5f))
				}
			}

			otherStarship.damagers.getOrPut(shooter) {
				ShipKillXP.ShipDamageData()
			}.incrementPoints(points)

			otherStarship.lastWeaponName = name

			onImpactStarship(otherStarship, block.location)

			if (!ProtectionListener.isProtectedCity(block.location)) {
				CombatTimer.evaluateSvs(shooter, otherStarship)
			}
		}
	}

	open fun onImpactStarship(starship: ActiveStarship, impactLocation: Location) {}
}
