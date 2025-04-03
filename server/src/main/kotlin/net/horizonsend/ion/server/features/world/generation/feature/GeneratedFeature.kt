package net.horizonsend.ion.server.features.world.generation.feature

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.space.data.CompletedSection
import net.horizonsend.ion.server.features.world.generation.WorldGenerationManager
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetaData
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetadataFactory
import net.horizonsend.ion.server.features.world.generation.feature.start.FeatureStart
import net.horizonsend.ion.server.features.world.generation.generators.IonWorldGenerator
import net.horizonsend.ion.server.features.world.generation.generators.configuration.FeaturePlacementConfiguration
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.minecraft.core.Holder.Reference
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.levelgen.structure.Structure
import org.bukkit.NamespacedKey
import org.bukkit.World
import kotlin.random.Random

abstract class GeneratedFeature<T: FeatureMetaData>(val key: NamespacedKey) {
	abstract val placementPriority: Int

	abstract val metaFactory: FeatureMetadataFactory<T>
	abstract suspend fun generateSection(generator: IonWorldGenerator<*>, chunkPos: ChunkPos, start: FeatureStart, metaData: T, sectionY: Int, sectionMin: Int, sectionMax: Int): CompletedSection

	val resourceKey = ResourceKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(key.namespace, key.key))
	lateinit var ionStructure: Reference<Structure> // by lazy { MinecraftServer.getServer().registryAccess().lookupOrThrow(Registries.STRUCTURE).getValueOrThrow(resourceKey) as IonStructureTypes.IonStructure }

	@Suppress("UNCHECKED_CAST")
	suspend fun castAndGenerateChunk(generator: IonWorldGenerator<*>, chunkPos: ChunkPos, start: FeatureStart): List<CompletedSection> = generateChunk(generator, chunkPos, start, start.metaData as T)

	suspend fun generateChunk(generator: IonWorldGenerator<*>, chunkPos: ChunkPos, start: FeatureStart, metaData: T): List<CompletedSection> {
		val (minPoint, maxPoint) = getExtents(metaData)
		val minY = minPoint.y + start.y
		val maxY = maxPoint.y + start.y

		val sections = IntRange(
			maxOf(generator.heightAccessor.getSectionIndex(minY), generator.heightAccessor.minSectionY),
			minOf(generator.heightAccessor.getSectionIndex(maxY), generator.heightAccessor.maxSectionY),
		)

		val deferredSections = mutableListOf<CompletableDeferred<CompletedSection>>()

		for (section: Int in sections) {
			val deferred = CompletableDeferred<CompletedSection>()
			deferredSections.add(deferred)

			val sectionMin = (section.shl(4) + generator.heightAccessor.minY)
			val sectionMax = (section.shl(4) + generator.heightAccessor.minY) + 15

			WorldGenerationManager.coroutineScope.launch {
				deferred.complete(generateSection(generator, chunkPos, start, metaData, section, sectionMin, sectionMax))
			}
		}

		return deferredSections.awaitAll()
	}

	/**
	 * Returns min point to max point, centered on the placement origin.
	 **/
	abstract fun getExtents(metaData: T): Pair<Vec3i, Vec3i>

	/**
	 * Gets a transformed minimum and maximum chunk
	 **/
	fun getChunkExtents(start: FeatureStart): Pair<ChunkPos, ChunkPos> {
		val (minPoint, maxPoint) = @Suppress("UNCHECKED_CAST") getExtents(start.metaData as T)
		val origin = Vec3i(start.x, start.y, start.z)

		val minAdjusted = minPoint.plus(origin)
		val maxAdjusted = maxPoint.plus(origin)

		val pair = ChunkPos(minAdjusted.x.shr(4), minAdjusted.z.shr(4)) to ChunkPos(maxAdjusted.x.shr(4), maxAdjusted.z.shr(4))
		return pair
	}

	fun buildStartsData(world: World, chunkPos: ChunkPos, random: Random, placementConfiguration: FeaturePlacementConfiguration<T>): List<FeatureStart> {
		return placementConfiguration.generatePlacements(world, chunkPos, random).map { (context, meta) ->
			FeatureStart(
				this,
				context.x,
				context.y,
				context.z,
				meta
			)
		}
	}

	fun canPlace(): Boolean = true
}
