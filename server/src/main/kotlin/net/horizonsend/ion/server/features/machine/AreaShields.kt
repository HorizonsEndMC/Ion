package net.horizonsend.ion.server.features.machine

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.multiblock.type.defense.passive.areashield.AreaShield
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.isInRange
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object AreaShields : IonServerComponent() {
	val bypassShieldEvents = ConcurrentHashMap.newKeySet<EntityExplodeEvent>()
	private var explosionPowerOverride: Double? = null

	fun getNearbyAreaShields(
		location: Location,
		explosionSize: Double
	): List<AreaShield.AreaShieldEntity> {
		val shields = location.world.ion.multiblockManager[AreaShield.AreaShieldEntity::class]

		return shields.filter {
			val radius = it.poweredMultiblock.radius + explosionSize
			val shieldLoc = it.location

			return@filter shieldLoc.world == location.world && shieldLoc.isInRange(location, radius) && !it.removed
		}
	}

	fun withExplosionPowerOverride(value: Double, block: () -> Unit) {
		try {
			explosionPowerOverride = value
			block()
		} finally {
			explosionPowerOverride = null
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	fun onBlockExplode(event: BlockExplodeEvent) {
		handleExplosion(event.block.location, event.blockList(), event.yield, event)
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	fun onEntityExplode(event: EntityExplodeEvent) {
		if (bypassShieldEvents.remove(event)) return
		handleExplosion(event.location, event.blockList(), event.yield, event)
	}

	private fun handleExplosion(location: Location, blockList: MutableList<Block>, yield: Float, event: Cancellable) {
		if (yield == 0.123f) return

		val power = explosionPowerOverride ?: yield

		var distance = 0.0

		for (block in blockList) distance = max(distance, block.location.distanceSquared(location))

		distance = ceil(sqrt(distance))

		onShieldImpact(
			location,
			blockList,
			distance,
			// I think this is a remnant from some jank micle did to prevent friendly fire from drawing power, idk if removing it will break anything so I'm keeping it
			power != 0.10203f
		)

		if (blockList.isEmpty()) event.isCancelled = true
	}

	private fun onShieldImpact(
		location: Location,
		blockList: MutableList<Block>,
		explosionSize: Double,
		usePower: Boolean
	) {
		val areaShields = getNearbyAreaShields(location, explosionSize)
		var shielded = false

		var explosionResistanceTotal = 0.0
		blockList.forEach { explosionResistanceTotal += it.type.blastResistance }

		for (shield in areaShields) {
			var power = shield.powerStorage.getPower()
			if (power <= 0) continue

			power -= ((blockList.size.toDouble()/explosionResistanceTotal) * 10 * (this.explosionPowerOverride ?: 1.0)).toInt()
			val percent = power.toFloat() / shield.powerStorage.capacity.toFloat()

			if (usePower) shield.powerStorage.setPower(power)

			val color = Color.fromRGB(
				min(255f, 255 - max(0f, 255 * percent)).toInt(),
				0,
				min(255f, max(0f, 255 * percent)).toInt()
			)

			val particle = Particle.DUST
			val dustOptions = Particle.DustOptions(color, 100f)
			location.world.spawnParticle(particle, location, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, true)

			shielded = true
		}
		if (shielded) blockList.clear()
	}

	@EventHandler
	fun onEntityDamage(event: EntityDamageEvent) {
		if (event.cause != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION &&
			event.cause != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
		) return

		val areaShields = getNearbyAreaShields(event.entity.location, 1.0)

		for (shield in areaShields) {
			if (shield.powerStorage.getPower() > 0) event.isCancelled = true
		}
	}
}
