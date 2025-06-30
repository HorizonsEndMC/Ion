package net.horizonsend.ion.server.features.multiblock.type.fluid.storage

import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.fluid.SplitFluidDisplayModule
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidRestriction
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.fluid.storage.TestFluidTank.TestFluidTankEntity
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.transport.inputs.InputsData
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace.LEFT
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace.RIGHT
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Stairs.Shape.STRAIGHT

object TestFluidTank : Multiblock(), EntityMultiblock<TestFluidTankEntity> {
	override val name: String = "fluidtank"
	override val signText: Array<Component?> = createSignText(
		Component.text("Fluid Smank"),
		null,
		null,
		null
	)

	override fun MultiblockShape.buildStructure() {
		z(6) {
			y(-1) {
				x(-4).anyStairs(PrepackagedPreset.stairs(RIGHT, Bisected.Half.TOP, shape = STRAIGHT))
				x(-3).ironBlock()
				x(0).type(Material.PALE_OAK_WOOD)
				x(3).ironBlock()
				x(4).anyStairs(PrepackagedPreset.stairs(LEFT, Bisected.Half.TOP, shape = STRAIGHT))
			}
			y(0) {
				x(-4).ironBlock()
				x(-3).ironBlock()
				x(0).type(Material.PALE_OAK_WOOD)
				x(3).ironBlock()
				x(4).ironBlock()
			}
			y(1) {
				x(-4).anyStairs(PrepackagedPreset.stairs(RIGHT, Bisected.Half.BOTTOM, shape = STRAIGHT))
				x(-3).ironBlock()
				x(-2).ironBlock()
				x(-1).ironBlock()
				x(0).type(Material.PALE_OAK_WOOD)
				x(1).ironBlock()
				x(2).ironBlock()
				x(3).ironBlock()
				x(4).anyStairs(PrepackagedPreset.stairs(LEFT, Bisected.Half.BOTTOM, shape = STRAIGHT))
			}
			y(2) {
				x(-2).titaniumBlock()
				x(0).type(Material.PALE_OAK_WOOD)
				x(2).titaniumBlock()
			}
			y(3) {
				x(-2).type(Material.WAXED_COPPER_GRATE)
				x(2).type(Material.WAXED_COPPER_GRATE)
			}
			y(4) {
				x(-2).type(Material.WAXED_COPPER_GRATE)
				x(2).type(Material.WAXED_COPPER_GRATE)
			}
			y(5) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(6) {
				x(-2).steelBlock()
				x(2).steelBlock()
			}
			y(7) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(8) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(9) {
				x(-1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(0).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
			}
		}
		z(5) {
			y(-1) {
				x(-4).anyStairs(PrepackagedPreset.stairs(RIGHT, Bisected.Half.TOP, shape = STRAIGHT))
				x(4).extractor()
			}
			y(0) {
				x(-4).fluidInput()
				x(4).fluidInput()
			}
			y(1) {
				x(-4).anyStairs(PrepackagedPreset.stairs(RIGHT, Bisected.Half.BOTTOM, shape = STRAIGHT))
				x(-3).ironBlock()
				x(-2).ironBlock()
				x(-1).ironBlock()
				x(0).ironBlock()
				x(1).ironBlock()
				x(2).ironBlock()
				x(3).ironBlock()
				x(4).anyStairs(PrepackagedPreset.stairs(LEFT, Bisected.Half.BOTTOM, shape = STRAIGHT))
			}
			y(2) {
				x(-3).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(3) {
				x(-3).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(4) {
				x(-3).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(5) {
				x(-3).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(6) {
				x(-3).steelBlock()
				x(3).steelBlock()
			}
			y(7) {
				x(-3).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(8) {
				x(-3).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(9) {
				x(-2).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(-1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(0).titaniumBlock()
				x(1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(2).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
			}
		}
		z(4) {
			y(-1) {
				x(-4).anyStairs(PrepackagedPreset.stairs(RIGHT, Bisected.Half.TOP, shape = STRAIGHT))
				x(-2).type(Material.PALE_OAK_WOOD)
				x(2).type(Material.PALE_OAK_WOOD)
				x(4).extractor()
			}
			y(0) {
				x(-4).anyPipedInventory()
				x(-2).type(Material.PALE_OAK_WOOD)
				x(2).type(Material.PALE_OAK_WOOD)
				x(4).anyPipedInventory()
			}
			y(1) {
				x(-4).anyStairs(PrepackagedPreset.stairs(RIGHT, Bisected.Half.BOTTOM, shape = STRAIGHT))
				x(-3).ironBlock()
				x(-2).type(Material.PALE_OAK_WOOD)
				x(-1).ironBlock()
				x(0).ironBlock()
				x(1).ironBlock()
				x(2).type(Material.PALE_OAK_WOOD)
				x(3).ironBlock()
				x(4).anyStairs(PrepackagedPreset.stairs(LEFT, Bisected.Half.BOTTOM, shape = STRAIGHT))
			}
			y(2) {
				x(-3).titaniumBlock()
				x(-2).type(Material.PALE_OAK_WOOD)
				x(2).type(Material.PALE_OAK_WOOD)
				x(3).titaniumBlock()
			}
			y(3) {
				x(-3).type(Material.WAXED_COPPER_GRATE)
				x(3).type(Material.WAXED_COPPER_GRATE)
			}
			y(4) {
				x(-3).type(Material.WAXED_COPPER_GRATE)
				x(3).type(Material.WAXED_COPPER_GRATE)
			}
			y(5) {
				x(-3).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(6) {
				x(-3).steelBlock()
				x(3).steelBlock()
			}
			y(7) {
				x(-3).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(8) {
				x(-3).titaniumBlock()
				x(0).steelBlock()
				x(3).titaniumBlock()
			}
			y(9) {
				x(-2).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(-1).titaniumBlock()
				x(0).steelBlock()
				x(1).titaniumBlock()
				x(2).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
			}
		}
		z(3) {
			y(-1) {
				x(-4).anyStairs(PrepackagedPreset.stairs(RIGHT, Bisected.Half.TOP, shape = STRAIGHT))
				x(4).extractor()
			}
			y(0) {
				x(-4).fluidInput()
				x(4).fluidInput()
			}
			y(1) {
				x(-4).anyStairs(PrepackagedPreset.stairs(RIGHT, Bisected.Half.BOTTOM, shape = STRAIGHT))
				x(-3).ironBlock()
				x(-2).ironBlock()
				x(-1).ironBlock()
				x(0).ironBlock()
				x(1).ironBlock()
				x(2).ironBlock()
				x(3).ironBlock()
				x(4).anyStairs(PrepackagedPreset.stairs(LEFT, Bisected.Half.BOTTOM, shape = STRAIGHT))
			}
			y(2) {
				x(-3).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(3) {
				x(-3).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(4) {
				x(-3).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(5) {
				x(-3).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(6) {
				x(-3).steelBlock()
				x(3).steelBlock()
			}
			y(7) {
				x(-3).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(8) {
				x(-3).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(9) {
				x(-2).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(-1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(0).titaniumBlock()
				x(1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(2).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
			}
		}
		z(2) {
			y(-1) {
				x(-4).anyStairs(PrepackagedPreset.stairs(RIGHT, Bisected.Half.TOP, shape = STRAIGHT))
				x(-3).ironBlock()
				x(0).type(Material.PALE_OAK_WOOD)
				x(3).ironBlock()
				x(4).anyStairs(PrepackagedPreset.stairs(LEFT, Bisected.Half.TOP, shape = STRAIGHT))
			}
			y(0) {
				x(-4).ironBlock()
				x(-3).ironBlock()
				x(0).type(Material.PALE_OAK_WOOD)
				x(3).ironBlock()
				x(4).ironBlock()
			}
			y(1) {
				x(-4).anyStairs(PrepackagedPreset.stairs(RIGHT, Bisected.Half.BOTTOM, shape = STRAIGHT))
				x(-3).ironBlock()
				x(-2).ironBlock()
				x(-1).ironBlock()
				x(0).type(Material.PALE_OAK_WOOD)
				x(1).ironBlock()
				x(2).ironBlock()
				x(3).ironBlock()
				x(4).anyStairs(PrepackagedPreset.stairs(LEFT, Bisected.Half.BOTTOM, shape = STRAIGHT))
			}
			y(2) {
				x(-2).titaniumBlock()
				x(0).type(Material.PALE_OAK_WOOD)
				x(2).titaniumBlock()
			}
			y(3) {
				x(-2).type(Material.WAXED_COPPER_GRATE)
				x(2).type(Material.WAXED_COPPER_GRATE)
			}
			y(4) {
				x(-2).type(Material.WAXED_COPPER_GRATE)
				x(2).type(Material.WAXED_COPPER_GRATE)
			}
			y(5) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(6) {
				x(-2).steelBlock()
				x(2).steelBlock()
			}
			y(7) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(8) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(9) {
				x(-1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(0).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
			}
		}
		z(7) {
			y(-1) {
				x(-3).steelBlock()
				x(-2).steelBlock()
				x(2).steelBlock()
				x(3).steelBlock()
			}
			y(0) {
				x(-3).steelBlock()
				x(-2).steelBlock()
				x(2).steelBlock()
				x(3).steelBlock()
			}
			y(1) {
				x(-1).ironBlock()
				x(0).ironBlock()
				x(1).ironBlock()
			}
			y(2) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
			}
			y(3) {
				x(-1).titaniumBlock()
				x(0).type(Material.WAXED_COPPER_GRATE)
				x(1).titaniumBlock()
			}
			y(4) {
				x(-1).titaniumBlock()
				x(0).type(Material.WAXED_COPPER_GRATE)
				x(1).titaniumBlock()
			}
			y(5) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
			}
			y(6) {
				x(-1).steelBlock()
				x(0).steelBlock()
				x(1).steelBlock()
			}
			y(7) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
			}
			y(8) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
			}
		}
		z(1) {
			y(-1) {
				x(-3).steelBlock()
				x(-2).steelBlock()
				x(2).steelBlock()
				x(3).steelBlock()
			}
			y(0) {
				x(-3).steelBlock()
				x(-2).steelBlock()
				x(-1).steelBlock()
				x(1).steelBlock()
				x(2).steelBlock()
				x(3).steelBlock()
			}
			y(1) {
				x(-1).ironBlock()
				x(0).ironBlock()
				x(1).ironBlock()
			}
			y(2) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
			}
			y(3) {
				x(-1).titaniumBlock()
				x(0).type(Material.WAXED_COPPER_GRATE)
				x(1).titaniumBlock()
			}
			y(4) {
				x(-1).titaniumBlock()
				x(0).type(Material.WAXED_COPPER_GRATE)
				x(1).titaniumBlock()
			}
			y(5) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
			}
			y(6) {
				x(-1).steelBlock()
				x(0).steelBlock()
				x(1).steelBlock()
			}
			y(7) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
			}
			y(8) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
			}
		}
		z(8) {
			y(-1) {
				x(-1).ironBlock()
				x(0).ironBlock()
				x(1).ironBlock()
			}
			y(0) {
				x(-1).ironBlock()
				x(0).type(Material.TARGET)
				x(1).ironBlock()
			}
			y(1) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = STRAIGHT))
				x(0).ironBlock()
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = STRAIGHT))
			}
		}
		z(0) {
			y(-1) {
				x(-1).ironBlock()
				x(0).powerInput()
				x(1).ironBlock()
			}
			y(0) {
				x(-1).anyGlass()
				x(0).ironBlock()
				x(1).anyGlass()
			}
			y(1) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = STRAIGHT))
				x(0).ironBlock()
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = STRAIGHT))
			}
		}
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): TestFluidTankEntity {
		return TestFluidTankEntity(data, manager, world, x, y, z, structureDirection)
	}

	class TestFluidTankEntity(data: PersistentMultiblockData, manager: MultiblockManager, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace) : MultiblockEntity(
		manager, TestFluidTank, world, x, y, z, structureDirection
	), DisplayMultiblockEntity, FluidStoringMultiblock, SyncTickingMultiblockEntity {
		override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(1)

		override val inputsData: InputsData = InputsData.builder(this)
			.addFluidInput(4, 0, 3)
			.addFluidInput(4, 0, 5)
			.addFluidInput(-4, 0, 3)
			.addFluidInput(-4, 0, 5)
			.build()

		val input1 = FluidStorageContainer(data, "input1", Component.text("Main"), NamespacedKeys.MAIN_STORAGE, 100_000.0, FluidRestriction.Unlimited)
		val input2 = FluidStorageContainer(data, "input2", Component.text("Main"), NamespacedKeys.MAIN_STORAGE, 100_000.0, FluidRestriction.Unlimited)
		val output1 = FluidStorageContainer(data, "output1", Component.text("Main"), NamespacedKeys.MAIN_STORAGE, 100_000.0, FluidRestriction.Unlimited)
		val output2 = FluidStorageContainer(data, "output2", Component.text("Main"), NamespacedKeys.MAIN_STORAGE, 100_000.0, FluidRestriction.Unlimited)

		override val displayHandler: TextDisplayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			{ SplitFluidDisplayModule(handler = it, storage = input1, offsetLeft = 4.5, offsetUp = 1.25, offsetBack = -4.0 + 0.39, scale = 1.0f, relativeFace = RIGHT) },
			{ SplitFluidDisplayModule(handler = it, storage = input2, offsetLeft = 4.5, offsetUp = 1.25, offsetBack = -6.0 + 0.39, scale = 1.0f, relativeFace = RIGHT) },
			{ SplitFluidDisplayModule(handler = it, storage = output1, offsetLeft = -4.5, offsetUp = 1.25, offsetBack = -4.0 + 0.39, scale = 1.0f, relativeFace = LEFT) },
			{ SplitFluidDisplayModule(handler = it, storage = output2, offsetLeft = -4.5, offsetUp = 1.25, offsetBack = -6.0 + 0.39, scale = 1.0f, relativeFace = LEFT) }
		)

		override fun getStores(): List<FluidStorageContainer> {
			return listOf(input1, input2, output1, output2)
		}

		override fun saveStorageData(destination: PersistentMultiblockData) {
			saveStorageData(destination)
		}

		override fun tick() {
			pushFluids()
		}
	}
}
