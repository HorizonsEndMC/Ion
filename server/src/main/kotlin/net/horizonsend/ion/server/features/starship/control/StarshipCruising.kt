package net.horizonsend.ion.server.features.starship.control

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.informationAction
import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.StarshipType.PLATFORM
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.horizonsend.ion.server.features.starship.controllers.PlayerController
import net.horizonsend.ion.server.features.starship.event.movement.StarshipStartCruisingEvent
import net.horizonsend.ion.server.features.starship.event.movement.StarshipStopCruisingEvent
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.features.starship.movement.TranslateMovement
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.roundToHundredth
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign

object StarshipCruising : IonServerComponent() {
	const val SECONDS_PER_CRUISE = 2.0

	class CruiseData(
		starship: ActiveControlledStarship,
		var velocity: Vector = Vector(),
		var targetSpeed: Int = 0,
		var targetDir: Vector? = null,
		var accel: Double = 0.0
	) {
		var lastBlockCount = starship.initialBlockCount

		fun accelerate(maxSpeed: Int, thrusterPower: Double) {
			val dir = this.targetDir ?: Vector()
			val speed = if (maxSpeed <= 0) targetSpeed else min(targetSpeed, maxSpeed)
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
			for (starship in ActiveStarships.allPlayerShips()) {
				if (!PilotedStarships.isPiloted(starship)) {
					continue
				}

				val player = starship.requireController()

				if (shouldStopCruising(starship)) {
					stopCruising(player, starship)
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

			starship.onlinePassengers.forEach { passenger ->
				passenger.informationAction(
					"Cruise Speed: ${"<aqua>" + speed.roundToHundredth()}<gray>/</gray><dark_aqua>$targetSpeed"
				)
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

		if (starship.isInterdicting) {
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
		if (starship.isDirectControlEnabled) {
			return true
		}

		return Hyperspace.isWarmingUp(starship)
	}

	fun startCruising(controller: Controller, starship: ActiveControlledStarship) {
		if (starship.type == PLATFORM) {
			controller.userErrorAction("This ship type is not capable of moving.")
			return
		}

		if (!StarshipStartCruisingEvent(starship, controller).callEvent()) {
			return
		}

		val player = (controller as? PlayerController)?.player ?: return

		val dir = player.location.direction.setY(0).normalize()

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

		starship.cruiseData.accel = accel
		starship.cruiseData.targetSpeed = maxSpeed
		starship.cruiseData.targetDir = Vector(dx, 0, dz).normalize()

		val realAccel = starship.cruiseData.getRealAccel(starship.reactor.powerDistributor.thrusterPortion)
		val info =
			"<aqua>$dx,$dz <dark_gray>; <yellow>Accel<dark_gray>/<green>Speed<dark_gray>: <yellow>$realAccel<dark_gray>/<yellow>$maxSpeed"
		if (!isCruising(starship)) {
			starship.onlinePassengers.forEach { passenger ->
				passenger.informationAction("Cruise started, dir<dark_gray>: $info")
			}

			updateCruisingShip(starship)
		} else {
			starship.onlinePassengers.forEach { passenger ->
				passenger.informationAction("Adjusted dir to $info <yellow>[Left click to stop]")
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

		if (!isCruising(starship)) {
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
		}
	}

	fun forceStopCruising(starship: ActiveControlledStarship) {
		starship.cruiseData = CruiseData(starship)
	}

	fun isCruising(starship: ActiveControlledStarship) = starship.cruiseData.targetDir != null
}
