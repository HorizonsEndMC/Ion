package net.horizonsend.ion.server.features.world.generation.generators

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.features.world.generation.feature.FeatureFactoryRegistry
import net.horizonsend.ion.server.features.world.generation.feature.FeatureRegistry
import net.horizonsend.ion.server.features.world.generation.feature.GeneratedFeature
import net.horizonsend.ion.server.features.world.generation.generators.FeatureGenerator.FeatureGenerationData
import net.horizonsend.ion.server.features.world.generation.generators.configuration.GenerationConfiguration
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.chunk.storage.RegionFile
import net.minecraft.world.level.chunk.storage.RegionStorageInfo
import org.bukkit.Chunk
import org.bukkit.NamespacedKey
import java.time.Duration
import kotlin.io.path.Path

abstract class IonWorldGenerator<T: GenerationConfiguration>(val world: IonWorld, val configuration: T) {
	val generationMetaDataFolder = world.world.worldFolder.resolve("data/ion/generation_metadata").apply { mkdirs() }
	val seed = world.world.seed

	abstract suspend fun generateChunk(chunk: Chunk)

	companion object {
		const val STATRTS_TAG_NAME = "starts"
		const val REFERENCES_TAG_NAME = "references"
	}

	fun getChunkStructureData(chunk: ChunkPos): ChunkStructureData {
		val regionFile = regionFileCache[chunk]

		val inputStream = regionFile.getChunkDataInputStream(chunk) ?: return ChunkStructureData()
		val tag = NbtIo.read(inputStream)

		val startsData = tag.getCompound(STATRTS_TAG_NAME) // 10 = compound
		val starts = startsData.allKeys.map { key ->
			val namespace = NamespacedKey.fromString(key, IonServer) ?: throw IllegalArgumentException("Improperly formatted namespace key")

			FeatureFactoryRegistry.load(namespace, startsData.getCompound(key))
		}

		val referencesData = tag.getCompound(REFERENCES_TAG_NAME)
		val references = referencesData.allKeys.associate { key ->
			val namespace = NamespacedKey.fromString(key, IonServer) ?: throw IllegalArgumentException("Improperly formatted namespace key")
			val chunkKeys = referencesData.getLongArray(key)

			FeatureRegistry[namespace] to chunkKeys
		}

		return ChunkStructureData(starts, references)
	}

	data class ChunkStructureData(
		val starts: List<FeatureGenerationData> = listOf(),
		val references: Map<GeneratedFeature, LongArray> = mapOf()
	) {
		fun saveTo(compound: CompoundTag) {
			val startsData = CompoundTag()
			for (start in starts) {
				startsData.put(start.feature.key.toString(), start.placementContext.toCompound())
			}
			compound.put(STATRTS_TAG_NAME, startsData)

			val referencesData = CompoundTag()
			references.forEach { feature, chunkKeys ->
				referencesData.putLongArray(feature.key.toString(), chunkKeys)
			}
			compound.put(REFERENCES_TAG_NAME, referencesData)
		}
	}

	@Synchronized
	fun saveStarts(chunk: ChunkPos, starts: List<FeatureGenerationData>) {
		val adjusted = getChunkStructureData(chunk).copy(starts = starts)
		saveChunkData(chunk, adjusted)
	}

	@Synchronized
	fun saveReferences(chunk: ChunkPos, references: Map<GeneratedFeature, LongArray>) {
		val adjusted = getChunkStructureData(chunk).copy(references = references)
		saveChunkData(chunk, adjusted)
	}

	@Synchronized
	fun saveChunkData(chunkPos: ChunkPos, data: ChunkStructureData) {
		val stream = regionFileCache[chunkPos]
		val chunkData = CompoundTag()
		data.saveTo(chunkData)

		stream.`moonrise$startWrite`(chunkData, chunkPos)
	}

	val regionFileCache: LoadingCache<ChunkPos, RegionFile> = CacheBuilder.newBuilder().weakKeys().expireAfterAccess(Duration.ofMinutes(1L)).build(CacheLoader.from { chunk: ChunkPos ->
		val world = world.world

		RegionFile(
			RegionStorageInfo(world.key.toString(), world.minecraft.dimension(), "chunk"),
			generationMetaDataFolder.toPath(),
			Path(world.key.toString()),
			false
		)
	})
}
