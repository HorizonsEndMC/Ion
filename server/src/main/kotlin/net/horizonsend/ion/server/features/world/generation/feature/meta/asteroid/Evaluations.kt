package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid

import kotlinx.serialization.Serializable

@Serializable
data class Sum(val values: List<IterativeValueProvider>) : IterativeValueProvider {
	override fun getFallbackValue(meta: ConfigurableAsteroidMeta): Double {
		return values.sumOf { it.getFallbackValue(meta) }
	}

	override fun getValue(x: Double, y: Double, z: Double, meta: ConfigurableAsteroidMeta): Double {
		return values.sumOf { it.getValue(x, y, z, meta) }
	}

	override fun getValue(x: Double, z: Double, meta: ConfigurableAsteroidMeta): Double {
		return values.sumOf { it.getValue(x, z, meta) }
	}
}

@Serializable
data class Add(val a: IterativeValueProvider, val b: IterativeValueProvider) : IterativeValueProvider {
	override fun getFallbackValue(meta: ConfigurableAsteroidMeta): Double {
		return a.getFallbackValue(meta) + b.getFallbackValue(meta)
	}

	override fun getValue(x: Double, y: Double, z: Double, meta: ConfigurableAsteroidMeta): Double {
		return a.getValue(x, y, z, meta) + b.getValue(x, y, z, meta)
	}

	override fun getValue(x: Double, z: Double, meta: ConfigurableAsteroidMeta): Double {
		return a.getValue(x, z, meta) + b.getValue(x, z, meta)
	}
}

@Serializable
data class Subtract(val a: IterativeValueProvider, val b: IterativeValueProvider) : IterativeValueProvider {
	override fun getFallbackValue(meta: ConfigurableAsteroidMeta): Double {
		return a.getFallbackValue(meta) - b.getFallbackValue(meta)
	}

	override fun getValue(x: Double, y: Double, z: Double, meta: ConfigurableAsteroidMeta): Double {
		return a.getValue(x, y, z, meta) - b.getValue(x, y, z, meta)
	}

	override fun getValue(x: Double, z: Double, meta: ConfigurableAsteroidMeta): Double {
		return a.getValue(x, z, meta) - b.getValue(x, z, meta)
	}
}

@Serializable
data class Multiply(val a: IterativeValueProvider, val b: IterativeValueProvider) : IterativeValueProvider {
	override fun getFallbackValue(meta: ConfigurableAsteroidMeta): Double {
		return a.getFallbackValue(meta) * b.getFallbackValue(meta)
	}

	override fun getValue(x: Double, y: Double, z: Double, meta: ConfigurableAsteroidMeta): Double {
		return a.getValue(x, y, z, meta) * b.getValue(x, y, z, meta)
	}

	override fun getValue(x: Double, z: Double, meta: ConfigurableAsteroidMeta): Double {
		return a.getValue(x, z, meta) * b.getValue(x, z, meta)
	}
}

@Serializable
data class Divide(val a: IterativeValueProvider, val b: IterativeValueProvider) : IterativeValueProvider {
	override fun getFallbackValue(meta: ConfigurableAsteroidMeta): Double {
		return a.getFallbackValue(meta) / b.getFallbackValue(meta)
	}

	override fun getValue(x: Double, y: Double, z: Double, meta: ConfigurableAsteroidMeta): Double {
		return a.getValue(x, y, z, meta) / b.getValue(x, y, z, meta)
	}

	override fun getValue(x: Double, z: Double, meta: ConfigurableAsteroidMeta): Double {
		return a.getValue(x, z, meta) / b.getValue(x, z, meta)
	}
}

@Serializable
data class Min(val a: IterativeValueProvider, val b: IterativeValueProvider) : IterativeValueProvider {
	override fun getFallbackValue(meta: ConfigurableAsteroidMeta): Double {
		return minOf(a.getFallbackValue(meta), b.getFallbackValue(meta))
	}

	override fun getValue(x: Double, y: Double, z: Double, meta: ConfigurableAsteroidMeta): Double {
		return minOf(a.getValue(x, y, z, meta), b.getValue(x, y, z, meta))
	}

	override fun getValue(x: Double, z: Double, meta: ConfigurableAsteroidMeta): Double {
		return minOf(a.getValue(x, z, meta), b.getValue(x, z, meta))
	}
}

@Serializable
data class Max(val a: IterativeValueProvider, val b: IterativeValueProvider) : IterativeValueProvider {
	override fun getFallbackValue(meta: ConfigurableAsteroidMeta): Double {
		return maxOf(a.getFallbackValue(meta), b.getFallbackValue(meta))
	}

	override fun getValue(x: Double, y: Double, z: Double, meta: ConfigurableAsteroidMeta): Double {
		return maxOf(a.getValue(x, y, z, meta), b.getValue(x, y, z, meta))
	}

	override fun getValue(x: Double, z: Double, meta: ConfigurableAsteroidMeta): Double {
		return maxOf(a.getValue(x, z, meta), b.getValue(x, z, meta))
	}
}

@Serializable
data class Static(val value: Double) : IterativeValueProvider {
	override fun getFallbackValue(meta: ConfigurableAsteroidMeta): Double = value
	override fun getValue(x: Double, z: Double, meta: ConfigurableAsteroidMeta): Double = value
	override fun getValue(x: Double, y: Double, z: Double, meta: ConfigurableAsteroidMeta): Double = value
}

@Serializable
data object Size : IterativeValueProvider {
	override fun getFallbackValue(meta: ConfigurableAsteroidMeta): Double = meta.size
	override fun getValue(x: Double, z: Double, meta: ConfigurableAsteroidMeta): Double = meta.size
	override fun getValue(x: Double, y: Double, z: Double, meta: ConfigurableAsteroidMeta): Double  = meta.size
}
