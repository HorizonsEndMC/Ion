package net.horizonsend.ion.server.miscellaneous.utils

fun celsiusToKelvin(amountCelsius: Double): Double {
	return amountCelsius + 273.15
}

fun kelvinToCelsius(amountKelvin: Double): Double {
	return amountKelvin - 273.15
}
