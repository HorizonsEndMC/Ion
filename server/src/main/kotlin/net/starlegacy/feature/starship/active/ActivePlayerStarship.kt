package net.starlegacy.feature.starship.active

import co.aikar.commands.ConditionFailedException
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.ServerConfiguration
import net.horizonsend.ion.server.starships.control.LegacyController
import net.horizonsend.ion.server.starships.control.PlayerController
import net.minecraft.core.BlockPos
import net.starlegacy.cache.nations.NationCache
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.database.Oid
import net.starlegacy.database.schema.starships.PlayerStarshipData
import net.starlegacy.feature.starship.StarshipType
import net.starlegacy.feature.starship.control.StarshipControl
import net.starlegacy.feature.starship.control.StarshipCruising
import net.starlegacy.feature.starship.event.StarshipMoveEvent
import net.starlegacy.feature.starship.event.StarshipRotateEvent
import net.starlegacy.feature.starship.event.StarshipTranslateEvent
import net.starlegacy.feature.starship.movement.RotationMovement
import net.starlegacy.feature.starship.movement.StarshipMovement
import net.starlegacy.feature.starship.movement.TranslateMovement
import net.starlegacy.util.Tasks
import net.starlegacy.util.msg
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.boss.BossBar
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer
import org.bukkit.entity.Player
import java.lang.Math.cbrt
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class ActivePlayerStarship(
	val data: PlayerStarshipData,
	blocks: LongOpenHashSet,
	mass: Double,
	centerOfMass: BlockPos,
	hitbox: ActiveStarshipHitbox,
	// map of carried ship to its blocks
	carriedShips: Map<PlayerStarshipData, LongOpenHashSet>
) : ActiveStarship(
	(data.bukkitWorld() as CraftWorld).handle,
	blocks,
	mass,
	centerOfMass,
	hitbox
) {
	val carriedShips: MutableMap<PlayerStarshipData, LongOpenHashSet> = carriedShips.toMutableMap()
	override val type: StarshipType = data.starshipType
	override val interdictionRange: Int = data.starshipType.interdictionRange

	var lastUnpilotTime: Long = 0

	var pilot: Player?
		get() = (controller as? PlayerController)?.serverPlayer?.bukkitEntity
		set(value) {
			controller = if (value == null) null else LegacyController(this, (value as CraftPlayer).handle)
		}

	var oldpilot: Player? = null

	val minutesUnpiloted = if (pilot != null) 0 else TimeUnit.NANOSECONDS.toMinutes(System.nanoTime() - lastUnpilotTime)

	var speedLimit = -1

	private data class PendingRotation(val clockwise: Boolean)

	private val pendingRotations = LinkedBlockingQueue<PendingRotation>()
	private val rotationTime get() = TimeUnit.MILLISECONDS.toNanos(250L + initialBlockCount / 40L)

	fun tryRotate(clockwise: Boolean) {
		pendingRotations.add(PendingRotation(clockwise))

		if (pendingRotations.size > 1) {
			return
		}

		scheduleRotation()
	}

	private fun scheduleRotation() {
		val rotationTimeTicks = TimeUnit.NANOSECONDS.toMillis(rotationTime) / 50L
		Tasks.sync {
			pilot?.setCooldown(StarshipControl.CONTROLLER_TYPE, rotationTimeTicks.toInt())
		}
		Tasks.syncDelay(rotationTimeTicks) {
			if (pendingRotations.none()) {
				return@syncDelay
			}

			val rotation = pendingRotations.poll()

			if (pendingRotations.any()) {
				scheduleRotation()
			}

			moveAsync(RotationMovement(this, rotation.clockwise))
		}
	}

	override fun moveAsync(movement: StarshipMovement): CompletableFuture<Boolean> {
		if (!ActiveStarships.isActive(this)) {
			return CompletableFuture.completedFuture(false)
		}

		val pilot = this.pilot
		if (pilot != null) {
			val event: StarshipMoveEvent = when (movement) {
				is TranslateMovement -> StarshipTranslateEvent(this, pilot, movement)
				is RotationMovement -> StarshipRotateEvent(this, pilot, movement)
				else -> error("Unrecognized movement type ${movement.javaClass.name}")
			}

			if (!event.callEvent()) {
				return CompletableFuture.completedFuture(false)
			}
		}

		val future = CompletableFuture<Boolean>()
		Tasks.async {
			val result = executeMovement(movement, pilot)
			future.complete(result)
		}

		return future
	}

	@Synchronized
	private fun executeMovement(movement: StarshipMovement, pilot: Player?): Boolean {
		try {
			movement.execute()
		} catch (e: ConditionFailedException) {
			pilot?.msg("&c" + (e.message ?: "Starship could not move for an unspecified reason!"))
			sneakMovements = 0
			return false
		}

		return true
	}

	val dataId: Oid<PlayerStarshipData> = data._id

	// manual move is sneak/direct control
	val manualMoveCooldownMillis: Long = (cbrt(initialBlockCount.toDouble()) * 40).toLong()
	var lastManualMove = System.nanoTime() / 1_000_000
	var sneakMovements = 0
	val shieldBars = mutableMapOf<String, BossBar>()

	var beacon: ServerConfiguration.HyperspaceBeacon? = null

	var cruiseData = StarshipCruising.CruiseData(this)

	override val weaponColor: Color
		get() = pilot?.let { PlayerCache[it].nation }?.let { Color.fromRGB(NationCache[it].color) } ?: Color.RED

	fun requirePilot(): Player = requireNotNull(pilot) { "Starship must be piloted!" }

	override fun removePassenger(playerID: UUID) {
		super.removePassenger(playerID)
		val player = Bukkit.getPlayer(playerID) ?: return
		for (shieldBar in shieldBars.values) {
			shieldBar.removePlayer(player)
		}
	}

	override fun clearPassengers() {
		for (passenger in onlinePassengers) {
			for (shieldBar in shieldBars.values) {
				shieldBar.removePlayer(passenger)
			}
		}
		super.clearPassengers()
	}
}
