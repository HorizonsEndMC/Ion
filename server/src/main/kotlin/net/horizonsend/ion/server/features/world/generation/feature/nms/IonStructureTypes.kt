package net.horizonsend.ion.server.features.world.generation.feature.nms

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.world.generation.feature.FeatureRegistry
import net.horizonsend.ion.server.features.world.generation.feature.GeneratedFeature
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetaData
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.MappedRegistry
import net.minecraft.core.RegistrationInfo
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.MinecraftServer
import net.minecraft.tags.BiomeTags
import net.minecraft.util.RandomSource
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.StructureManager
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.levelgen.structure.Structure.StructureSettings
import net.minecraft.world.level.levelgen.structure.StructurePiece
import net.minecraft.world.level.levelgen.structure.StructureType
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType.StructureTemplateType
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager
import org.bukkit.NamespacedKey
import java.util.IdentityHashMap
import java.util.Optional

object IonStructureTypes : IonServerComponent() {
	private lateinit var structureType: StructureType<IonStructure>

	override fun onEnable() {
		bootstrapStructureTypes()
		registerStructures()
		registerStructurePiece()
	}

	private fun bootstrapStructureTypes() {
		unfreezeRegistry(BuiltInRegistries.STRUCTURE_TYPE)

		try {
			val registerFunction = StructureType::class.java.getDeclaredMethod("register", String::class.java, MapCodec::class.java)
			registerFunction.isAccessible = true

			// Register feature type
			@Suppress("UNCHECKED_CAST")
			structureType = registerFunction.invoke(null, "ion", IonStructure.CODEC) as StructureType<IonStructure>
			log.info("Successfully registered structure type")
		} catch (e: Throwable) {
			log.warn("Could not register structure type! Feature generation will not function.")
			e.printStackTrace()

//			var cause: Throwable? = e
//
//			while (cause != null) {
//				cause.printStackTrace()
//				cause = cause.cause
//			}

			throw e
		}

		freezeRegistry(BuiltInRegistries.STRUCTURE_TYPE)
	}

	private val unregisteredIntrusiveHoldersField = MappedRegistry::class.java.getDeclaredField("unregisteredIntrusiveHolders")

	private fun registerStructures() {
		unfreezeRegistry(MinecraftServer.getServer().registryAccess().lookupOrThrow(Registries.STRUCTURE))

		log.info("Registering ${FeatureRegistry.features.size} features into the structure registry")

		unregisteredIntrusiveHoldersField.isAccessible = true

		val holderLookup = MinecraftServer.getServer().registryAccess()
		val structureRegistry = holderLookup.lookupOrThrow(Registries.STRUCTURE) as MappedRegistry<Structure>

		unregisteredIntrusiveHoldersField.set(structureRegistry, IdentityHashMap<Structure, Holder.Reference<Structure>>())

		for ((key, feature) in FeatureRegistry.features) try {
			val resourceKey = feature.resourceKey

			val biomes = holderLookup.lookupOrThrow(Registries.BIOME).getOrThrow(BiomeTags.IS_END)
			val structure = IonStructure(StructureSettings(biomes), feature)

			structureRegistry.createIntrusiveHolder(structure)
			feature.ionStructure = structureRegistry.register(resourceKey, structure, RegistrationInfo.BUILT_IN)

			log.info("Successfully registered structure $key into the structure registry")
		} catch (e: Throwable) {
			var cause: Throwable? = e

			while (cause != null) {
				cause.printStackTrace()
				cause = cause.cause
			}
		}

		freezeRegistry(MinecraftServer.getServer().registryAccess().lookupOrThrow(Registries.STRUCTURE))
	}

	private fun registerStructurePiece() {
		unfreezeRegistry(BuiltInRegistries.STRUCTURE_PIECE)

		try {
			unregisteredIntrusiveHoldersField.set(BuiltInRegistries.STRUCTURE_PIECE, IdentityHashMap<Structure, Holder.Reference<Structure>>())

			val registerFunction = StructurePieceType::class.java.getDeclaredMethod(
				"setTemplatePieceId",
				StructureTemplateType::class.java,
				String::class.java
			)
			registerFunction.isAccessible = true

			// Register feature type
			BuiltInRegistries.STRUCTURE_PIECE.createIntrusiveHolder(PieceDataStorage.Type)
			registerFunction.invoke(null, PieceDataStorage.Type, "ion")

			log.info("Successfully registered piece type for feature meta.")
		} catch (e: Throwable) {
			var cause: Throwable? = e

			while (cause != null) {
				cause.printStackTrace()
				cause = cause.cause
			}
		}

		freezeRegistry(BuiltInRegistries.STRUCTURE_PIECE)
	}

	private val frozenField = MappedRegistry::class.java.getDeclaredField("frozen")
	private fun unfreezeRegistry(registry: Registry<*>) {
		try {
			frozenField.isAccessible = true
			frozenField.set(registry, false)
			log.info("Successfully unfroze registry $registry")
		} catch (e: Throwable) {
			log.warn("Could not unfreeze registries! Feature generation will not function.")
			e.printStackTrace()

			throw e
		}
	}

	private fun freezeRegistry(registry: Registry<*>) {
		try {
			frozenField.isAccessible = true
			frozenField.set(registry, true)
			log.info("Successfully froze registry $registry")
		} catch (e: Throwable) {
			log.warn("Could not freeze registries! Feature generation will not function.")
			e.printStackTrace()

			throw e
		}
	}

	class IonStructure(
		settings: StructureSettings,
		val feature: GeneratedFeature<*>,
	) : Structure(settings) {
		override fun findGenerationPoint(context: GenerationContext): Optional<GenerationStub> {
			return Optional.empty()
		}

		override fun type(): StructureType<IonStructure> {
			return structureType
		}

		companion object {
//			val CODEC: MapCodec<IonStructure> = simpleCodec { IonStructure(it) }
			val CODEC: MapCodec<IonStructure> = RecordCodecBuilder.mapCodec { instance ->
				instance.group(
					settingsCodec(instance),
					Codec.string(0, 100).fieldOf("ion_feature").forGetter { structure -> structure.feature.key.toString() }
				).apply(instance) { settings, ionFeatureKey -> IonStructure(settings, FeatureRegistry[NamespacedKey.fromString(ionFeatureKey)!!]) }
			}
		}
	}

	class PieceDataStorage(val pos: Vec3i, val seed: Long, val feature: GeneratedFeature<*>, val metaData: FeatureMetaData) : StructurePiece(Type, 1, BoundingBox.infinite()) {
		override fun addAdditionalSaveData(context: StructurePieceSerializationContext, tag: CompoundTag) {
			tag.putInt("x", pos.x)
			tag.putInt("y", pos.y)
			tag.putInt("z", pos.z)
			tag.putLong("seed", seed)
			tag.putString("feature", feature.key.toString())

			tag.put("meta_data", feature.metaFactory.castAndSave(metaData))
		}

		override fun postProcess(p0: WorldGenLevel, p1: StructureManager, p2: ChunkGenerator, p3: RandomSource, p4: BoundingBox, p5: ChunkPos, p6: BlockPos) {}

		companion object Type : StructureTemplateType {
			override fun load(context: StructureTemplateManager, tag: CompoundTag): StructurePiece {
				val feature = FeatureRegistry[NamespacedKey.fromString(tag.getString("feature"))!!]

				return PieceDataStorage(
					Vec3i(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")),
					tag.getLong("seed"),
					feature,
					feature.metaFactory.load(tag.getCompound("meta_data"))
				)
			}
		}
	}
}
