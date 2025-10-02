package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise

import kotlinx.serialization.Serializable

@Serializable
sealed interface GlobalEvaluationConfiguration {
	fun build(seed: Long): IterativeValueProvider
}

@Serializable
data class SumConfigurationGlobal(val values: List<GlobalEvaluationConfiguration>) : GlobalEvaluationConfiguration {
	override fun build(seed: Long): IterativeValueProvider {
		return Sum(values.map { it.build(seed) })
	}
}

@Serializable
data class AddConfigurationGlobal(val a: GlobalEvaluationConfiguration, val b: GlobalEvaluationConfiguration) : GlobalEvaluationConfiguration {
	override fun build(seed: Long): IterativeValueProvider {
		return Add(a.build(seed), b.build(seed))
	}
}

@Serializable
data class SubtractConfigurationGlobal(val a: GlobalEvaluationConfiguration, val b: GlobalEvaluationConfiguration) : GlobalEvaluationConfiguration {
	override fun build(seed: Long): IterativeValueProvider {
		return Subtract(a.build(seed), b.build(seed))
	}
}

@Serializable
data class MultiplyConfigurationGlobal(val a: GlobalEvaluationConfiguration, val b: GlobalEvaluationConfiguration) : GlobalEvaluationConfiguration {
	override fun build(seed: Long): IterativeValueProvider {
		return Multiply(a.build(seed), b.build(seed))
	}
}

@Serializable
data class DivideConfigurationGlobal(val a: GlobalEvaluationConfiguration, val b: GlobalEvaluationConfiguration) : GlobalEvaluationConfiguration {
	override fun build(seed: Long): IterativeValueProvider {
		return Divide(a.build(seed), b.build(seed))
	}
}

@Serializable
data class MinConfigurationGlobal(val a: GlobalEvaluationConfiguration, val b: GlobalEvaluationConfiguration) : GlobalEvaluationConfiguration {
	override fun build(seed: Long): IterativeValueProvider {
		return Min(a.build(seed), b.build(seed))
	}
}

@Serializable
data class MaxConfigurationGlobal(val a: GlobalEvaluationConfiguration, val b: GlobalEvaluationConfiguration) : GlobalEvaluationConfiguration {
	override fun build(seed: Long): IterativeValueProvider {
		return Max(a.build(seed), b.build(seed))
	}
}

@Serializable
data class StaticConfigurationGlobal(val value: Double) : GlobalEvaluationConfiguration {
	override fun build(seed: Long): IterativeValueProvider {
		return Static(value)
	}
}

@Serializable
data class ThresholdConfigurationGlobal(
	val a: GlobalEvaluationConfiguration,
	val b: GlobalEvaluationConfiguration,
	val threshold: GlobalEvaluationConfiguration,
	val selector: GlobalEvaluationConfiguration,
) : GlobalEvaluationConfiguration {
	override fun build(seed: Long): IterativeValueProvider {
		return Threshhold(a.build(seed), b.build(seed), threshold.build(seed), selector.build(seed))
	}
}
