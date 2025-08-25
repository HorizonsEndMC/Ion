package net.horizonsend.ion.server.features.multiblock.type.fluid

import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.MATCH_SIGN_FONT_SIZE
import net.horizonsend.ion.server.features.client.display.modular.display.fluid.ComplexFluidDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.getLinePos
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidInputMetadata
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidRestriction
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.fluid.PumpMultiblock.PumpMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.transport.inputs.IOData
import net.horizonsend.ion.server.features.transport.inputs.IOPort
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs
import org.bukkit.persistence.PersistentDataAdapterContext

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
				x(2).fluidInput()
				x(1).redstoneBlock()
				x(0).anyCopperGrate()
				x(-1).redstoneBlock()
				x(-2).fluidInput()
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
	) : MultiblockEntity(manager, PumpMultiblock, world, x, y, z, structureDirection), DisplayMultiblockEntity, FluidStoringMultiblock, SyncTickingMultiblockEntity {
		override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(20)

		override val ioData: IOData = IOData.Companion.builder(this)
			// Input
			.addPowerInput(0, -1, 0)
			// Output
			.addPort(IOType.FLUID, 2, -1, 1) { IOPort.RegisteredMetaDataInput<FluidInputMetadata>(this, FluidInputMetadata(connectedStore = mainStorage, inputAllowed = false, outputAllowed = true)) }
			.addPort(IOType.FLUID, -2, -1, 1) { IOPort.RegisteredMetaDataInput<FluidInputMetadata>(this, FluidInputMetadata(connectedStore = mainStorage, inputAllowed = false, outputAllowed = true)) }
			.build()

		val mainStorage = FluidStorageContainer(data, "main_storage", Component.text("Main Storage"), NamespacedKeys.MAIN_STORAGE, 100_000.0, FluidRestriction.Unlimited)

		override val displayHandler: TextDisplayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			{ ComplexFluidDisplayModule(handler = it, container = mainStorage, title = mainStorage.displayName, offsetLeft = 0.0, offsetUp = getLinePos(4), offsetBack = 0.0, scale = MATCH_SIGN_FONT_SIZE) },
		)

		override fun getStores(): List<FluidStorageContainer> {
			return listOf(mainStorage)
		}

		override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
			saveStorageData(store)
		}

		override fun tick() {
			bootstrapNetwork()
			tryPumpWater()
		}

		companion object {
			private val TUBE_ORIGIN = Vec3i(0, -2, 1)
		}

		private fun tryPumpWater() {
			val delta = deltaTMS / 1000.0

			val depth = getDepth()

			println("$delta $depth")
		}

		fun getDepth(): Int {
			var depth = 0
//			var block = getBlockRelative(TUBE_ORIGIN.x, TUBE_ORIGIN.y, TUBE_ORIGIN.z)
//			val data = block.blockData
////			debugAudience.highlightBlock(Vec3i(block.x, block.y, block.z), 20L)
//
//			while (data.material.isWater || (data is Waterlogged && data.isWaterlogged)) {
////				debugAudience.highlightBlock(Vec3i(block.x, block.y, block.z), 20L)
//
//				depth++
//				block = block.getRelative(BlockFace.DOWN)
//			}

			return depth
		}
	}
}
