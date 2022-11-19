package net.starlegacy.feature.gear.blaster

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import net.horizonsend.ion.server.legacy.commands.GracePeriod
import net.starlegacy.feature.misc.CustomItems
import net.starlegacy.util.Tasks
import net.starlegacy.util.enumValueOfOrNull
import org.bukkit.Color
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Material.AIR
import org.bukkit.Material.SHIELD
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector

private const val ITERATIONS_PER_BLOCK = 1f

class BlasterProjectile(
	private var shooter: Entity,
	private var location: Location,
	private var color: Color,
	private val damage: Double,
	private val range: Int,
	private val thickness: Double,
	speed: Double,
	private val explosionPower: Float?,
	sound: String,
	pitchBase: Double,
	pitchRange: Double,
	direction: Vector
) {

	companion object {
		val scheduler = Executors.newScheduledThreadPool(1, Tasks.namedThreadFactory("blasters"))
	}

	private var distance: Double = 0.0
	private val world = location.world
	private val movementPerBlock = 1.0 / ITERATIONS_PER_BLOCK
	private var velocity: Vector = direction.normalize().multiply(movementPerBlock)

	private val movementDelay = (1000 / (speed * ITERATIONS_PER_BLOCK)).toInt()

	private val offsets = listOf(
		Vector(0.0, 0.0, 0.0),
		Vector(-1.0, 0.0, 0.0),
		Vector(+1.0, 0.0, 0.0),
		Vector(0.0, -1.0, 0.0),
		Vector(0.0, +1.0, 0.0),
		Vector(0.0, 0.0, -1.0),
		Vector(0.0, 0.0, +1.0)
	).map { it.multiply(thickness) }

	init {
		scheduler.submit {
			schedule()
		}
		playSound(sound, pitchBase, pitchRange)
	}

	private fun playSound(sound: String, pitchBase: Double, pitchRange: Double) {
		val volume = 1.0f
		val pitch = (pitchBase + Math.random() * pitchRange).toFloat()

		val enumSound = enumValueOfOrNull<Sound>(sound)

		if (enumSound == null) {
			location.world.playSound(location, sound, volume, pitch)
			return
		}

		location.world.playSound(location, enumSound, volume, pitch)
	}

	private fun schedule() {
		if (GracePeriod.isGracePeriod) return

		scheduler.schedule({
			if (distance < range) {
				Tasks.sync { tick() }
				schedule()
			}
		}, movementDelay.toLong(), TimeUnit.MILLISECONDS)
	}

	private fun tick() {
		if (GracePeriod.isGracePeriod) return

		this.distance += movementPerBlock
		if (this.distance > range) {
			return
		}
		val newLocation = location.clone().add(velocity)
		val newBlockX = newLocation.blockX
		val newBlockZ = newLocation.blockZ
		if (!world.isChunkLoaded(newBlockX shr 4, newBlockZ shr 4)) {
			return
		}

		val ray = offsets.mapNotNull { offset ->
			val start = location.clone().add(offset)
			world.rayTrace(start, velocity, movementPerBlock, FluidCollisionMode.NEVER, true, 0.0)
			{ it != shooter }
		}.minByOrNull { it.hitPosition.distanceSquared(location.toVector()) }

		if (ray?.hitBlock != null) {
			this.distance = Double.MAX_VALUE
			world.playSound(location, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.25f, 1.5f)
			if (explosionPower != 0f) {
				val block = location.block
				val type = block.type

				// liquid stuff is so explosions work underwater
				val liquid = block.isLiquid
				if (liquid) {
					block.setType(AIR, false)
				}
				explosionPower?.let {
					world.createExplosion(
						location.toVector().midpoint(newLocation.toVector()).toLocation(world),
						explosionPower
					)
				}
				if (liquid) {
					block.setType(type, false)
				}
			}
			return
		}

		val entity: LivingEntity? = ray?.hitEntity as? LivingEntity

		if (entity == null) {
			location = newLocation
			if (distance > 0.75) {
				val particle = Particle.REDSTONE
				val dustOptions = Particle.DustOptions(color, thickness.toFloat() * 4.0f)
				world.spawnParticle(particle, location, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, true)
			}
			return
		}

		var deflected = false
		if (entity is Player) {
			if (entity.isBlocking && entity.getCooldown(SHIELD) == 0) {
				for (slot in arrayOf(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND)) {
					// check if sword
					CustomItems[entity.inventory.getItem(slot)] as? CustomItems.EnergySwordItem
						?: continue
					entity.world.playSound(entity.location, "energy_sword.strike", 5.0f, 1.0f)
					velocity = velocity.getCrossProduct(entity.location.direction).normalize()
					shooter = entity
					deflected = true
					break
				}
			}
		}

		entity.damage(damage, shooter)
		if (entity is Player) {
			(shooter as? Player)?.playSound(shooter.location, "laserhit", 1f, 0.5f)
		}

		world.playSound(location, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.25f, 1.5f)
		if (!deflected) {
			distance = Double.MAX_VALUE
		}
	}
}