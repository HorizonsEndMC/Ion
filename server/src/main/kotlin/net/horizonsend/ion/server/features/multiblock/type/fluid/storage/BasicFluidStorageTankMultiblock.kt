package net.horizonsend.ion.server.features.multiblock.type.fluid.storage

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.MATCH_SIGN_FONT_SIZE
import net.horizonsend.ion.server.features.client.display.modular.display.StaticTextDisplayModule
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
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.type.fluid.storage.BasicFluidStorageTankMultiblock.FluidTankMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.transport.inputs.IOData
import net.horizonsend.ion.server.features.transport.inputs.IOPort
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataAdapterContext

object BasicFluidStorageTankMultiblock : Multiblock(), EntityMultiblock<FluidTankMultiblockEntity>, InteractableMultiblock {
	override val name: String = "basicfluidtank"
	override val signText: Array<Component?> = createSignText(
		ofChildren(Component.text("Fluid", NamedTextColor.GOLD), Component.text(" Tank", HEColorScheme.Companion.HE_MEDIUM_GRAY)),
		null,
		null,
		null
	)

	override val alternativeDetectionNames: Array<String> = arrayOf("tank", "fluidtank")

	override fun MultiblockShape.buildStructure() {
		z(2) {
			y(-1) {
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(-1).titaniumBlock()
				x(0).anyCopperGrate()
				x(1).titaniumBlock()
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(0) {
				x(-2).anyTerracotta()
				x(-1).titaniumBlock()
				x(0).anyCopperGrate()
				x(1).titaniumBlock()
				x(2).anyTerracotta()
			}
			y(1) {
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-1).titaniumBlock()
				x(0).anyCopperGrate()
				x(1).titaniumBlock()
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
		z(1) {
			y(-1) {
				x(-2).anyTerracotta()
				x(-1).titaniumBlock()
				x(0).anyCopperGrate()
				x(1).titaniumBlock()
				x(2).anyTerracotta()
			}
			y(0) {
				x(-2).anyCopperBulb()
				x(-1).anyGlass()
				x(0).anyGlass()
				x(1).anyGlass()
				x(2).anyCopperBulb()
			}
			y(1) {
				x(-2).terracottaOrDoubleSlab()
				x(-1).titaniumBlock()
				x(0).anyCopperGrate()
				x(1).titaniumBlock()
				x(2).terracottaOrDoubleSlab()
			}
		}
		z(0) {
			y(-1) {
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(-1).fluidInput()
				x(0).anyCopperGrate()
				x(1).fluidInput()
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(0) {
				x(-2).terracottaOrDoubleSlab()
				x(-1).titaniumBlock()
				x(0).anyCopperGrate()
				x(1).titaniumBlock()
				x(2).terracottaOrDoubleSlab()
			}
			y(1) {
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-1).titaniumBlock()
				x(0).anyCopperGrate()
				x(1).titaniumBlock()
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): FluidTankMultiblockEntity {
		return FluidTankMultiblockEntity(data, manager, world, x, y, z, structureDirection)
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		getMultiblockEntity(sign, false)?.getStores()?.first()?.let { container -> player.information(container.getContents().toString()) }
	}

	class FluidTankMultiblockEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		world: World,
		x: Int,
		y: Int,
		z: Int,
		structureDirection: BlockFace
	) : MultiblockEntity(manager, BasicFluidStorageTankMultiblock, world, x, y, z, structureDirection), DisplayMultiblockEntity, FluidStoringMultiblock, AsyncTickingMultiblockEntity {
		override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(20)

		override val ioData: IOData = IOData.Companion.builder(this)
			// Input
			.addPort(IOType.FLUID, -1, -1, 0) { IOPort.RegisteredMetaDataInput<FluidInputMetadata>(this, FluidInputMetadata(connectedStore = mainStorage, inputAllowed = true, outputAllowed = false)) }
			// Output
			.addPort(IOType.FLUID, 1, -1, 0) { IOPort.RegisteredMetaDataInput<FluidInputMetadata>(this, FluidInputMetadata(connectedStore = mainStorage, inputAllowed = false, outputAllowed = true)) }
			.build()

		val mainStorage = FluidStorageContainer(data, "main_storage", Component.text("Main Storage"), NamespacedKeys.MAIN_STORAGE, 100_000.0, FluidRestriction.Unlimited)

		override val displayHandler: TextDisplayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			{ SplitFluidDisplayModule(handler = it, storage = mainStorage, offsetLeft = 0.0, offsetUp = getLinePos(4), offsetBack = 0.0, scale = MATCH_SIGN_FONT_SIZE) },
			{ StaticTextDisplayModule(handler = it, text = Component.text("Input"), offsetLeft = 1.0, offsetUp = 0.15, offsetBack = 0.0, scale = 0.7f) },
			{ StaticTextDisplayModule(handler = it, text = Component.text("Output"), offsetLeft = -1.0, offsetUp = 0.15, offsetBack = 0.0, scale = 0.7f) }
		)

		override fun getStores(): List<FluidStorageContainer> {
			return listOf(mainStorage)
		}

		override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
			saveStorageData(store)
		}

		override fun tickAsync() {
			bootstrapNetwork()
		}
	}
}

