package net.horizonsend.ion.server.features.starship.control.movement

import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.informationAction
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.text.colors.Colors
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSettingOrThrow
import net.horizonsend.ion.server.features.gui.custom.settings.commands.SoundSettingsCommand
import net.horizonsend.ion.server.features.nations.DominionTerritoryBuffTypes
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
import net.horizonsend.ion.server.features.starship.status_effects.StarshipStatusEffectTypes
import net.horizonsend.ion.server.miscellaneous.playSoundInRadius
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.actualType
import net.horizonsend.ion.server.miscellaneous.utils.runnable
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor.color
import org.bukkit.util.Vector
import org.litote.kmongo.month
import kotlin.math.abs
import kotlin.math.sign

object StarshipCruising : IonServerComponent() {
	const val SECONDS_PER_CRUISE = 2.0

	override fun onEnable() {
		Tasks.syncRepeat(0L, 1) {

			for (starship in ActiveStarships.allControlledStarships().filter { it.moveThisShipThisTick }) {
				starship.moveThisShipThisTick = false
				if (!PilotedStarships.isPiloted(starship)) continue

				if (shouldStopCruising(starship)) {
					stopCruising(starship.controller, starship)
				}

				updateCruisingShip(starship)
			}
		}
	}

	fun updateCruisingShip(starship: ActiveControlledStarship) {
		processUpdatedHullIntegrity(starship)

		val oldVelocity = starship.cruiseData.velocity.clone()

		val speedModifier = starship.getStrongestActiveStatusEffectFromType(StarshipStatusEffectTypes.CRUISE_SPEED)?.strength ?: 0.0
		val slowModifier = starship.getStrongestActiveStatusEffectFromType(StarshipStatusEffectTypes.CRUISE_SLOW)?.strength ?: 0.0
		/*
		val nationCruiseModifier = starship.playerPilot?.let { player ->
			val cruiseBuffActive = NationBuffTypes.isEffectActive(player, NationBuffTypes.CRUISE_SPEED)
			if (cruiseBuffActive) {
				NationBuffTypes.CRUISE_SPEED.value
			} else 0.0
		} ?: 0.0
		 */

		val dominionBpsModifier = starship.playerPilot?.let { player ->
			if (DominionTerritoryBuffTypes.isEffectActive(player, DominionTerritoryBuffTypes.SPEED))
				DominionTerritoryBuffTypes.SPEED.value
			else 0.0
		} ?: 0.0

		starship.cruiseData.accelerate(starship.speedLimit, starship.reactor.powerDistributor.thrusterPortion)
		val velocity = starship.cruiseData.velocity
		val speed = velocity.length()

		if (oldVelocity.distance(velocity) > 0.01) {
			// velocity has changed
			val targetSpeed = (starship.cruiseData.targetSpeed * (1 + speedModifier) * (1 - slowModifier) + /*nationCruiseModifier + */dominionBpsModifier).toInt()

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

		TranslateMovement.loadChunksAndMove(starship, dx, dy, dz, type = TranslateMovement.MovementSource.CRUISE)
	}

	private fun processUpdatedHullIntegrity(starship: ActiveControlledStarship) {
		val oldBlockCount = starship.cruiseData.lastBlockCount
		val newBlockCount = starship.initialBlockCount

		if (oldBlockCount == newBlockCount) {
			return
		}

		starship.generateThrusterMap()
	}

	fun shouldStopCruising(starship: ActiveControlledStarship): Boolean {
		if (starship.isDirectControlEnabled) return true

		if (starship.controller is NoOpController || starship.controller is UnpilotedController) return true

		return Hyperspace.isWarmingUp(starship)
	}

	fun startCruising(controller: Controller, starship: ActiveControlledStarship, dir: Vector, allowVerticalMovement: Boolean = false) {
		if (starship.type == PLATFORM) {
			controller.userErrorAction("This ship type is not capable of moving.")
			return
		}

		if (!StarshipStartCruisingEvent(starship, controller).callEvent()) {
			return
		}

		val dx = if (abs(dir.x) >= 0.5) sign(dir.x).toInt() else 0
		val dz = if (abs(dir.z) > 0.5) sign(dir.z).toInt() else 0

		if (dx == 0 && dz == 0 && allowVerticalMovement) {
			controller.userErrorAction("Can't go up or down")

			return
		}

		// ThrustData is a binomial data class so we can just expand it like this
		var (accel, maxSpeed) = starship.getThrustData(dx, dz)
		if (maxSpeed == 0) {
			controller.userErrorAction("Can't cruise in that direction")

			return
		}

		maxSpeed /= 2 //This change was done to save the server from imploding from chunk loading lag.
		maxSpeed = (maxSpeed * starship.balancing.cruiseSpeedMultiplier).toInt()
		maxSpeed = minOf(maxSpeed, starship.balancing.maxCruiseSpeed)

		val wasCruising = isCruisingAndAccelerating(starship)

		starship.cruiseData.accel = accel
		starship.cruiseData.targetSpeed = maxSpeed
		starship.cruiseData.targetDir = Vector(
			dx,
			0,
			dz).add(
				if(allowVerticalMovement) Vector(0.0,dir.y,0.0) else Vector()
			).normalize()

		val realAccel = starship.cruiseData.getRealAccel(starship.reactor.powerDistributor.thrusterPortion)

		val info = "<aqua>$dx,$dz <dark_gray>; <yellow>Accel<dark_gray>/<green>Speed<dark_gray>: <yellow>$realAccel<dark_gray>/<yellow>$maxSpeed"

		val useAlternateMethod = (controller as? PlayerController)?.player?.getSettingOrThrow(PlayerSettings::useAlternateDCCruise) ?: false

		if (!wasCruising) {
			starship.informationAction("Cruise started, dir<dark_gray>: $info")

			// Enabling cruise while DC active and setting enabled stops DC and starts cruise
			if (useAlternateMethod && starship.isDirectControlEnabled) {
				starship.setDirectControlEnabled(false)
				starship.onlinePassengers.forEach { passenger ->
					passenger.information(
						"Stopping DC. Starting cruise..."
					)
				}
			} else if (useAlternateMethod) {
				starship.onlinePassengers.forEach { passenger ->
					passenger.information(
						"Cruise started..."
					)
				}
			}
		} else {
			starship.informationAction("Adjusted dir to $info <yellow>[Left click to stop]")
			//if (starship.controller !is AIController) starship.success("Adjusted dir to $info <yellow>[Left click to stop]")
		}

		// Sound alert for cruise
		starship.onlinePassengers.forEach { passenger ->
			if (passenger.getSettingOrThrow(PlayerSettings::enableAdditionalSounds)) {
				var tick = 0
				val length = when (passenger.getSettingOrThrow(PlayerSettings::soundCruiseIndicator)) {
					SoundSettingsCommand.CruiseIndicatorSounds.OFF.ordinal -> 0
					SoundSettingsCommand.CruiseIndicatorSounds.SHORT.ordinal -> 1
					SoundSettingsCommand.CruiseIndicatorSounds.LONG.ordinal -> 4
					else -> 0
				}

				runnable {
					if (tick >= length) cancel()
					if (length != 0) {
						val startCruiseSound =
							starship.data.starshipType.actualType.balancing.shipSounds.startCruise.sound
						playSoundInRadius(passenger.location, 1.0, startCruiseSound)
						tick += 1
					} else cancel()
				}.runTaskTimer(IonServer, 0L, 5L)
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
				if (starship.isDirectControlEnabled) return
				if (!Hyperspace.isWarmingUp(starship)) controller.userErrorAction("Starship is not cruising")
			}
			return
		}

		starship.cruiseData.targetDir = null

		starship.onlinePassengers.forEach { passenger ->
			passenger.information(
				"Cruise stopped, decelerating..."
			)
			if (passenger.getSettingOrThrow(PlayerSettings::enableAdditionalSounds)) {
				var tick = 0
				val length = when (passenger.getSettingOrThrow(PlayerSettings::soundCruiseIndicator)) {
					SoundSettingsCommand.CruiseIndicatorSounds.OFF.ordinal -> 0
					SoundSettingsCommand.CruiseIndicatorSounds.SHORT.ordinal -> 5
					SoundSettingsCommand.CruiseIndicatorSounds.LONG.ordinal -> 20
					else -> 0
				}

				runnable {
					if (tick >= length) cancel()
					if (length != 0) {
						val stopCruiseSound =
							starship.data.starshipType.actualType.balancing.shipSounds.stopCruise.sound
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
}
