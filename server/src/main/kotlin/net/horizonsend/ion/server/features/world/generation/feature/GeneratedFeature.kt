package net.horizonsend.ion.server.features.world.generation.feature

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.Keyed
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetaData
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetadataFactory
import net.horizonsend.ion.server.features.world.generation.feature.start.FeatureStart
import net.horizonsend.ion.server.features.world.generation.generators.IonWorldGenerator
import net.horizonsend.ion.server.features.world.generation.generators.configuration.feature.FeaturePlacementConfiguration
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.minecraft.core.Holder.Reference
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.levelgen.structure.Structure
import org.bukkit.World
import org.bukkit.generator.ChunkGenerator
import kotlin.random.Random

abstract class GeneratedFeature<T: FeatureMetaData>(override val key: IonRegistryKey<GeneratedFeature<*>, out GeneratedFeature<T>>): Keyed<GeneratedFeature<*>> {
	abstract val placementPriority: Int

	abstract val metaFactory: FeatureMetadataFactory<T>

	abstract fun generateChunk(generator: IonWorldGenerator<*>, chunkPos: ChunkPos, chunkData: ChunkGenerator.ChunkData, start: FeatureStart, metaData: T, minY: Int, maxY: Int)

	val resourceKey: ResourceKey<Structure> = ResourceKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(key.ionNamespacedKey.namespace, key.ionNamespacedKey.key))
	lateinit var ionStructure: Reference<Structure> // by lazy { MinecraftServer.getServer().registryAccess().lookupOrThrow(Registries.STRUCTURE).getValueOrThrow(resourceKey) as IonStructureTypes.IonStructure }

	@Suppress("UNCHECKED_CAST")
	fun castAndGenerateChunk(generator: IonWorldGenerator<*>, chunkPos: ChunkPos, chunkData: ChunkGenerator.ChunkData, start: FeatureStart) = generateChunk(generator, chunkPos, chunkData, start, start.metaData as T)

	fun generateChunk(generator: IonWorldGenerator<*>, chunkPos: ChunkPos, data: ChunkGenerator.ChunkData, start: FeatureStart, metaData: T) {
		val (minPoint, maxPoint) = getExtents(metaData)
		val minY = maxOf(minPoint.y + start.y, generator.heightAccessor.minY)
		val maxY = minOf(maxPoint.y + start.y, generator.heightAccessor.maxY)

		generateChunk(generator, chunkPos, data , start, metaData, minY, maxY)
	}

	/**
	 * Returns min point to max point, relative from the origin of the feature
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
