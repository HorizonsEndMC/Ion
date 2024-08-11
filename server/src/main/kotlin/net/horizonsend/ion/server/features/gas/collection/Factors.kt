package net.horizonsend.ion.server.features.gas.collection

import kotlinx.serialization.Serializable
import org.bukkit.Location
import kotlin.math.roundToInt
import kotlin.random.Random

@Serializable
sealed interface Factor {
	fun getAmount(location: Location): Int
}

@Serializable
sealed interface ChildFactor : Factor {
	val parent: Factor

	fun modifyFactor(location: Location, previousResult: Int): Int

	override fun getAmount(location: Location): Int {
		return modifyFactor(location, parent.getAmount(location))
	}
}

@Serializable
data class StaticBase(val amount: Int) : Factor {
	override fun getAmount(location: Location): Int {
		return amount
	}
}

@Serializable
data class HeightRamp(
	override val parent: Factor,
	val minHeight: Int,
	val maxHeight: Int,
	val minValue: Double,
	val maxValue: Double
) : ChildFactor {
	override fun modifyFactor(location: Location, previousResult: Int): Int {
		if (location.y < minHeight || location.y > maxHeight) return previousResult

		val slope = (maxValue - minValue) / (maxHeight - minHeight)
		val ramp = ((location.y - minHeight) * slope) + minValue

		return (previousResult * ramp).roundToInt()
	}
}

@Serializable
data class NoiseFactor(
	override val parent: Factor,
	val minHeight: Int,
	val maxHeight: Int,
	val noiseMin: Double,
	val noiseMax: Double,
) : ChildFactor {
	override fun modifyFactor(location: Location, previousResult: Int): Int {
		if (location.y < minHeight || location.y > maxHeight) return previousResult

		val noise = Random.nextDouble(noiseMin, noiseMax)

		return previousResult + (previousResult * noise).roundToInt()
	}
}
