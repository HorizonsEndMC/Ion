package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid

import kotlin.math.min

enum class BlendMode {
	ADD {
		override fun apply(previous: Float, noise: Float): Float {
			return previous + noise
		}
	},
	SUBTRACT {
		override fun apply(previous: Float, noise: Float): Float {
			return previous - noise
		}
	},
	MULTIPLY {
		override fun apply(previous: Float, noise: Float): Float {
			return previous * noise
		}
	},
	DIVIDE {
		override fun apply(previous: Float, noise: Float): Float {
			return previous / noise
		}
	},
	MAX {
		override fun apply(previous: Float, noise: Float): Float {
			return maxOf(previous, noise)
		}
	},
	MIN {
		override fun apply(previous: Float, noise: Float): Float {
			return min(previous, noise)
		}
	},

	;

	abstract fun apply(previous: Float, noise: Float): Float
}
