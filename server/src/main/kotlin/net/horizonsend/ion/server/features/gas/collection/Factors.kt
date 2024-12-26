package net.horizonsend.ion.server.features.gas.collection

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.configuration.util.DoubleAmount
import net.horizonsend.ion.server.configuration.util.IntegerAmount
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
data class StaticBase(val amount: IntegerAmount) : Factor {
	override fun getAmount(location: Location): Int {
		return amount.get()
	}
}

@Serializable
data class ChildWeight(
	override val parent: Factor,
	val weight: DoubleAmount
) : ChildFactor {
	override fun modifyFactor(location: Location, previousResult: Int): Int {
		return (previousResult * weight.get()).roundToInt()
	}
}

@Serializable
data class HeightRamp(
    override val parent: Factor,
    val minHeight: IntegerAmount,
    val maxHeight: IntegerAmount,
    val minWeight: DoubleAmount,
    val maxWeight: DoubleAmount
) : ChildFactor {
	override fun modifyFactor(location: Location, previousResult: Int): Int {
		if (location.y < minHeight.get() || location.y > maxHeight.get()) return previousResult

		val slope = (maxWeight.get() - minWeight.get()) / (maxHeight.get() - minHeight.get())
		val ramp = ((location.y - minHeight.get()) * slope) + minWeight.get()

		return (previousResult * ramp).roundToInt()
	}
}

@Serializable
data class NoiseFactor(
    override val parent: Factor,
    val minHeight: IntegerAmount,
    val maxHeight: IntegerAmount,
    val noiseMin: DoubleAmount,
    val noiseMax: DoubleAmount,
) : ChildFactor {
	override fun modifyFactor(location: Location, previousResult: Int): Int {
		if (location.y < minHeight.get() || location.y > maxHeight.get()) return previousResult

		val noise = Random.nextDouble(noiseMin.get(), noiseMax.get())

		return previousResult + (previousResult * noise).roundToInt()
	}
}
