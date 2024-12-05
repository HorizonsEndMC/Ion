package net.horizonsend.ion.server.features.multiblock.type.fluid

import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplay
import net.horizonsend.ion.server.features.client.display.modular.display.fluid.ComplexFluidDisplay
import net.horizonsend.ion.server.features.client.display.modular.display.fluid.SimpleFluidDisplay
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.SingleFluidStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.StorageContainer
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent.TickingManager
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset.pane
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset.stairs
import net.horizonsend.ion.server.features.transport.fluids.FluidRegistry.LOW_PRESSURE_HYDROGEN_GAS
import net.horizonsend.ion.server.features.transport.fluids.FluidRegistry.LOW_PRESSURE_OXYGEN_GAS
import net.horizonsend.ion.server.features.transport.fluids.FluidRegistry.WATER
import net.horizonsend.ion.server.features.transport.nodes.inputs.InputsData
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.TANK_1
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.TANK_2
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.TANK_3
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace.BACKWARD
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace.FORWARD
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace.LEFT
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace.RIGHT
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.Bisected.Half.BOTTOM
import org.bukkit.block.data.Bisected.Half.TOP
import org.bukkit.block.data.type.Stairs.Shape.STRAIGHT
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataAdapterContext

object ElectrolysisMultiblock : Multiblock(), EntityMultiblock<ElectrolysisMultiblock.ElectrolysisMultiblockEntity>, InteractableMultiblock, DisplayNameMultilblock {
	override val name: String = "ElectrolysisMultiblock"
	override val alternativeDetectionNames: Array<String> = arrayOf("Electrolysis")

	override val displayName: Component = ofChildren(text("Electrolysis ", GOLD), text("Machine", RED))

	override val signText: Array<Component?> = arrayOf(
		text("Electrolysis", GOLD),
		text("Machine", RED),
		null,
		null
	)

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(-1) {
				x(-2).anyStairs(stairs(BACKWARD, TOP, STRAIGHT))
				x(-1).extractor()
				x(+0).powerInput()
				x(+1).extractor()
				x(+2).anyStairs(stairs(BACKWARD, TOP, STRAIGHT))
			}
			y(+0) {
				x(-2).anyStairs(stairs(FORWARD, BOTTOM, STRAIGHT))
				x(-1).anyGlass()
				x(+0).anySolidCopperBlock()
				x(+1).anyGlass()
				x(+2).anyStairs(stairs(FORWARD, BOTTOM, STRAIGHT))
			}
			y(+1) {
				x(-2).anyGlassPane(pane(FORWARD, RIGHT))
				x(-1).anyGlass()
				x(+0).anyGlassPane(pane(LEFT, RIGHT))
				x(+1).anyGlass()
				x(+2).anyGlassPane(pane(FORWARD, LEFT))
			}
			y(+2) {
				x(-2).anyGlassPane(pane(FORWARD, RIGHT))
				x(-1).anyGlass()
				x(+0).anyGlassPane(pane(LEFT, RIGHT))
				x(+1).anyGlass()
				x(+2).anyGlassPane(pane(FORWARD, LEFT))
			}
			y(+3) {
				x(-2).anyGlassPane(pane(FORWARD, RIGHT))
				x(-1).anyGlass()
				x(+0).anyGlassPane(pane(LEFT, RIGHT))
				x(+1).anyGlass()
				x(+2).anyGlassPane(pane(FORWARD, LEFT))
			}
		}
		z(1) {
			y(-1) {
				x(-2).fluidInput()
				x(-1).redstoneBlock()
				x(+0).anySolidCopperBlock()
				x(+1).redstoneBlock()
				x(+2).fluidInput()
			}
			y(+0) {
				x(-2).titaniumBlock()
				x(-1).anyCopperGrate()
				x(+0).anySolidCopperBlock()
				x(+1).anyCopperGrate()
				x(+2).titaniumBlock()
			}
			y(+1) {
				x(-2).anyGlass()
				x(-1).anyCopperGrate()
				x(+0).anyGlass()
				x(+1).anyCopperGrate()
				x(+2).anyGlass()
			}
			y(+2) {
				x(-2).anyGlass()
				x(-1).anyCopperGrate()
				x(+0).anyGlass()
				x(+1).anyCopperGrate()
				x(+2).anyGlass()
			}
			y(+3) {
				x(-2).anyGlass()
				x(-1).anyCopperGrate()
				x(+0).anyGlass()
				x(+1).anyCopperGrate()
				x(+2).anyGlass()
			}
		}
		z(2) {
			y(-1) {
				x(-2).anyStairs(stairs(BACKWARD, TOP, STRAIGHT))
				x(-1).titaniumBlock()
				x(+0).anySolidCopperBlock()
				x(+1).titaniumBlock()
				x(+2).anyStairs(stairs(BACKWARD, TOP, STRAIGHT))
			}
			y(+0) {
				x(-2).anyStairs(stairs(BACKWARD, BOTTOM, STRAIGHT))
				x(-1).titaniumBlock()
				x(+0).anyStairs(stairs(BACKWARD, BOTTOM, STRAIGHT))
				x(+1).titaniumBlock()
				x(+2).anyStairs(stairs(BACKWARD, BOTTOM, STRAIGHT))
			}
			y(+1) {
				x(-2).anyGlassPane(pane(BACKWARD, RIGHT))
				x(-1).anyGlass()
				x(+0).anyGlassPane(pane(LEFT, RIGHT))
				x(+1).anyGlass()
				x(+2).anyGlassPane(pane(BACKWARD, LEFT))
			}
			y(+2) {
				x(-2).anyGlassPane(pane(BACKWARD, RIGHT))
				x(-1).anyGlass()
				x(+0).anyGlassPane(pane(LEFT, RIGHT))
				x(+1).anyGlass()
				x(+2).anyGlassPane(pane(BACKWARD, LEFT))
			}
			y(+3) {
				x(-2).anyGlassPane(pane(BACKWARD, RIGHT))
				x(-1).anyGlass()
				x(+0).anyGlassPane(pane(LEFT, RIGHT))
				x(+1).anyGlass()
				x(+2).anyGlassPane(pane(BACKWARD, LEFT))
			}
		}
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): ElectrolysisMultiblockEntity {
		return ElectrolysisMultiblockEntity(data, manager, x, y, z, world, structureDirection)
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		val entity = getMultiblockEntity(sign) ?: return player.alert("NULL")

		entity.displayHandler.displays.forEach {
			player.highlightBlock(it.entity.blockPosition().toVec3i(), 10L)
		}

		player.information(entity.toString())
	}

	class ElectrolysisMultiblockEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace
	) : MultiblockEntity(manager, ElectrolysisMultiblock, x, y, z, world, structureDirection), AsyncTickingMultiblockEntity, FluidStoringEntity, PoweredMultiblockEntity, DisplayMultiblockEntity {
		override val maxPower: Int = 100_000
		override val multiblock = ElectrolysisMultiblock
		override val tickingManager: TickingManager = TickingManager(interval = 4)

		override val powerStorage: PowerStorage = loadStoredPower(data)

		override val fluidStores: Array<StorageContainer> = arrayOf(
			loadStoredResource(data, "water_tank", text("Water Tank"), TANK_1, SingleFluidStorage(1000, WATER, inputAllowed =  true, extractionAllowed = false)),
			loadStoredResource(data, "oxygen_tank", text("Oxygen Tank"), TANK_2, SingleFluidStorage(10000, LOW_PRESSURE_OXYGEN_GAS, inputAllowed =  false, extractionAllowed = true)),
			loadStoredResource(data, "hydrogen_tank", text("Hydrogen Tank"), TANK_3, SingleFluidStorage(10000, LOW_PRESSURE_HYDROGEN_GAS, inputAllowed =  false, extractionAllowed = true))
		)

		private val hydrogenStorage by lazy { getNamedStorage("hydrogen_tank") }
		private val oxygenStorage by lazy { getNamedStorage("oxygen_tank") }
		private val waterStorage by lazy { getNamedStorage("water_tank") }

		override val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			PowerEntityDisplay(this, +0.0, +0.0, +0.0, 0.45f),
			SimpleFluidDisplay(waterStorage, +0.0, -0.10, +0.0, 0.45f),
			ComplexFluidDisplay(hydrogenStorage, text("Hydrogen"), +1.0, +0.0, +0.0, 0.5f),
			ComplexFluidDisplay(oxygenStorage, text("Oxygen"), -1.0, +0.0, +0.0, 0.5f)
		).register()

		override fun tickAsync() {
			val remainder = waterStorage.internalStorage.removeAmount(WATER_INCREMENT)
			val removed = WATER_INCREMENT - remainder

			oxygenStorage.internalStorage.addAmount(3 * removed)
			hydrogenStorage.internalStorage.addAmount(6 * removed)
		}

		override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
			storeFluidData(store, adapterContext)
			savePowerData(store)
		}

		override val inputsData: InputsData = InputsData.builder(this)
			.addPowerInput(0, -1, 0)
			.addFluidInput(-2, -1, 1)
			.addFluidInput(+2, -1, 1)
			.build()

//		override fun toString(): String = "Structure direction $structureDirection, display direction ${displayHandler.facing}"

		override val fluidInputOffset: Vec3i = Vec3i(0, -1, 0)

		companion object {
			const val WATER_INCREMENT = 5
		}
	}
}
