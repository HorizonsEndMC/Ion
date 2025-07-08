package net.horizonsend.ion.server.features.multiblock.type.fluid.storage

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.fluid.ComplexFluidDisplayModule
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.crafting.input.ChemicalProcessorEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.MultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ProgressMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.ProgressMultiblock.ProgressManager
import net.horizonsend.ion.server.features.multiblock.entity.type.RecipeProcessingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidInputMetadata
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidRestriction
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.fluid.storage.ChemicalProcessorMultiblock.ChemicalProcessorEntity
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.transport.inputs.IOData
import net.horizonsend.ion.server.features.transport.inputs.IOPort.RegisteredMetaDataInput
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace.LEFT
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace.RIGHT
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Stairs.Shape.STRAIGHT
import org.bukkit.persistence.PersistentDataAdapterContext

object ChemicalProcessorMultiblock : Multiblock(), EntityMultiblock<ChemicalProcessorEntity> {
	override val name: String = "chemprocessor"
	override val signText: Array<Component?> = createSignText(
		Component.text("Chemical", NamedTextColor.GOLD),
		Component.text("Processor", HE_MEDIUM_GRAY),
		null,
		null
	)

	override fun MultiblockShape.buildStructure() {
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
		z(3) {
			y(-1) {
				x(-4).anyStairs(PrepackagedPreset.stairs(RIGHT, Bisected.Half.TOP, shape = STRAIGHT))
				x(4).anyStairs(PrepackagedPreset.stairs(LEFT, Bisected.Half.TOP, shape = STRAIGHT))
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
				x(4).anyStairs(PrepackagedPreset.stairs(LEFT, Bisected.Half.TOP, shape = STRAIGHT))
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
		z(5) {
			y(-1) {
				x(-4).anyStairs(PrepackagedPreset.stairs(RIGHT, Bisected.Half.TOP, shape = STRAIGHT))
				x(4).anyStairs(PrepackagedPreset.stairs(LEFT, Bisected.Half.TOP, shape = STRAIGHT))
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
				x(0).fluidInput()
				x(1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
			}
			y(10) {
				x(0).customBlock(CustomBlockKeys.FLUID_PIPE.getValue())
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
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): ChemicalProcessorEntity {
		return ChemicalProcessorEntity(data, manager, world, x, y, z, structureDirection)
	}

	class ChemicalProcessorEntity(data: PersistentMultiblockData, manager: MultiblockManager, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace) : MultiblockEntity(
		manager, ChemicalProcessorMultiblock, world, x, y, z, structureDirection
	), DisplayMultiblockEntity,
		FluidStoringMultiblock,
		SyncTickingMultiblockEntity,
		ProgressMultiblock,
		RecipeProcessingMultiblockEntity<ChemicalProcessorEnviornment>
	{
		override var lastRecipe: MultiblockRecipe<ChemicalProcessorEnviornment>? = null
		override var hasTicked: Boolean = false

		override val progressManager: ProgressManager = ProgressManager(data)
		override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(20)

		override val ioData: IOData = IOData.builder(this)
			// Inputs
			.addPort(IOType.FLUID, -4, 0, 3) { RegisteredMetaDataInput<FluidInputMetadata>(this, FluidInputMetadata(connectedStore = input1, inputAllowed = true, outputAllowed = false)) }
			.addPort(IOType.FLUID, -4, 0, 5) { RegisteredMetaDataInput<FluidInputMetadata>(this, FluidInputMetadata(connectedStore = input2, inputAllowed = true, outputAllowed = false)) }

			// Outputs
			.addPort(IOType.FLUID, 4, 0, 3) { RegisteredMetaDataInput<FluidInputMetadata>(this, FluidInputMetadata(connectedStore = output1, inputAllowed = false, outputAllowed = true)) }
			.addPort(IOType.FLUID, 4, 0, 5) { RegisteredMetaDataInput<FluidInputMetadata>(this, FluidInputMetadata(connectedStore = output2, inputAllowed = false, outputAllowed = true)) }

			.addPort(IOType.FLUID, 0, 9, 6) { RegisteredMetaDataInput<FluidInputMetadata>(this, FluidInputMetadata(connectedStore = pollutionContainer, inputAllowed = false, outputAllowed = true)) }

			.build()

		val input1 = FluidStorageContainer(data, "input1", Component.text("input1"), NamespacedKeys.key("input1"), 100_000.0, FluidRestriction.Unlimited)
		val input2 = FluidStorageContainer(data, "input2", Component.text("input2"), NamespacedKeys.key("input2"), 100_000.0, FluidRestriction.Unlimited)
		val output1 = FluidStorageContainer(data, "output1", Component.text("output1"), NamespacedKeys.key("output1"), 100_000.0, FluidRestriction.Unlimited)
		val output2 = FluidStorageContainer(data, "output2", Component.text("output2"), NamespacedKeys.key("output2"), 100_000.0, FluidRestriction.Unlimited)
		val pollutionContainer = FluidStorageContainer(data, "pollution", Component.text("pollution"), NamespacedKeys.key("pollution"), 100_000.0, FluidRestriction.Unlimited)

		override val displayHandler: TextDisplayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			{ ComplexFluidDisplayModule(handler = it, container = input1, title = input1.displayName, offsetLeft = 4.5, offsetUp = 1.15, offsetBack = -4.0 + 0.39, scale = 0.7f, relativeFace = RIGHT) },
			{ ComplexFluidDisplayModule(handler = it, container = input2, title = input2.displayName, offsetLeft = 4.5, offsetUp = 1.15, offsetBack = -6.0 + 0.39, scale = 0.7f, relativeFace = RIGHT) },
			{ ComplexFluidDisplayModule(handler = it, container = output1, title = output1.displayName, offsetLeft = -4.5, offsetUp = 1.15, offsetBack = -4.0 + 0.39, scale = 0.7f, relativeFace = LEFT) },
			{ ComplexFluidDisplayModule(handler = it, container = output2, title = output2.displayName, offsetLeft = -4.5, offsetUp = 1.15, offsetBack = -6.0 + 0.39, scale = 0.7f, relativeFace = LEFT) }
		)

		override fun getStores(): List<FluidStorageContainer> {
			return listOf(input1, input2, output1, output2, pollutionContainer)
		}

		override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
			progressManager.saveProgressData(store)
			saveStorageData(store)
		}


		override fun tick() {
			bootstrapNetwork()

			if (!tryProcessRecipe()) {
				progressManager.reset()
				return
			}
		}

		override fun buildRecipeEnviornment(): ChemicalProcessorEnviornment = ChemicalProcessorEnviornment(
			this,
			leftInventory!!,
			rightInventory!!,
			input1,
			input2,
			output1,
			output2,
			pollutionContainer
		)

		val leftInventory get() = getInventory(INVENTORY_OFFSET_LEFT.x, INVENTORY_OFFSET_LEFT.y, INVENTORY_OFFSET_LEFT.z)
		val rightInventory get() = getInventory(INVENTORY_OFFSET_RIGHT.x, INVENTORY_OFFSET_RIGHT.y, INVENTORY_OFFSET_RIGHT.z)

		companion object {
			private val INVENTORY_OFFSET_LEFT = Vec3i(-4, 0, 4)
			private val INVENTORY_OFFSET_RIGHT = Vec3i(4, 0, 4)
		}
	}
}
