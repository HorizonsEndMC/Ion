package net.horizonsend.ion.server.features.world.generation.generators

import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.features.world.generation.feature.FeaturePlacementContext
import net.horizonsend.ion.server.features.world.generation.feature.GeneratedFeature
import net.horizonsend.ion.server.features.world.generation.generators.configuration.FeatureConfiguration
import net.horizonsend.ion.server.features.world.generation.generators.configuration.FeatureGeneratorConfiguration
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import org.bukkit.Chunk

class FeatureGenerator(world: IonWorld, configuration: FeatureGeneratorConfiguration) : IonWorldGenerator<FeatureGeneratorConfiguration>(world, configuration) {
	val features = configuration.features.map(FeatureConfiguration::loadFeature)

	override suspend fun generateChunk(chunk: Chunk) {
		val nmsChunk = chunk.minecraft

		val toGenerate = getStructureGenerationData(chunk)

		val sections = toGenerate
			.associateWith { (feature, context) -> feature.generateChunk(context) }
			.entries.sortedBy { it.key.feature.placementConfiguration.placementPriority }

		sections.forEach { t ->  t.value.forEach { section -> section.place(nmsChunk) } }
	}

	fun getStructureGenerationData(chunk: Chunk): List<FeatureGenerationData> {
		val starts = mutableListOf<FeatureGenerationData>()

		for (feature in features.filter { feature -> feature.canPlace() }) {
			val count = feature.placementConfiguration.getCount()
			repeat(count) {
				starts.add(FeatureGenerationData(feature, feature.placementConfiguration.generatePlacementContext(this, chunk)))
			}
		}

		return starts
	}

	data class FeatureGenerationData(val feature: GeneratedFeature, val placementContext: FeaturePlacementContext)
}
