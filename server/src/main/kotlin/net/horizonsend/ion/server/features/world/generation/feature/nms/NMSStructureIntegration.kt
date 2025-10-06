package net.horizonsend.ion.server.features.world.generation.feature.nms

import com.google.common.collect.ImmutableList
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.WorldGenerationFeatureKeys
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.generation.feature.GeneratedFeature
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetaData
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.MappedRegistry
import net.minecraft.core.RegistrationInfo
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.GenerationChunkHolder
import net.minecraft.tags.BiomeTags
import net.minecraft.util.RandomSource
import net.minecraft.util.StaticCache2D
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.StructureManager
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.chunk.status.ChunkPyramid
import net.minecraft.world.level.chunk.status.ChunkStatus
import net.minecraft.world.level.chunk.status.ChunkStatusTask
import net.minecraft.world.level.chunk.status.ChunkStep
import net.minecraft.world.level.chunk.status.WorldGenContext
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
import sun.misc.Unsafe
import java.lang.reflect.Field
import java.util.IdentityHashMap
import java.util.Optional

object NMSStructureIntegration : IonServerComponent() {
	private lateinit var structureType: StructureType<IonStructure>

	override fun onEnable() {
		bootstrapStructureTypes()
		registerStructures()
		registerStructurePiece()
		registerChunkPyramidIntercept()
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

			throw e
		}

		freezeRegistry(BuiltInRegistries.STRUCTURE_TYPE)
	}

	private val unregisteredIntrusiveHoldersField = MappedRegistry::class.java.getDeclaredField("unregisteredIntrusiveHolders")

	private fun registerStructures() {
		unfreezeRegistry(MinecraftServer.getServer().registryAccess().lookupOrThrow(Registries.STRUCTURE))

		val featureKeys = WorldGenerationFeatureKeys.allkeys()

		log.info("Registering ${featureKeys.size} features into the structure registry")

		unregisteredIntrusiveHoldersField.isAccessible = true

		val holderLookup = MinecraftServer.getServer().registryAccess()
		val structureRegistry = holderLookup.lookupOrThrow(Registries.STRUCTURE) as MappedRegistry<Structure>

		unregisteredIntrusiveHoldersField.set(structureRegistry, IdentityHashMap<Structure, Holder.Reference<Structure>>())

		for (key in featureKeys) try {
			val feature = key.getValue()
			val resourceKey = feature.resourceKey

			val biomes = holderLookup.lookupOrThrow(Registries.BIOME).getOrThrow(BiomeTags.IS_END)
			val structure = IonStructure(StructureSettings(biomes), feature.key)

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
		val feature: IonRegistryKey<GeneratedFeature<*>, out GeneratedFeature<*>>,
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
				).apply(instance) { settings, ionFeatureKey -> IonStructure(settings, WorldGenerationFeatureKeys[NamespacedKey.fromString(ionFeatureKey)!!]!!) }
			}
		}
	}

	class PieceDataStorage(val pos: Vec3i, val feature: GeneratedFeature<*>, val metaData: FeatureMetaData) : StructurePiece(Type, 1, BoundingBox.infinite()) {
		override fun addAdditionalSaveData(context: StructurePieceSerializationContext, tag: CompoundTag) {
			tag.putInt("x", pos.x)
			tag.putInt("y", pos.y)
			tag.putInt("z", pos.z)
			tag.putString("feature", feature.key.ionNapespacedKey.asString())

			tag.put("meta_data", feature.metaFactory.castAndSave(metaData))
		}

		override fun postProcess(p0: WorldGenLevel, p1: StructureManager, p2: ChunkGenerator, p3: RandomSource, p4: BoundingBox, p5: ChunkPos, p6: BlockPos) {}

		companion object Type : StructureTemplateType {
			override fun load(context: StructureTemplateManager, tag: CompoundTag): StructurePiece {
				val namespacedKey = NamespacedKey.fromString(tag.getString("feature")) ?: throw IllegalArgumentException("Invalid namespaced key ${tag.getString("feature")}!")
				val feature = WorldGenerationFeatureKeys[namespacedKey]?.getValue() ?: throw NullPointerException("World generation feature ${namespacedKey.asString()} not found!")

				return PieceDataStorage(
					Vec3i(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")),
					feature,
					feature.metaFactory.load(tag.getCompound("meta_data"))
				)
			}
		}
	}

	private fun registerChunkPyramidIntercept() {
		val clazz = ChunkPyramid::class.java
		val stepsField: Field = clazz.getDeclaredField("steps")
		stepsField.isAccessible = true

		@Suppress("UNCHECKED_CAST")
		val immutable: ImmutableList<ChunkStep> = stepsField.get(ChunkPyramid.GENERATION_PYRAMID) as ImmutableList<ChunkStep>
		val mutable = immutable.toMutableList()

		val existingStepIndex = mutable.indexOfFirst { step -> step.targetStatus() == ChunkStatus.STRUCTURE_STARTS }
		val oldStep = mutable[existingStepIndex]

		val step = ChunkStep(
			oldStep.targetStatus(),
			oldStep.directDependencies(),
			oldStep.accumulatedDependencies(),
			oldStep.blockStateWriteRadius(),
			ChunkStatusTask { context: WorldGenContext, step: ChunkStep, neighborCache: StaticCache2D<GenerationChunkHolder>, chunk: ChunkAccess ->
				try {
					context.level.world.ion.terrainGenerator?.generateStructureStarts(context, step, neighborCache, chunk)
				} catch (e: Throwable) {
					log.warn("Error placing structure starts!")
					e.printStackTrace()
				}

				return@ChunkStatusTask oldStep.task().doWork(context, step, neighborCache, chunk)
			}
		)

		mutable[existingStepIndex] = step

		val newPyramidSteps = ImmutableList.copyOf(mutable)

		val regularUnsafe = unsafe

		val internalUnsafeClazz = Class.forName("jdk.internal.misc.Unsafe")
		val theInternalUnsafeField = Unsafe::class.java.getDeclaredField("theInternalUnsafe")
		theInternalUnsafeField.setAccessible(true)
		val internalUnsafe = theInternalUnsafeField.get(null)

		val objectFieldOffsetMethod = internalUnsafeClazz.getDeclaredMethod("objectFieldOffset", Field::class.java)
		regularUnsafe.putBoolean(objectFieldOffsetMethod, 12, true)

		val offset = objectFieldOffsetMethod.invoke(internalUnsafe, stepsField) as Long

		regularUnsafe.putObject(ChunkPyramid.GENERATION_PYRAMID, offset, newPyramidSteps)

		log.info("Successfully hooked chunk pyramid!")
	}

	private val unsafe: Unsafe get() {
		val unsafeField: Field = Unsafe::class.java.getDeclaredField("theUnsafe")
		unsafeField.setAccessible(true)
		return unsafeField.get(null) as Unsafe
	}
}
