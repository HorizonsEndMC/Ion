package net.horizonsend.ion.server.features.starship.control.movement

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.informationAction
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.text.colors.Colors
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.gui.custom.settings.commands.SoundSettingsCommand
import net.horizonsend.ion.server.features.nations.utils.playSoundInRadius
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.StarshipType.PLATFORM
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.control.controllers.NoOpController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.control.controllers.player.UnpilotedController
import net.horizonsend.ion.server.features.starship.event.movement.StarshipStartCruisingEvent
import net.horizonsend.ion.server.features.starship.event.movement.StarshipStopCruisingEvent
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.features.starship.movement.TranslateMovement
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.actualType
import net.horizonsend.ion.server.miscellaneous.utils.leftFace
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.horizonsend.ion.server.miscellaneous.utils.runnable
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor.color
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign

object StarshipCruising : IonServerComponent() {
	const val SECONDS_PER_CRUISE = 2.0

	class CruiseData(
		val starship: ActiveControlledStarship,
		var velocity: Vector = Vector(),
		var targetSpeed: Int = 0,
		var targetDir: Vector? = null,
		var accel: Double = 0.0
	) {
		var lastBlockCount = starship.initialBlockCount

		fun accelerate(maxSpeed: Int, thrusterPower: Double) {
			val limitedTarget = (targetSpeed * starship.disabledThrusterRatio).toInt()

			val dir = this.targetDir ?: Vector()
			val speed = if (maxSpeed <= 0) limitedTarget else min(limitedTarget, maxSpeed)
			val newVelocity = dir.clone().multiply(speed)

			newVelocity.y = 0.0
			moveTowards(velocity, newVelocity, getRealAccel(thrusterPower) * SECONDS_PER_CRUISE)
			velocity.y = 0.0

			if (velocity.x.isNaN()) velocity.x = 0.0
			if (velocity.z.isNaN()) velocity.z = 0.0
		}

		// multiplied by power percent and rounded to the nearest hundredth
		fun getRealAccel(thrusterPower: Double): Double = (accel * thrusterPower).roundToHundredth()

		private fun moveTowards(vector: Vector, other: Vector, maxDistance: Double): Vector {
			val direction = other.clone().subtract(vector).normalize()
			val distance = min(maxDistance, other.distance(vector))
			if (distance < maxDistance) {
				vector.x = other.x
				vector.y = other.y
				vector.z = other.z
				return vector
			}
			return vector.add(direction.multiply(distance))
		}
	}

	override fun onEnable() {
		Tasks.syncRepeat(0L, (20 * SECONDS_PER_CRUISE).toLong()) {

			for (starship in ActiveStarships.allControlledStarships()) {
				if (!PilotedStarships.isPiloted(starship)) continue

				if (shouldStopCruising(starship)) {
					stopCruising(starship.controller, starship)
				}

				updateCruisingShip(starship)
			}
		}
	}

	private fun updateCruisingShip(starship: ActiveControlledStarship) {
		processUpdatedHullIntegrity(starship)

		val oldVelocity = starship.cruiseData.velocity.clone()

		starship.cruiseData.accelerate(starship.speedLimit, starship.reactor.powerDistributor.thrusterPortion)
		val velocity = starship.cruiseData.velocity
		val speed = velocity.length()

		if (oldVelocity.distance(velocity) > 0.01) {
			// velocity has changed
			val targetSpeed = starship.cruiseData.targetSpeed

			starship.sendActionBar(ofChildren(
				text("Cruise Speed: ", color(Colors.INFORMATION)),
				text(speed.roundToHundredth(), NamedTextColor.AQUA),
				text("/", NamedTextColor.GRAY),
				text(targetSpeed, NamedTextColor.DARK_AQUA)
			))

			if (starship.isInterdicting && starship.controller !is AIController) {
				starship.setIsInterdicting(false)
			}
		}

		// immobile
		if (speed * SECONDS_PER_CRUISE < 1) {
			return
		}

		val dx = (velocity.x * SECONDS_PER_CRUISE).toInt()
		val dy = (velocity.y * SECONDS_PER_CRUISE).toInt()
		val dz = (velocity.z * SECONDS_PER_CRUISE).toInt()

		if (StarshipControl.locationCheck(starship, dx, dy, dz)) {
			return
		}

		if (starship.isInterdicting && starship.controller !is AIController) {
			starship.setIsInterdicting(false)
		}

		if (starship.isTeleporting) {
			return
		}

		TranslateMovement.loadChunksAndMove(starship, dx, dy, dz)
	}

	private fun processUpdatedHullIntegrity(starship: ActiveControlledStarship) {
		val oldBlockCount = starship.cruiseData.lastBlockCount
		val newBlockCount = starship.initialBlockCount

		if (oldBlockCount == newBlockCount) {
			return
		}

		starship.generateThrusterMap()
	}

	private fun shouldStopCruising(starship: ActiveControlledStarship): Boolean {
		if (starship.isDirectControlEnabled) return true

		if (starship.controller is NoOpController || starship.controller is UnpilotedController) return true

		return Hyperspace.isWarmingUp(starship)
	}

	fun startCruising(controller: Controller, starship: ActiveControlledStarship, dir: Vector) {
		if (starship.type == PLATFORM) {
			controller.userErrorAction("This ship type is not capable of moving.")
			return
		}

		if (!StarshipStartCruisingEvent(starship, controller).callEvent()) {
			return
		}

		val dx = if (abs(dir.x) >= 0.5) sign(dir.x).toInt() else 0
		val dz = if (abs(dir.z) > 0.5) sign(dir.z).toInt() else 0

		if (dx == 0 && dz == 0) {
			controller.userErrorAction("Can't go up or down")

			return
		}

		// ThrustData is a binomial data class so we can just expand it like this
		var (accel, maxSpeed) = starship.getThrustData(dx, dz)
		if (maxSpeed == 0) {
			controller.userErrorAction("Can't cruise in that direction")

			return
		}

		maxSpeed /= 2
		maxSpeed = (maxSpeed * starship.balancing.cruiseSpeedMultiplier).toInt()

		val wasCruising = isCruisingAndAccelerating(starship)

		starship.cruiseData.accel = accel
		starship.cruiseData.targetSpeed = maxSpeed
		starship.cruiseData.targetDir = Vector(dx, 0, dz).normalize()

		val realAccel = starship.cruiseData.getRealAccel(starship.reactor.powerDistributor.thrusterPortion)

		val info = "<aqua>$dx,$dz <dark_gray>; <yellow>Accel<dark_gray>/<green>Speed<dark_gray>: <yellow>$realAccel<dark_gray>/<yellow>$maxSpeed"

		val useAlternateMethod = (controller as? PlayerController)?.player?.let { PlayerCache[it].useAlternateDCCruise } ?: false

		if (useAlternateMethod) {
			if (!wasCruising) {
				updateCruisingShip(starship)
				starship.informationAction("Cruise started, dir<dark_gray>: $info")

				if (starship.isDirectControlEnabled) {
					starship.setDirectControlEnabled(false)
					starship.onlinePassengers.forEach { passenger ->
						passenger.information(
							"Stopping DC. Starting cruise..."
						)
					}
				} else {
					starship.onlinePassengers.forEach { passenger ->
						passenger.information(
							"Cruise started..."
						)
					}
				}
			} else {
				starship.informationAction("Adjusted dir to $info <yellow>[Left click to stop]")
				if (starship.controller !is AIController) starship.success("Adjusted dir to $info <yellow>[Left click to stop]")
			}

			starship.onlinePassengers.forEach { passenger ->
				if (PlayerCache[passenger.uniqueId].enableAdditionalSounds) {
					var tick = 0
					val length = when (PlayerCache[passenger.uniqueId].soundCruiseIndicator) {
						SoundSettingsCommand.CruiseIndicatorSounds.OFF.ordinal -> 0
						SoundSettingsCommand.CruiseIndicatorSounds.SHORT.ordinal -> 1
						SoundSettingsCommand.CruiseIndicatorSounds.LONG.ordinal -> 4
						else -> 0
					}

					runnable {
						if (tick >= length) cancel()
						if (length != 0) {
							val startCruiseSound =
								starship.data.starshipType.actualType.balancingSupplier.get().sounds.startCruise.sound
							playSoundInRadius(passenger.location, 1.0, startCruiseSound)
							tick += 1
						} else cancel()
					}.runTaskTimer(IonServer, 0L, 5L)
				}
			}
		} else {
			if (!isCruisingAndAccelerating(starship)) {
				starship.informationAction("Cruise started, dir<dark_gray>: $info")
			} else {
				starship.informationAction("Adjusted dir to $info <yellow>[Left click to stop]")
				if (starship.controller !is AIController) starship.success("Adjusted dir to $info <yellow>[Left click to stop]")
			}
			starship.onlinePassengers.forEach { passenger ->
				if (PlayerCache[passenger.uniqueId].enableAdditionalSounds) {
					var tick = 0
					val length = when (PlayerCache[passenger.uniqueId].soundCruiseIndicator) {
						SoundSettingsCommand.CruiseIndicatorSounds.OFF.ordinal -> 0
						SoundSettingsCommand.CruiseIndicatorSounds.SHORT.ordinal -> 1
						SoundSettingsCommand.CruiseIndicatorSounds.LONG.ordinal -> 4
						else -> 0
					}

					runnable {
						if (tick >= length) cancel()
						if (length != 0) {
							val startCruiseSound =
								starship.data.starshipType.actualType.balancingSupplier.get().sounds.startCruise.sound
							playSoundInRadius(passenger.location, 1.0, startCruiseSound)
							tick += 1
						} else cancel()
					}.runTaskTimer(IonServer, 0L, 5L)
				}
			}
		}
	}

	fun stopCruising(controller: Controller, starship: ActiveControlledStarship) {
		if (starship.type == PLATFORM) {
			controller.userErrorAction("This ship type is not capable of moving.")
			return
		}

		if (!StarshipStopCruisingEvent(starship, controller).callEvent()) {
			return
		}

		if (!isCruisingAndAccelerating(starship)) {
			if (starship.cruiseData.velocity.lengthSquared() != 0.0) {
				controller.userErrorAction("Starship is decelerating")
			} else {
				controller.userErrorAction("Starship is not cruising")
			}
			return
		}

		starship.cruiseData.targetDir = null

		starship.onlinePassengers.forEach { passenger ->
			passenger.information(
				"Cruise stopped, decelerating..."
			)
			if (PlayerCache[passenger.uniqueId].enableAdditionalSounds) {
				var tick = 0
				val length = when (PlayerCache[passenger.uniqueId].soundCruiseIndicator) {
					SoundSettingsCommand.CruiseIndicatorSounds.OFF.ordinal -> 0
					SoundSettingsCommand.CruiseIndicatorSounds.SHORT.ordinal -> 5
					SoundSettingsCommand.CruiseIndicatorSounds.LONG.ordinal -> 20
					else -> 0
				}

				runnable {
					if (tick >= length) cancel()
					if (length != 0) {
						val stopCruiseSound =
							starship.data.starshipType.actualType.balancingSupplier.get().sounds.stopCruise.sound
						playSoundInRadius(passenger.location, 1.0, stopCruiseSound)
						tick += 1
					} else cancel()
				}.runTaskTimer(IonServer, 0L, 1L)
			}
		}
	}

	fun forceStopCruising(starship: ActiveControlledStarship) {
		starship.cruiseData = CruiseData(starship)
	}

	// If the starship is actively accelerating while in the cruise state
	fun isCruisingAndAccelerating(starship: ActiveControlledStarship) = starship.cruiseData.targetDir != null
	// If the starship is moving due to cruising at all, even if not accelerating
	fun isCruising(starship: ActiveControlledStarship) = starship.cruiseData.velocity.lengthSquared() != 0.0

	enum class Diagonal {
		DIAGONAL_LEFT { override fun getRightFace(forward: BlockFace): BlockFace { return forward.leftFace } },
		DIAGONAL_RIGHT { override fun getRightFace(forward: BlockFace): BlockFace { return forward.rightFace } }

		;

		abstract fun getRightFace(forward: BlockFace): BlockFace

		fun vector(forward: BlockFace): Vector {
			return forward.direction.add(getRightFace(forward).direction).normalize()
		}
	}
}
