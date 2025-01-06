package net.horizonsend.ion.server.features.world.generation.generators.configuration

import io.netty.util.internal.ThreadLocalRandom
import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.features.world.generation.GenerationPlacementContext

@Serializable
sealed interface VerticalPlacementConfiguration {
	fun getPlacementY(context: GenerationPlacementContext): Int

	fun Number.clamp(context: GenerationPlacementContext) = toInt().coerceIn(context.worldMinHeight + 1, context.worldMaxHeight)

	@Serializable
	data class RandomInRange(val minY: Int, val maxY: Int): VerticalPlacementConfiguration {
		override fun getPlacementY(context: GenerationPlacementContext): Int {
			return ThreadLocalRandom.current().nextInt(minY, maxY).clamp(context)
		}
	}

	@Serializable
	data class SurfaceRelative(val offset: Int): VerticalPlacementConfiguration {
		override fun getPlacementY(context: GenerationPlacementContext): Int {
			return context.surfaceY.clamp(context)
		}
	}

	@Serializable
	data class SetY(val value: Int): VerticalPlacementConfiguration {
		override fun getPlacementY(context: GenerationPlacementContext): Int {
			return value.clamp(context)
		}
	}

	@Serializable
	data class Guassian(val center: Double, val stdDev: Double): VerticalPlacementConfiguration {
		override fun getPlacementY(context: GenerationPlacementContext): Int {
			return ThreadLocalRandom.current().nextGaussian(center, stdDev).clamp(context)
		}
	}
}
