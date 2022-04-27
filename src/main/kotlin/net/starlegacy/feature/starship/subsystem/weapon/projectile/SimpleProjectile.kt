package net.starlegacy.feature.starship.subsystem.weapon.projectile

import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import net.horizonsend.ion.core.commands.GracePeriod
import net.minecraft.network.protocol.game.ClientboundCustomSoundPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundSource
import net.minecraft.world.phys.Vec3
import net.starlegacy.feature.progression.ShipKillXP
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.feature.starship.subsystem.shield.StarshipShields
import net.starlegacy.util.nms
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_18_R2.util.CraftMagicNumbers
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.util.RayTraceResult
import org.bukkit.util.Vector

abstract class SimpleProjectile(
	starship: ActiveStarship?,
	var loc: Location,
	var dir: Vector,
	shooter: Player?
) : Projectile(starship, shooter) {
	abstract val range: Double
	abstract val speed: Double
	abstract val shieldDamageMultiplier: Int
	abstract val thickness: Double
	abstract val explosionPower: Float
	open val volume: Int = 12
	open val pitch: Float = 1f
	abstract val soundName: String
	protected var distance: Double = 0.0
	protected var firedAtNanos: Long = -1
	private var lastTick: Long = -1
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
		val x = loc.x
		val y = loc.y
		val z = loc.z
		val packet = ClientboundCustomSoundPacket(
			ResourceLocation(soundName),
			SoundSource.MASTER,
			Vec3(x, y, z),
			1.0f,
			pitch
		)
		val range = if (chunkRange > 1) chunkRange * 16.0 else 16.0
		val nmsWorld = loc.world.nms
		val playerList = checkNotNull(nmsWorld.server).playerList
		playerList.broadcast(null, x, y, z, range, nmsWorld.dimension(), packet)
	}

	override fun tick() {
		delta = (System.nanoTime() - lastTick) / 1_000_000_000.0

		val predictedNewLoc = loc.clone().add(dir.clone().multiply(delta * speed))
		if (!predictedNewLoc.isChunkLoaded) {
			return
		}

		val result = loc.world.rayTrace(loc, dir, delta * speed, FluidCollisionMode.NEVER, true, 0.1) { true }
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

	private fun tryImpact(result: RayTraceResult, newLoc: Location): Boolean {
		if (starship?.world?.name?.lowercase(Locale.getDefault())?.contains("hyperspace")!!) return false
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

		// use these so we dont use hardcoded Material values
		val armorBlastResist = CraftMagicNumbers.getBlock(Material.STONE).explosionResistance
		val impactedBlastResist = CraftMagicNumbers.getBlock(block?.type ?: Material.STONE_BRICKS).explosionResistance
		val fraction = 1.0 + (armorBlastResist - impactedBlastResist) / 20.0

		StarshipShields.withExplosionPowerOverride(fraction * explosionPower * shieldDamageMultiplier) {
			if (!hasHit) {
				world.createExplosion(newLoc, explosionPower)
				hasHit = true
			}
		}

		if (block != null && shooter != null) {
			addToDamagers(world, block)
		}

		if (entity != null && entity is LivingEntity) {
			entity.damage(10.0, shooter)
		}
	}

	private fun addToDamagers(world: World, block: Block) {
		val damagerId: UUID = requireNotNull(shooter).uniqueId
		val damagerSize: Int? = starship?.blockCount
		val damager = ShipKillXP.Damager(damagerId, damagerSize)
		val x = block.x
		val y = block.y
		val z = block.z
		for (otherStarship in ActiveStarships.getInWorld(world)) {
			if (otherStarship != starship && otherStarship.contains(x, y, z)) {
				otherStarship.damagers.getOrPut(damager, { AtomicInteger() }).incrementAndGet()
			}
		}
	}
}
