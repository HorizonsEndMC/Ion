package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise

import net.horizonsend.ion.server.miscellaneous.utils.Vec3i

data class Sum(val values: List<IterativeValueProvider>) : IterativeValueProvider {
	override fun getFallbackValue(): Double {
		return values.sumOf { it.getFallbackValue() }
	}

	override fun getValue(x: Double, y: Double, z: Double, origin: Vec3i): Double {
		return values.sumOf { it.getValue(x, y, z, origin) }
	}

}

data class Add(val a: IterativeValueProvider, val b: IterativeValueProvider) : IterativeValueProvider {
	override fun getFallbackValue(): Double {
		return a.getFallbackValue() + b.getFallbackValue()
	}

	override fun getValue(x: Double, y: Double, z: Double, origin: Vec3i): Double {
		return a.getValue(x, y, z, origin) + b.getValue(x, y, z, origin)
	}

}

data class Subtract(val a: IterativeValueProvider, val b: IterativeValueProvider) : IterativeValueProvider {
	override fun getFallbackValue(): Double {
		return a.getFallbackValue() - b.getFallbackValue()
	}

	override fun getValue(x: Double, y: Double, z: Double, origin: Vec3i): Double {
		return a.getValue(x, y, z, origin) - b.getValue(x, y, z, origin)
	}

}

data class Multiply(val a: IterativeValueProvider, val b: IterativeValueProvider) : IterativeValueProvider {
	override fun getFallbackValue(): Double {
		return a.getFallbackValue() * b.getFallbackValue()
	}

	override fun getValue(x: Double, y: Double, z: Double, origin: Vec3i): Double {
		return a.getValue(x, y, z, origin) * b.getValue(x, y, z, origin)
	}

}

data class Divide(val a: IterativeValueProvider, val b: IterativeValueProvider) : IterativeValueProvider {
	override fun getFallbackValue(): Double {
		return a.getFallbackValue() / b.getFallbackValue()
	}

	override fun getValue(x: Double, y: Double, z: Double, origin: Vec3i): Double {
		return a.getValue(x, y, z, origin) / b.getValue(x, y, z, origin)
	}

}

data class Min(val a: IterativeValueProvider, val b: IterativeValueProvider) : IterativeValueProvider {
	override fun getFallbackValue(): Double {
		return minOf(a.getFallbackValue(), b.getFallbackValue())
	}

	override fun getValue(x: Double, y: Double, z: Double, origin: Vec3i): Double {
		return minOf(a.getValue(x, y, z, origin), b.getValue(x, y, z, origin))
	}

}

data class Max(val a: IterativeValueProvider, val b: IterativeValueProvider) : IterativeValueProvider {
	override fun getFallbackValue(): Double {
		return maxOf(a.getFallbackValue(), b.getFallbackValue())
	}

	override fun getValue(x: Double, y: Double, z: Double, origin: Vec3i): Double {
		return maxOf(a.getValue(x, y, z, origin), b.getValue(x, y, z, origin))
	}

}

data class Static(val value: Double) : IterativeValueProvider {
	override fun getFallbackValue(): Double = value
	override fun getValue(x: Double, y: Double, z: Double, origin: Vec3i): Double = value
}

