package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.server.command.admin.GracePeriod
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.configuration.StarshipSounds
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSetting
import net.horizonsend.ion.server.features.machine.AreaShields
import net.horizonsend.ion.server.features.nations.utils.toPlayersInRadius
import net.horizonsend.ion.server.features.player.CombatTimer
import net.horizonsend.ion.server.features.progression.ShipKillXP
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.shield.StarshipShields
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

abstract class SimpleProjectile(
	starship: ActiveStarship?,
	val name: Component,
	var loc: Location,
	var dir: Vector,
	shooter: Damager,
	private val damageType: DamageType,
) : Projectile(starship, shooter) {
	abstract val range: Double
	abstract val speed: Double
	abstract val starshipShieldDamageMultiplier: Double
	abstract val areaShieldDamageMultiplier: Double
	abstract val explosionPower: Float
	open val volume: Int = 12
	open val pitch: Float = 1f
	abstract val soundName: String
	abstract val nearSound: StarshipSounds.SoundInfo
	abstract val farSound: StarshipSounds.SoundInfo
	protected var distance: Double = 0.0
	protected var firedAtNanos: Long = -1
	protected var lastTick: Long = -1
	protected var delta: Double = 0.0
	private var hasHit: Boolean = false

	override fun fire() {
		firedAtNanos = System.nanoTime()
		lastTick = firedAtNanos

		super.fire()

		playCustomSound(loc, nearSound, farSound)
	}

	protected open fun playCustomSound(loc: Location, nearSound: StarshipSounds.SoundInfo, farSound: StarshipSounds.SoundInfo) {
		toPlayersInRadius(loc, range * 20.0) { player ->
			playDirectionalStarshipSound(loc, player, nearSound, farSound, range)
		}
	}

	override fun tick() {
		delta = (System.nanoTime() - lastTick) / 1_000_000_000.0 // Convert to seconds

		val predictedNewLoc = loc.clone().add(dir.clone().multiply(delta * speed))
		if (!predictedNewLoc.isChunkLoaded) {
			return onDespawn()
		}
		val result: RayTraceResult? = loc.world.rayTrace(loc, dir, delta * speed, FluidCollisionMode.NEVER, true, 0.1) { it.type != EntityType.ITEM_DISPLAY }
		val newLoc = result?.hitPosition?.toLocation(loc.world) ?: predictedNewLoc
		val travel = loc.distance(newLoc)

		moveVisually(loc, newLoc, travel)

		var impacted = false

		if (result != null) {
			impacted = tryImpact(result, newLoc)
		}

		loc = newLoc

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
		if (loc.world.name.lowercase(Locale.getDefault()).contains("hyperspace", ignoreCase = true)) return false
		if (GracePeriod.isGracePeriod) return false

		val block: Block? = result.hitBlock
		val entity: Entity? = result.hitEntity

		if (block == null && entity == null) {
			return false
		}

		if (block != null && starship != null && starship.contains(block.x, block.y, block.z)) {
			return false
		}

		if (entity != null && starship != null && starship.isPassenger(entity.uniqueId)) {
			return false
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

		starship?.debug(
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
							val useAlt = player.getSetting(PlayerSettings::useAlternateShieldHitParticle)

							if (!useAlt) {
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
			if (otherStarship == starship || !otherStarship.contains(x, y, z)) continue

			// plays hitmarker sound if the shot did hull damage (assumes the hit block was part of a starship)
			if (explosionOccurred) {
				val player = shooter.starship?.playerPilot?.player
				if (player != null && player.getSetting(PlayerSettings::hitmarkerOnHull))
					player.playSound(sound(key("horizonsend:blaster.hitmarker.standard"), Source.PLAYER, 20f, 0.5f))
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
