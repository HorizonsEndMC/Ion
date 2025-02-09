package net.horizonsend.ion.server.features.starship

object StarshipDebug {

	fun logStatistics(starship: Starship) {
		println("---Stats for ${starship.getDisplayNamePlain()}---")
		println("Current CoM : ${starship.centerOfMass}")
		println("Estimated CoM : ${starship.forecast(System.currentTimeMillis(), 0)}")
		println("Estimated velocity : ${starship.velocity}")
		//println("Estimated accel : ${starship.accel}")
		println("Estimated Pos in 2 seconds : ${starship.forecast(System.currentTimeMillis() + 2000, 0)}")
	}
}
