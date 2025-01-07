package net.horizonsend.ion.server.features.world.generation.generators

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.common.cache.RemovalCause
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.features.world.generation.feature.FeatureRegistry
import net.horizonsend.ion.server.features.world.generation.feature.GeneratedFeature
import net.horizonsend.ion.server.features.world.generation.feature.start.FeatureStart
import net.horizonsend.ion.server.features.world.generation.generators.configuration.GenerationConfiguration
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.util.datafix.DataFixers
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.LevelHeightAccessor
import net.minecraft.world.level.chunk.storage.ChunkStorage
import net.minecraft.world.level.chunk.storage.RegionStorageInfo
import org.bukkit.Chunk
import org.bukkit.NamespacedKey
import java.time.Duration

abstract class IonWorldGenerator<T: GenerationConfiguration>(val world: IonWorld, val configuration: T) {
	val generationMetaDataFolder = world.world.worldFolder.resolve("data/ion/generation_metadata/${world.world.name}").apply { mkdirs() }
	val seed = world.world.seed
	val heightAccessor: LevelHeightAccessor = LevelHeightAccessor.create(world.world.minHeight, world.world.maxHeight - world.world.minHeight)

	private val chunkStorage = ChunkStorage(
		RegionStorageInfo(world.world.key.toString(), world.world.minecraft.dimension(), "chunk"),
		generationMetaDataFolder.toPath(),
		DataFixers.getDataFixer(),
		true
	).`moonrise$getRegionStorage`()

	abstract suspend fun generateChunk(chunk: Chunk)

	companion object {
		const val STATRTS_TAG_NAME = "starts"
		const val REFERENCES_TAG_NAME = "references"
	}

	fun getChunkStructureData(chunk: ChunkPos): ChunkStructureData {
		val tag = chunkStorage.read(chunk) ?: return ChunkStructureData()

		val startsData = tag.getList(STATRTS_TAG_NAME, 10) // 10 = compound
		val starts = startsData.mapTo(mutableListOf()) { tag ->
			FeatureStart.load(tag as CompoundTag)
		}

		val referencesData = tag.getCompound(REFERENCES_TAG_NAME)
		val references = referencesData.allKeys.associateTo(mutableMapOf()) { key ->
			val namespace = NamespacedKey.fromString(key, IonServer) ?: throw IllegalArgumentException("Improperly formatted namespace key")
			val chunkKeys = referencesData.getLongArray(key)

			FeatureRegistry[namespace] to chunkKeys
		}

		return ChunkStructureData(starts, references)
	}

	fun saveStarts(chunk: ChunkPos, starts: MutableList<FeatureStart>) {
		chunkDataCache[chunk].starts = starts
	}

	fun saveReferences(chunk: ChunkPos, references: MutableMap<GeneratedFeature<*>, LongArray>) {
		chunkDataCache[chunk].references = references
	}

	fun addReference(chunk: ChunkPos, feature: GeneratedFeature<*>, holderPos: ChunkPos) {
		val references = chunkDataCache[chunk].references

		if (references.containsKey(feature)) {
			references[feature] = references[feature]!!.plus(holderPos.longKey)
		} else {
			references[feature] = longArrayOf(holderPos.longKey)
		}
	}

	fun saveChunkData(chunkPos: ChunkPos, data: ChunkStructureData) {
		val chunkData = CompoundTag()
		data.saveTo(chunkData)

		chunkStorage.write(chunkPos, chunkData)
	}

	val chunkDataCache: LoadingCache<ChunkPos, ChunkStructureData> = CacheBuilder.newBuilder()
		.expireAfterAccess(Duration.ofMinutes(1L))
		.removalListener<ChunkPos, ChunkStructureData> { notification ->
			if (notification.cause != RemovalCause.EXPIRED) return@removalListener
			saveChunkData(notification.key!!, notification.value!!)
		}
		.build(CacheLoader.from { pos ->
			getChunkStructureData(pos)
		})

	data class ChunkStructureData(
		var starts: MutableList<FeatureStart> = mutableListOf(),
		var references: MutableMap<GeneratedFeature<*>, LongArray> = mutableMapOf()
	) {
		fun saveTo(compound: CompoundTag) {
			val startsData = ListTag()
			for (start in starts) {
				startsData.add(start.save())
			}

			compound.put(STATRTS_TAG_NAME, startsData)

			val referencesData = CompoundTag()
			references.forEach { feature, chunkKeys ->
				referencesData.putLongArray(feature.key.toString(), chunkKeys)
			}
			compound.put(REFERENCES_TAG_NAME, referencesData)
		}
	}
}
