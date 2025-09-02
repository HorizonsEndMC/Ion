package net.horizonsend.ion.server.features.multiblock.type.fluid

import io.papermc.paper.registry.keys.BiomeKeys
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.core.registration.keys.FluidPropertyTypeKeys
import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.MATCH_SIGN_FONT_SIZE
import net.horizonsend.ion.server.features.client.display.modular.display.fluid.SplitFluidDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.getLinePos
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidInputMetadata
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidRestriction
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.fluid.PumpMultiblock.PumpMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.starship.destruction.SinkAnimation
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.LITERS_IN_BLOCK
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty
import net.horizonsend.ion.server.features.transport.inputs.IOData
import net.horizonsend.ion.server.features.transport.inputs.IOPort
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.isChiseled
import net.horizonsend.ion.server.miscellaneous.utils.isLava
import net.horizonsend.ion.server.miscellaneous.utils.isWater
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Levelled
import org.bukkit.block.data.Waterlogged
import org.bukkit.block.data.type.Stairs
import org.bukkit.persistence.PersistentDataAdapterContext
import kotlin.math.abs

object PumpMultiblock : Multiblock(), EntityMultiblock<PumpMultiblockEntity> {
	override val name: String = "pump"

	override val signText: Array<Component?> = createSignText(
		Component.text("Pump"),
		null,
		null,
		null,
	)

	override fun MultiblockShape.buildStructure() {
		z(2) {
			y(-1) {
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(1).titaniumBlock()
				x(0).titaniumBlock()
				x(-1).titaniumBlock()
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(0) {
				x(2).anyGlassPane(PrepackagedPreset.pane(RelativeFace.LEFT))
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.BACKWARD, RelativeFace.LEFT))
				x(0).anyCopperBulb()
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.BACKWARD, RelativeFace.LEFT))
				x(-2).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT))
			}
		}
		z(1) {
			y(-1) {
				x(2).fluidPort()
				x(1).redstoneBlock()
				x(0).anyCopperGrate()
				x(-1).redstoneBlock()
				x(-2).fluidPort()
			}
			y(0) {
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.LEFT, example = Material.GRINDSTONE.createBlockData()))
				x(0).anyCopperGrate()
				x(-1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.RIGHT, example = Material.GRINDSTONE.createBlockData()))
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
		z(0) {
			y(-1) {
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(1).titaniumBlock()
				x(0).powerInput()
				x(-1).titaniumBlock()
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(0) {
				x(2).anyGlassPane(PrepackagedPreset.pane(RelativeFace.LEFT))
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.LEFT, RelativeFace.FORWARD))
				x(0).anyCopperBulb()
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.LEFT, RelativeFace.FORWARD))
				x(-2).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT))
			}
		}
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): PumpMultiblockEntity {
		return PumpMultiblockEntity(data, manager, world, x, y, z, structureDirection)
	}

	class PumpMultiblockEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		world: World,
		x: Int,
		y: Int,
		z: Int,
		structureDirection: BlockFace
	) : MultiblockEntity(manager, PumpMultiblock, world, x, y, z, structureDirection), DisplayMultiblockEntity, FluidStoringMultiblock, AsyncTickingMultiblockEntity {
		override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(5)

		override val ioData: IOData = IOData.Companion.builder(this)
			// Input
			.addPowerInput(0, -1, 0)
			// Output
			.addPort(IOType.FLUID, 2, -1, 1) { IOPort.RegisteredMetaDataInput<FluidInputMetadata>(this, FluidInputMetadata(connectedStore = mainStorage, inputAllowed = false, outputAllowed = true)) }
			.addPort(IOType.FLUID, -2, -1, 1) { IOPort.RegisteredMetaDataInput<FluidInputMetadata>(this, FluidInputMetadata(connectedStore = mainStorage, inputAllowed = false, outputAllowed = true)) }
			.build()

		val mainStorage = FluidStorageContainer(data, "main_storage", Component.text("Main Storage"), NamespacedKeys.MAIN_STORAGE, 1_000.0, FluidRestriction.Unlimited)

		override val displayHandler: TextDisplayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			{ SplitFluidDisplayModule(handler = it, storage = mainStorage, offsetLeft = 0.0, offsetUp = getLinePos(4), offsetBack = 0.0, scale = MATCH_SIGN_FONT_SIZE) },
		)

		override fun getStores(): List<FluidStorageContainer> {
			return listOf(mainStorage)
		}

		override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
			saveStorageData(store)
		}

		override fun tickAsync() {
			bootstrapNetwork()
			tryPumpFluid()
		}

		companion object {
			private val TUBE_ORIGIN = Vec3i(0, -2, 1)

			/** Pump rate, in liters per second **/
			private const val PUMP_RATE = 1.0

			/** The maximum depth of pumping where salinity is increased **/
			private const val MAX_DEPTH_SALINITY_BONUS = 30

			/** The maximum salinity achievable **/
			private const val MAX_SALINITY = 0.05

			private val OCEAN_BIOMES = setOf(
				BiomeKeys.OCEAN,
				BiomeKeys.DEEP_OCEAN,
				BiomeKeys.WARM_OCEAN,
				BiomeKeys.LUKEWARM_OCEAN,
				BiomeKeys.DEEP_LUKEWARM_OCEAN,
				BiomeKeys.COLD_OCEAN,
				BiomeKeys.DEEP_COLD_OCEAN,
				BiomeKeys.FROZEN_OCEAN,
				BiomeKeys.DEEP_FROZEN_OCEAN,
			).mapTo(mutableSetOf()) { it.key() }

			private const val MAX_RANGE = 30

			fun isFullLavaSource(data: BlockData): Boolean {
				if (data.material != Material.LAVA) return false
				if (data !is Levelled) return false
				if (data.level != data.minimumLevel) return false
				return true
			}
		}

		private fun getPumpOrigin(): Block = getBlockRelative(TUBE_ORIGIN.x, TUBE_ORIGIN.y, TUBE_ORIGIN.z)

		private fun tryPumpFluid() {
			val delta = deltaTMS / 1000.0

			val pumpOriginBlock = getPumpOrigin()
			val type = pumpOriginBlock.type

			when {
				type == Material.LIGHTNING_ROD -> tryPumpWater(pumpOriginBlock, delta)
				type.isChiseled -> tryPumpLava(pumpOriginBlock, type)
			}
		}

		fun tryPumpWater(pumpOriginBlock: Block, delta: Double) {
			val (surfaceDepth, fluidDepth) = getWaterDepth()

			if (fluidDepth <= 0) return

			val stack = FluidStack(FluidTypeKeys.WATER, PUMP_RATE * delta)

			val bottomBlock = pumpOriginBlock.getRelative(BlockFace.DOWN, surfaceDepth + fluidDepth)
			val biome = world.getBiome(bottomBlock.x, bottomBlock.y, bottomBlock.z)
			if (OCEAN_BIOMES.contains(biome.key())) {
				// Ramp up salinity to
				val prog = minOf(fluidDepth, MAX_DEPTH_SALINITY_BONUS).toDouble() / MAX_DEPTH_SALINITY_BONUS.toDouble()
				val salinity = SinkAnimation.blend(0.0, MAX_SALINITY, prog)

				stack.setData(FluidPropertyTypeKeys.SALINITY.getValue(), FluidProperty.Salinity(salinity))
			}

			mainStorage.addFluid(stack, bottomBlock.location)
		}

		/**
		 * Returns the depth of fluid which is pumped
		 *
		 * Returns a pair of integers, the first being the water surface depth, the second beind the depth of the water.
		 **/
		fun getWaterDepth(): Pair<Int, Int> {
			var fluidFound = false
			var surfaceDepth = 0
			var fluidDepth = 0

			var block = getPumpOrigin()
			var data = block.blockData

			while (data.material == Material.LIGHTNING_ROD) {
				// Return the depth if the block is in water & surrounded by water
				if (data is Waterlogged && data.isWaterlogged) {
					if (CARDINAL_BLOCK_FACES.all { face ->
						val data = block.getRelative(face).blockData
						data.material.isWater || (data is Waterlogged && data.isWaterlogged)
					}) {
						fluidFound = true
						fluidDepth++
					}
				}

				// If not, search deeper
				block = block.getRelative(BlockFace.DOWN)
				data = block.blockData
				if (!fluidFound) surfaceDepth++
			}

			return surfaceDepth to fluidDepth
		}

		/**
		 * Tries to pump lava by removing blocks. If a source block cannot be removed, it will not be pumped.
		 **/
		private fun tryPumpLava(pumpOriginBlock: Block, type: Material) {
			val stack = FluidStack(FluidTypeKeys.LAVA, LITERS_IN_BLOCK)
			stack.setData(FluidPropertyTypeKeys.TEMPERATURE.getValue(), FluidProperty.Temperature(1000.0))

			if (!mainStorage.canAdd(stack)) return
			if (!mainStorage.hasRoomFor(stack)) return

			val surfaceDepth = getLavaSurface(pumpOriginBlock, type) ?: return

			val surfaceOrigin = pumpOriginBlock.getRelative(BlockFace.DOWN, surfaceDepth)

			val planeBlocks = getSurfaceLayerBlocks(surfaceOrigin)
			if (planeBlocks.isEmpty()) return
			Tasks.sync {
				val last = planeBlocks.reversed().firstOrNull { block -> block != pumpOriginBlock } ?: return@sync
				debugAudience.highlightBlock(Vec3i(last.location), 30L)

				if (!last.type.isLava) return@sync
				last.type = Material.AIR

				Tasks.async {
					mainStorage.addFluid(stack, surfaceOrigin.location)
				}
			}
		}

		/**
		 * Returns the depth of fluid which is pumped
		 *
		 * Returns the lava surface depth, or null if lava is never found
		 **/
		private fun getLavaSurface(pumpOriginBlock: Block, material: Material): Int? {
			var surfaceDepth = 0

			var block = pumpOriginBlock
			var data = block.blockData

			while (data.material == material) {
				// Return the depth if the block is adjacent to lava
				// Since this isn't correlated to new water sources are being created, we only need one adjacent source
				if (CARDINAL_BLOCK_FACES.any { face -> block.getRelativeIfLoaded(face)?.blockData?.let(::isFullLavaSource) == true }) {
					return surfaceDepth
				}

				// If not, search deeper
				block = block.getRelative(BlockFace.DOWN)
				data = block.blockData
				surfaceDepth++
			}

			return null
		}

		fun getSurfaceLayerBlocks(origin: Block): List<Block> {
			val visitQueue = ArrayDeque<Block>()
			// A set is maintained to allow faster checks of
			val visitSet = LongOpenHashSet()

			visitQueue.add(origin)
			visitSet.add(toBlockKey(origin.x, origin.y, origin.z))

			val visited = mutableListOf<Block>()

			var tick = 0

			while (visitQueue.isNotEmpty() && tick < 10000 && isAlive) whileLoop@{
				tick++
				val block = visitQueue.removeFirst()

				visited.add(block)

				for (face in CARDINAL_BLOCK_FACES) {
					val adjacent = block.getRelativeIfLoaded(face) ?: continue

					// if within range
					if (abs(adjacent.x - origin.x) > MAX_RANGE || abs(adjacent.z - origin.z) > MAX_RANGE) continue

					val adjacentData = adjacent.blockData
					if (!isFullLavaSource(adjacentData)) continue

					if (visitSet.add(toBlockKey(adjacent.x, adjacent.y, adjacent.z))) visitQueue.add(adjacent)
				}
			}

			return visited
		}
	}
}
