package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.command.admin.GracePeriod
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.features.machine.AreaShields
import net.horizonsend.ion.server.features.progression.ShipKillXP
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.damager.EntityDamager
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.starship.subsystem.shield.StarshipShields
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.SoundCategory
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_20_R3.util.CraftMagicNumbers
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.util.RayTraceResult
import org.bukkit.util.Vector
import java.util.Locale

abstract class SimpleProjectile(
	starship: ActiveStarship?,
	var loc: Location,
	var dir: Vector,
	shooter: Damager
) : Projectile(starship, shooter) {
	abstract val range: Double
	abstract val speed: Double
	abstract val starshipShieldDamageMultiplier: Double
	abstract val areaShieldDamageMultiplier: Double
	abstract val explosionPower: Float
	open val volume: Int = 12
	open val pitch: Float = 1f
	abstract val soundName: String
	protected var distance: Double = 0.0
	protected var firedAtNanos: Long = -1
	protected var lastTick: Long = -1
	protected var delta: Double = 0.0
	private var hasHit: Boolean = false

	override fun fire() {
		firedAtNanos = System.nanoTime()
		lastTick = firedAtNanos

		super.fire()

		val soundName = soundName
		val pitch = pitch
		val volume = volume
		playCustomSound(loc, soundName, volume, pitch)
	}

	protected fun playCustomSound(loc: Location, soundName: String, chunkRange: Int, pitch: Float = 1f) {
		loc.world.players.forEach {
			if (it.location.distance(loc) < range) {
				loc.world.playSound(it.location, soundName, SoundCategory.PLAYERS, 1.0f, pitch)
			}
		}
	}

	override fun tick() {
		delta = (System.nanoTime() - lastTick) / 1_000_000_000.0 // Convert to seconds

		val predictedNewLoc = loc.clone().add(dir.clone().multiply(delta * speed))
		if (!predictedNewLoc.isChunkLoaded) {
			return
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
			return
		}

		if (distance >= range) {
			return
		}

		lastTick = System.nanoTime()
		reschedule()
	}

	protected abstract fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double)

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
		StarshipShields.withExplosionPowerOverride(fraction * explosionPower * starshipShieldDamageMultiplier) {
			AreaShields.withExplosionPowerOverride(fraction * explosionPower * areaShieldDamageMultiplier) {
				if (!hasHit) {
					world.createExplosion(newLoc, explosionPower)

					world.spawnParticle(
						Particle.FLASH,
						newLoc.x,
						newLoc.y,
						newLoc.z,
						explosionPower.toInt(),
						explosionPower.toDouble() / 2,
						explosionPower.toDouble() / 2,
						explosionPower.toDouble() / 2,
						0.0,
						null,
						true
					)
					hasHit = true
				}
			}
		}

		if (block != null) addToDamagers(world, block, shooter)

		if (entity != null && entity is LivingEntity) when (shooter) {
			is PlayerDamager -> entity.damage(10.0, shooter.player)
			is EntityDamager -> entity.damage(10.0, shooter.entity)
			else -> entity.damage(10.0)
		}
	}

	private fun addToDamagers(world: World, block: Block, shooter: Damager) {
		val x = block.x
		val y = block.y
		val z = block.z
		for (otherStarship in ActiveStarships.getInWorld(world)) {
			if (otherStarship == starship || !otherStarship.contains(x, y, z)) continue

			otherStarship.damagers.getOrPut(shooter) { ShipKillXP.ShipDamageData() }.points.incrementAndGet()
			onImpactStarship(otherStarship, block.location)
		}
	}

	open fun onImpactStarship(starship: ActiveStarship, impactLocation: Location) {}
}
