package net.horizonsend.ion.server.legacy.starshipweapon.projectile

import net.horizonsend.ion.server.legacy.commands.GracePeriod
import net.starlegacy.feature.progression.ShipKillXP
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.feature.starship.subsystem.shield.StarshipShields
import net.starlegacy.feature.starship.subsystem.weapon.projectile.Projectile
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.SoundCategory
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_19_R2.util.CraftMagicNumbers
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.util.RayTraceResult
import org.bukkit.util.Vector
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

abstract class RayTracedProjectile(
	starship: ActiveStarship?,
	var loc: Location,
	var dir: Vector,
	shooter: Player?
) : Projectile(starship, shooter) {
	abstract val range: Double
	abstract var speed: Double
	abstract val shieldDamageMultiplier: Int
	abstract val thickness: Double
	abstract val explosionPower: Float
	open val volume: Float = 2f
	open val pitch: Float = 1.0f
	abstract val soundName: String
	protected var distance: Double = 0.0
	protected var delta: Double = 0.0

	override fun fire() {
		super.fire()

		val soundName = soundName
		val pitch = pitch
		val volume = volume
		playCustomSound(loc, soundName, volume, pitch)
	}

	private fun playCustomSound(loc: Location, soundName: String, volume: Float, pitch: Float = 1f) {
		loc.world.players.forEach {
			if (it.location.distance(loc) < range) {
				loc.world.playSound(it.location, soundName, SoundCategory.PLAYERS, volume, pitch)
			}
		}
	}

	override fun tick() {
		val result = loc.world.rayTrace(loc, dir, range, FluidCollisionMode.NEVER, true, 0.1, null)

		val targetLocation = result?.hitPosition?.toLocation(loc.world) ?: return

		val block: Block? = result.hitBlock
		val entity: Entity? = result.hitEntity

		visualize(loc, targetLocation)
		if (tryImpact(result)) impact(targetLocation, block, entity)
	}

	abstract fun visualize(loc: Location, targetLocation: Location)

	open fun tryImpact(result: RayTraceResult): Boolean {
		if (starship?.let { it.serverLevel.world }?.name?.lowercase(Locale.getDefault())?.contains("hyperspace")!!) return false
		if (GracePeriod.isGracePeriod) return false

		val block: Block? = result.hitBlock
		val entity: Entity? = result.hitEntity

		if (block == null && entity == null) {
			return false
		} else {
			if ((block != null) && starship.contains(block.x, block.y, block.z)) {
				return false
			} else {
				if ((entity != null) && ((entity.type == EntityType.ENDER_CRYSTAL) || starship.isPassenger(entity.uniqueId))) {
					return false
				}
			}
		}
		return true
	}

	protected open fun impact(newLoc: Location, block: Block?, entity: Entity?) {
		if (GracePeriod.isGracePeriod) return

		val world = newLoc.world

		// use these so we dont use hardcoded Material values
		val armorBlastResist = CraftMagicNumbers.getBlock(Material.STONE).explosionResistance
		val impactedBlastResist = CraftMagicNumbers.getBlock(block?.type ?: Material.STONE_BRICKS).explosionResistance
		val fraction = 1.0 + (armorBlastResist - impactedBlastResist) / 20.0

		StarshipShields.withExplosionPowerOverride(fraction * explosionPower * shieldDamageMultiplier) {
			world.createExplosion(newLoc, explosionPower)
		}

		if (block != null && shooter != null) {
			addToDamagers(world, block)
		}

		if (entity != null && entity is LivingEntity && !world.name.contains("Hyperspace")) {
			entity.damage(10.0, shooter)
		}
	}

	private fun addToDamagers(world: World, block: Block) {
		val damagerId: UUID = requireNotNull(shooter).uniqueId
		val damagerSize: Int = starship?.initialBlockCount ?: 0
		val damager = ShipKillXP.Damager(damagerId, damagerSize)
		val x = block.x
		val y = block.y
		val z = block.z
		for (otherStarship in ActiveStarships.getInWorld(world)) {
			if (otherStarship != starship && otherStarship.contains(x, y, z)) {
				otherStarship.damagers.getOrPut(damager) { AtomicInteger() }.incrementAndGet()
			}
		}
	}
}
