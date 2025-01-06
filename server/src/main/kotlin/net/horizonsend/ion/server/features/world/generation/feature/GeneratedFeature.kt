package net.horizonsend.ion.server.features.world.generation.feature

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.space.data.CompletedSection
import net.horizonsend.ion.server.features.world.generation.WorldGenerationManager
import net.horizonsend.ion.server.features.world.generation.generators.configuration.FeaturePlacementConfiguration
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.NamespacedKey

abstract class GeneratedFeature(val key: NamespacedKey, val placementConfiguration: FeaturePlacementConfiguration) {
	abstract suspend fun generateSection(context: FeaturePlacementContext, sectionMin: Int, sectionMax: Int): CompletedSection

	suspend fun generateChunk(context: FeaturePlacementContext): List<CompletedSection> {
		val (minPoint, maxPoint) = getExtents(context)
		val minY = minPoint.y
		val maxY = maxPoint.y

		val sections = IntRange(
			context.heightAccessor.getSectionIndex(minY),
			context.heightAccessor.getSectionIndex(maxY),
		)

		val deferredSections = mutableListOf<CompletableDeferred<CompletedSection>>()

		for (section: Int in sections) {
			val deferred = CompletableDeferred<CompletedSection>()
			val sectionMin = (section.shl(4) + context.heightAccessor.minY)
			val sectionMax = (section.shl(4) + context.heightAccessor.minY) + 15

			WorldGenerationManager.coroutineScope.launch {
				deferred.complete(generateSection(context, sectionMin, sectionMax))
			}
		}

		return deferredSections.awaitAll()
	}

	/**
	 * Returns min point to max point
	 **/
	abstract fun getExtents(context: FeaturePlacementContext): Pair<Vec3i, Vec3i>

	fun canPlace(): Boolean = true
}
