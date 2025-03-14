package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise

import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.ConfigurableAsteroidMeta
import net.horizonsend.ion.server.features.world.generation.feature.start.FeatureStart

data class Sum(val values: List<IterativeValueProvider>) : IterativeValueProvider {
	override fun getFallbackValue(meta: ConfigurableAsteroidMeta): Double {
		return values.sumOf { it.getFallbackValue(meta) }
	}

	override fun getValue(x: Double, y: Double, z: Double, meta: ConfigurableAsteroidMeta, start: FeatureStart): Double {
		return values.sumOf { it.getValue(x, y, z, meta, start) }
	}

}

data class Add(val a: IterativeValueProvider, val b: IterativeValueProvider) : IterativeValueProvider {
	override fun getFallbackValue(meta: ConfigurableAsteroidMeta): Double {
		return a.getFallbackValue(meta) + b.getFallbackValue(meta)
	}

	override fun getValue(x: Double, y: Double, z: Double, meta: ConfigurableAsteroidMeta, start: FeatureStart): Double {
		return a.getValue(x, y, z, meta, start) + b.getValue(x, y, z, meta, start)
	}

}

data class Subtract(val a: IterativeValueProvider, val b: IterativeValueProvider) : IterativeValueProvider {
	override fun getFallbackValue(meta: ConfigurableAsteroidMeta): Double {
		return a.getFallbackValue(meta) - b.getFallbackValue(meta)
	}

	override fun getValue(x: Double, y: Double, z: Double, meta: ConfigurableAsteroidMeta, start: FeatureStart): Double {
		return a.getValue(x, y, z, meta, start) - b.getValue(x, y, z, meta, start)
	}

}

data class Multiply(val a: IterativeValueProvider, val b: IterativeValueProvider) : IterativeValueProvider {
	override fun getFallbackValue(meta: ConfigurableAsteroidMeta): Double {
		return a.getFallbackValue(meta) * b.getFallbackValue(meta)
	}

	override fun getValue(x: Double, y: Double, z: Double, meta: ConfigurableAsteroidMeta, start: FeatureStart): Double {
		return a.getValue(x, y, z, meta, start) * b.getValue(x, y, z, meta, start)
	}

}

data class Divide(val a: IterativeValueProvider, val b: IterativeValueProvider) : IterativeValueProvider {
	override fun getFallbackValue(meta: ConfigurableAsteroidMeta): Double {
		return a.getFallbackValue(meta) / b.getFallbackValue(meta)
	}

	override fun getValue(x: Double, y: Double, z: Double, meta: ConfigurableAsteroidMeta, start: FeatureStart): Double {
		return a.getValue(x, y, z, meta, start) / b.getValue(x, y, z, meta, start)
	}

}

data class Min(val a: IterativeValueProvider, val b: IterativeValueProvider) : IterativeValueProvider {
	override fun getFallbackValue(meta: ConfigurableAsteroidMeta): Double {
		return minOf(a.getFallbackValue(meta,), b.getFallbackValue(meta))
	}

	override fun getValue(x: Double, y: Double, z: Double, meta: ConfigurableAsteroidMeta, start: FeatureStart): Double {
		return minOf(a.getValue(x, y, z, meta, start), b.getValue(x, y, z, meta, start))
	}

}

data class Max(val a: IterativeValueProvider, val b: IterativeValueProvider) : IterativeValueProvider {
	override fun getFallbackValue(meta: ConfigurableAsteroidMeta): Double {
		return maxOf(a.getFallbackValue(meta), b.getFallbackValue(meta))
	}

	override fun getValue(x: Double, y: Double, z: Double, meta: ConfigurableAsteroidMeta, start: FeatureStart): Double {
		return maxOf(a.getValue(x, y, z, meta, start), b.getValue(x, y, z, meta, start))
	}

}

data class Static(val value: Double) : IterativeValueProvider {
	override fun getFallbackValue(meta: ConfigurableAsteroidMeta): Double = value
	override fun getValue(x: Double, y: Double, z: Double, meta: ConfigurableAsteroidMeta, start: FeatureStart): Double = value
}

data object Size : IterativeValueProvider {
	override fun getFallbackValue(meta: ConfigurableAsteroidMeta): Double = meta.size
	override fun getValue(x: Double, y: Double, z: Double, meta: ConfigurableAsteroidMeta, start: FeatureStart): Double  = meta.size
}
