package net.horizonsend.ion.server.features.starship.active

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.starships.StarshipData
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.utils.text.MessageFactory
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.PilotedStarships.isPiloted
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.control.controllers.NoOpController
import net.horizonsend.ion.server.features.starship.control.controllers.player.ActivePlayerController
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.control.movement.StarshipControl
import net.horizonsend.ion.server.features.starship.control.movement.StarshipCruising
import net.horizonsend.ion.server.features.starship.event.movement.StarshipMoveEvent
import net.horizonsend.ion.server.features.starship.event.movement.StarshipRotateEvent
import net.horizonsend.ion.server.features.starship.event.movement.StarshipTranslateEvent
import net.horizonsend.ion.server.features.starship.modules.RewardsProvider
import net.horizonsend.ion.server.features.starship.modules.SinkMessageFactory
import net.horizonsend.ion.server.features.starship.movement.RotationMovement
import net.horizonsend.ion.server.features.starship.movement.StarshipBlockedException
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.starship.movement.StarshipMovementException
import net.horizonsend.ion.server.features.starship.movement.TranslateMovement
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.actualType
import net.horizonsend.ion.server.miscellaneous.utils.bukkitWorld
import net.horizonsend.ion.server.miscellaneous.utils.leftFace
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.starlegacy.feature.starship.active.ActiveStarshipHitbox
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.lang.Math.cbrt
import java.util.LinkedList
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class ActiveControlledStarship(
	val data: StarshipData,
	blocks: LongOpenHashSet,
	mass: Double,
	centerOfMass: Vec3i,
	hitbox: ActiveStarshipHitbox,
	// map of carried ship to its blocks
	carriedShips: Map<StarshipData, LongOpenHashSet>
) : ActiveStarship(
	data.bukkitWorld(),
	blocks,
	mass,
	centerOfMass,
	hitbox
) {
	val carriedShips: MutableMap<StarshipData, LongOpenHashSet> = carriedShips.toMutableMap()

	override val type: StarshipType = data.starshipType.actualType
	override val balancing = type.balancingSupplier.get()
	override val rewardsProviders: LinkedList<RewardsProvider> = LinkedList<RewardsProvider>()
	override var sinkMessageFactory: MessageFactory = SinkMessageFactory(this)

	override val interdictionRange: Int = balancing.interdictionRange

	val creationTime = System.currentTimeMillis()
	var lastUnpilotTime: Long = 0
	val minutesUnpiloted get() = if (isPiloted(this) || controller is NoOpController) 0 else TimeUnit.NANOSECONDS.toMinutes(System.nanoTime() - lastUnpilotTime)

	var speedLimit = -1

	data class PendingRotation(val clockwise: Boolean)

	val pendingRotations = LinkedBlockingQueue<PendingRotation>()
	private val rotationTime get() = TimeUnit.MILLISECONDS.toNanos(250L + initialBlockCount / 40L)

	fun getTargetForward(): BlockFace {
		val rotation = pendingRotations.peek()
		return when {
			rotation == null -> forward
			rotation.clockwise -> forward.rightFace
			else -> forward.leftFace
		}
	}

	fun tryRotate(clockwise: Boolean) {
//		Throwable("WHOS TRYING TO ROTATE").printStackTrace()
		pendingRotations.add(PendingRotation(clockwise))

		if (pendingRotations.size > 1) {
			return
		}

		scheduleRotation()
	}

	private fun scheduleRotation() {
		val rotationTimeTicks = TimeUnit.NANOSECONDS.toMillis(rotationTime) / 50L
		Tasks.sync {
			(controller as? ActivePlayerController)?.player?.setCooldown(StarshipControl.CONTROLLER_TYPE, rotationTimeTicks.toInt())
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

		val pilot = this.controller

		val event: StarshipMoveEvent = when (movement) {
			is TranslateMovement -> StarshipTranslateEvent(this, pilot, movement)
			is RotationMovement -> StarshipRotateEvent(this, pilot, movement)
			else -> error("Unrecognized movement type ${movement.javaClass.name}")
		}

		if (!event.callEvent()) {
			return CompletableFuture.completedFuture(false)
		}

		val future = CompletableFuture<Boolean>()
		Tasks.async {
			val result = executeMovement(movement, pilot)
			future.complete(result)
			controller.onMove(movement)
		}

		return future
	}

	var lastBlockedTime: Long = 0

	@Synchronized
	private fun executeMovement(movement: StarshipMovement, controller: Controller): Boolean {
		try {
			movement.execute()
		} catch (e: StarshipMovementException) {
			val location = if (e is StarshipBlockedException) e.location else null
			controller.onBlocked(movement, e, location)
			controller.sendMessage(e.formatMessage())

			sneakMovements = 0
			lastBlockedTime = System.currentTimeMillis()
			return false
		} catch (e: Throwable) {
			serverError("There was an unhandled exception during movement, releasing to prevent damage")

			IonServer.slF4JLogger.error(e.message)
			e.printStackTrace()

			Tasks.sync {
				PilotedStarships.unpilot(this)
				DeactivatedPlayerStarships.deactivateAsync(this)
			}

			return false
		}

		return true
	}

	val dataId: Oid<out StarshipData> = data._id

	// manual move is sneak/direct control
	val manualMoveCooldownMillis: Long = (cbrt(initialBlockCount.toDouble()) * 40).toLong()
	val directControlCooldown get() = 300L + (initialBlockCount / 700) * 30
	var lastManualMove = System.nanoTime() / 1_000_000
	var sneakMovements = 0
	val shieldBars = mutableMapOf<String, BossBar>()

	var beacon: ServerConfiguration.HyperspaceBeacon? = null

	var cruiseData = StarshipCruising.CruiseData(this)

	fun requirePlayerController(): Player = requireNotNull((controller as? PlayerController)?.player) { "Starship must be piloted!" }

	var isDirectControlEnabled: Boolean = false
		private set
	val directControlPreviousVectors = LinkedBlockingQueue<Vector>(4)
	val directControlVector: Vector = Vector()
	var directControlCenter: Location? = null

	fun setDirectControlEnabled(enabled: Boolean) {
		isDirectControlEnabled = enabled
		if (enabled) {
			val dcMessage = text()
				.append(text("Direct Control: ", NamedTextColor.GRAY))
				.append(text("ON ", NamedTextColor.GRAY))
				.append(text("[Use /dc to turn it off - scroll or use hotbar keys to adjust speed - use W/A/S/D to maneuver - hold sneak (", NamedTextColor.YELLOW))
				.append(Component.keybind("key.sneak", NamedTextColor.YELLOW))
				.append(text(") for a boost]", NamedTextColor.YELLOW))
				.build()

			sendMessage(dcMessage)

			val player: Player = (controller as? PlayerController)?.player ?: return

			player.walkSpeed = 0.009f
			directControlCenter = player.location.toBlockLocation().add(0.5, 0.0, 0.5)
		} else {
			sendMessage(
				text()
					.append(text("Direct Control: ", NamedTextColor.GRAY))
					.append(text("OFF ", NamedTextColor.RED))
					.append(text("[Use /dc to turn it on]", NamedTextColor.YELLOW))
					.build()
			)

			directControlVector.x = 0.0
			directControlVector.y = 0.0
			directControlVector.z = 0.0

			val player: Player = (controller as? PlayerController)?.player ?: return
			player.walkSpeed = 0.2f // default
		}
	}

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

	override fun getDisplayNameMiniMessage(): String = this.data.name ?: this.type.displayNameMiniMessage

	override fun getDisplayName(): Component {
		val name = this.data.name ?: return type.displayNameComponent

		return miniMessage().deserialize(name)
	}

	override fun getDisplayNamePlain(): String = getDisplayName().plainText()
}
