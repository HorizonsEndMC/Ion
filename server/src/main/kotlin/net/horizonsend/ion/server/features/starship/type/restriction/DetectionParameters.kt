package net.horizonsend.ion.server.features.starship.type.restriction

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.configuration.StarshipBalancing
import net.horizonsend.ion.server.core.registries.IonRegistryKey
import net.horizonsend.ion.server.features.starship.type.StarshipType

sealed interface DetectionParmeterHolder {
	fun getDetectionParameters(): DetectionParameters
}

class BalancingProvided(val typeKey: IonRegistryKey<StarshipType<*>, StarshipType<StarshipBalancing>>): DetectionParmeterHolder {
	override fun getDetectionParameters(): DetectionParameters {
		return (typeKey.getValue().balancing as DetectionBalancing).parameters
	}
}

interface DetectionBalancing {
	val parameters: DetectionParameters
}

@Serializable
class DetectionParameters(
	val minSize: Int = 25,
	val maxSize: Int = 100,
	val containerPercent: Double,
	val concretePercent: Double = 0.3,
	val crateLimitMultiplier: Double
) : DetectionParmeterHolder {
	override fun getDetectionParameters(): DetectionParameters {
		return this
	}
}
