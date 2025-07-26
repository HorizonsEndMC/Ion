package net.horizonsend.ion.server.features.starship.movement

import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.features.starship.Starship
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.util.Vector
import kotlin.math.abs
import kotlin.math.pow

object StarshipMovementForecast {

	/** used for estimating a ships pos, vel and accel at any time t*/
	fun forecast(starship: Starship, time : Long, order : Int) : Vector {
		val shiftKinematicEstimator = starship.shiftKinematicEstimator
		val cruiseKinematicEstimator = starship.cruiseKinematicEstimator

		if (order > 0) {
			val shiftForecast = shiftKinematicEstimator.getDerivative(time,order)
			val cruiseForecast = cruiseKinematicEstimator.getDerivative(time,order)
			return shiftForecast.add(cruiseForecast)
		}

		// order is 0
		// can get the best position prediction using the regressed position if the ship is only shift or cruise flying.
		if (shiftKinematicEstimator.movements.isEmpty() || cruiseKinematicEstimator.movements.isEmpty()) {
			val shiftForecast = shiftKinematicEstimator.getDerivative(time,order)
			val cruiseForecast = cruiseKinematicEstimator.getDerivative(time,order)

			//println("shift ref pos: ${shiftKinematicEstimator.referncePos}")
			//println("cruise ref pos: ${cruiseKinematicEstimator.referncePos}")
			//println("shift forcast: $shiftForecast")
			//println("cruise forcast: $cruiseForecast")
			val reference = if (shiftKinematicEstimator.referenceTime < cruiseKinematicEstimator.referenceTime) {
				shiftKinematicEstimator.referncePos
			} else {cruiseKinematicEstimator.referncePos}
			val forecast = shiftForecast.clone().add(cruiseForecast).add(reference)
			return forecast
		}
		// if not, well have to recalculate position from scratch using the current position
		// dear god if ANYONE can figure out how to do this better than by all means, but after looking at this
		//problem for an entire week and failing to do simple math, I give up
		//I will give 100 USD for whoever can replace this curse code with a proper call to getDerivative(time,0)
		val currentPos = starship.centerOfMass.toVector()
		val currentTime = System.currentTimeMillis()
		if (abs(time - currentTime) <= 50) {
			return  currentPos
		}
		// Get kinematics for the target time
		val shiftKinematics = shiftKinematicEstimator.getKinematics(currentTime).toMutableList()
		val cruiseKinematics = cruiseKinematicEstimator.getKinematics(currentTime)

		//kill the acceleration in shift flying cause its basically inaccurate.
		if (shiftKinematics.size >= 3) shiftKinematics.subList(2,shiftKinematics.size -1).replaceAll { Vector() }

		// Ensure both lists are of the same size
		val maxOrder = maxOf(shiftKinematics.size, cruiseKinematics.size)
		val combinedKinematics: List<Vector>

		// Combine derivatives additively
		if (shiftKinematics.size >= cruiseKinematics.size) {
			combinedKinematics = shiftKinematics
			cruiseKinematics.forEachIndexed { i, it ->  combinedKinematics[i].add(it)}
		} else {
			combinedKinematics = cruiseKinematics
			shiftKinematics.forEachIndexed { i, it ->  combinedKinematics[i].add(it)}
		}

		// Compute new position using Taylor series expansion
		val timeDelta = (time - currentTime).toDouble() / 1000.0
		var forecast = currentPos.clone()

		for (order in 1 until maxOrder) { // Start from velocity (1st derivative)
			val factor = (1..order).fold(1.0) { acc, i -> acc * i } // Factorial calculation
			forecast.add(combinedKinematics[order].multiply(timeDelta.pow(order) / factor))
		}
		return  forecast
	}

	fun displayForecast(starship: Starship) {
		val endpoint = 5000
		val interval = 1000
		val particle = Particle.DUST
		for (t in 0 .. endpoint step interval) {
			val size = if(t == 0) 3.0f else 1.5f
			val dustOptions = Particle.DustOptions(Color.GRAY, size)
			val pos = forecast(starship, System.currentTimeMillis() + t, 0)
			starship.world.spawnParticle(particle,pos.x, pos.y, pos.z,1, 0.0, 0.0, 0.0, 0.0, dustOptions, true)
		}
	}

	fun logStatistics(starship: Starship) {
		if (!starship.shiftKinematicEstimator.needsUpdate) return
		starship.debug("---Stats for ${starship.getDisplayNamePlain()}---")
		starship.debug("Current CoM : ${starship.centerOfMass}")
		starship.debug("Estimated CoM : ${forecast(starship, System.currentTimeMillis(), 0)}")
		starship.debug("Estimated velocity : ${starship.velocity}")
		starship.debug("Estimated accel : ${starship.accel}")
		starship.debug("Estimated Pos in 2 seconds : ${forecast(starship, System.currentTimeMillis() + 2000, 0)}")
	}
}
