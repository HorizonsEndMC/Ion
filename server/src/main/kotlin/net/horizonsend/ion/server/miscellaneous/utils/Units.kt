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

fun litersToCentimetersCubed(amountLiters: Double): Double {
	return amountLiters * 1000.0
}

fun centimetersCubedToLiters(amountCentimetersCubed: Double): Double {
	return amountCentimetersCubed / 1000.0
}

fun gramsToKilograms(amountGrams: Double): Double {
	return amountGrams / 1000.0
}

fun kilogramsToGrams(amountKilorams: Double): Double {
	return amountKilorams * 1000.0
}

fun kilogramsToTons(amountKilograms: Double): Double {
	return amountKilograms / 1000.0
}

fun pascalsToBars(valuePascals: Double): Double {
	return valuePascals / 1_000_000.0
}

fun rpmToHertz(valueRPM: Double): Double {
	return valueRPM / 60.0
}

fun hertzToRPM(valueHertz: Double): Double {
	return valueHertz * 60.0
}
