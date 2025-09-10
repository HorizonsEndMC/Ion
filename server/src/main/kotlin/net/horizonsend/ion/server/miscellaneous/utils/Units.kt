package net.horizonsend.ion.server.miscellaneous.utils

fun celsiusToKelvin(amountCelsius: Double): Double {
	return amountCelsius + 273.15
}

fun kelvinToCelsius(amountKelvin: Double): Double {
	return amountKelvin - 273.15
}

fun litersToMetersCubed(amountLiters: Double): Double {
	return amountLiters / 1000.0
}

fun metersCubedToLiters(amountMetersCubed: Double): Double {
	return amountMetersCubed * 1000.0
}
