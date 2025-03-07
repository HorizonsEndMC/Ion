package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.ConfigurableAsteroidMeta

@Serializable
sealed interface EvaluationConfiguration {
	fun build(meta: ConfigurableAsteroidMeta): IterativeValueProvider
}

@Serializable
data class SumConfiguration(val values: List<EvaluationConfiguration>) : EvaluationConfiguration {
	override fun build(meta: ConfigurableAsteroidMeta): IterativeValueProvider {
		return Sum(values.map { it.build(meta) })
	}
}

@Serializable
data class AddConfiguration(val a: EvaluationConfiguration, val b: EvaluationConfiguration) : EvaluationConfiguration {
	override fun build(meta: ConfigurableAsteroidMeta): IterativeValueProvider {
		return Add(a.build(meta), b.build(meta))
	}
}

@Serializable
data class SubtractConfiguration(val a: EvaluationConfiguration, val b: EvaluationConfiguration) : EvaluationConfiguration {
	override fun build(meta: ConfigurableAsteroidMeta): IterativeValueProvider {
		return Subtract(a.build(meta), b.build(meta))
	}
}

@Serializable
data class MultiplyConfiguration(val a: EvaluationConfiguration, val b: EvaluationConfiguration) : EvaluationConfiguration {
	override fun build(meta: ConfigurableAsteroidMeta): IterativeValueProvider {
		return Multiply(a.build(meta), b.build(meta))
	}
}

@Serializable
data class DivideConfiguration(val a: EvaluationConfiguration, val b: EvaluationConfiguration) : EvaluationConfiguration {
	override fun build(meta: ConfigurableAsteroidMeta): IterativeValueProvider {
		return Divide(a.build(meta), b.build(meta))
	}
}

@Serializable
data class MinConfiguration(val a: EvaluationConfiguration, val b: EvaluationConfiguration) : EvaluationConfiguration {
	override fun build(meta: ConfigurableAsteroidMeta): IterativeValueProvider {
		return Min(a.build(meta), b.build(meta))
	}
}

@Serializable
data class MaxConfiguration(val a: EvaluationConfiguration, val b: EvaluationConfiguration) : EvaluationConfiguration {
	override fun build(meta: ConfigurableAsteroidMeta): IterativeValueProvider {
		return Max(a.build(meta), b.build(meta))
	}
}

@Serializable
data class StaticConfiguration(val value: Double) : EvaluationConfiguration {
	override fun build(meta: ConfigurableAsteroidMeta): IterativeValueProvider {
		return Static(value)
	}
}

@Serializable
data object SizeConfiguration : EvaluationConfiguration {
	override fun build(meta: ConfigurableAsteroidMeta): IterativeValueProvider {
		return Size
	}
}
