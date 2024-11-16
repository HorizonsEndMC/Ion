package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.miscellaneous.utils.weightedRandom
import java.util.function.Supplier
import kotlin.random.Random

@Serializable
sealed interface IntegerAmount : Supplier<Int>

@Serializable
data class VariableIntegerAmount(
	val lowerBound: Int,
	val upperBound: Int
) : IntegerAmount {
	override fun get(): Int = Random.nextInt(lowerBound, upperBound)
}

@Serializable
data class StaticIntegerAmount(
	val value: Int
) : IntegerAmount {
	override fun get(): Int = value
}

@Serializable
data class WeightedIntegerAmount(
	val items: Set<Pair<Int,Double>>
) : IntegerAmount {
	override fun get(): Int = items.weightedRandom { it.second }.first
}

@Serializable
sealed interface LongAmount : Supplier<Long>

@Serializable
data class VariableLongAmount(
	val lowerBound: Long,
	val upperBound: Long
) : LongAmount {
	override fun get(): Long = Random.nextLong(lowerBound, upperBound)
}

@Serializable
data class StaticLongAmount(
	val value: Long
) : LongAmount {
	override fun get(): Long = value
}


@Serializable
sealed interface DoubleAmount : Supplier<Double>

@Serializable
data class VariableDoubleAmount(
	val lowerBound: Double,
	val upperBound: Double
) : DoubleAmount {
	override fun get(): Double = Random.nextDouble(lowerBound, upperBound)
}

@Serializable
data class StaticDoubleAmount(
	val value: Double
) : DoubleAmount {
	override fun get(): Double = value
}

@Serializable
sealed interface FloatAmount : Supplier<Float>

@Serializable
data class VariableFloatAmount(
	val lowerBound: Float,
	val upperBound: Float
) : FloatAmount {
	override fun get(): Float = (Random.nextFloat() * (upperBound - lowerBound)) - lowerBound
}

@Serializable
data class StaticFloatAmount(
	val value: Float
) : FloatAmount {
	override fun get(): Float = value
}
