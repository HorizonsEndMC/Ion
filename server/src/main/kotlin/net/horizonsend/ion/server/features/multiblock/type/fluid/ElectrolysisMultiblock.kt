package net.horizonsend.ion.server.features.multiblock.type.fluid

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplay
import net.horizonsend.ion.server.features.client.display.modular.display.fluid.ComplexFluidDisplay
import net.horizonsend.ion.server.features.client.display.modular.display.fluid.SimpleFluidDisplay
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.SingleFluidStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.StorageContainer
import net.horizonsend.ion.server.features.multiblock.entity.type.power.UpdatedPowerDisplayEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.type.NewPoweredMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset.pane
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset.stairs
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.transport.fluids.TransportedFluids.HYDROGEN
import net.horizonsend.ion.server.features.transport.fluids.TransportedFluids.OXYGEN
import net.horizonsend.ion.server.features.transport.fluids.TransportedFluids.WATER
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.TANK_1
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.TANK_2
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.TANK_3
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace.LEFT
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace.OPPOSITE
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace.RIGHT
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace.SELF
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.Bisected.Half.BOTTOM
import org.bukkit.block.data.Bisected.Half.TOP
import org.bukkit.block.data.type.Stairs.Shape.STRAIGHT
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

object ElectrolysisMultiblock : Multiblock(), NewPoweredMultiblock<ElectrolysisMultiblock.ElectrolysisMultiblockEntity>, InteractableMultiblock {
	override val name: String = "ElectrolysisMultiblock"
	override val alternativeDetectionNames: Array<String> = arrayOf("Electrolysis")

	override val signText: Array<Component?> = arrayOf(
		text("Electrolysis", NamedTextColor.GOLD),
		text("Machine", NamedTextColor.RED),
		null,
		null
	)

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(-2) {
				x(-2).anyStairs(stairs(SELF, TOP, STRAIGHT))
				x(-1).craftingTable()
				x(+0).wireInputComputer()
				x(+1).craftingTable()
				x(+2).anyStairs(stairs(SELF, TOP, STRAIGHT))
			}
			y(-1) {
				x(-2).copperBlock()
				x(-1).craftingTable()
				x(+0).fluidInput()
				x(+1).craftingTable()
				x(+2).copperBlock()
			}
			y(+0) {
				x(-2).anyStairs(stairs(SELF, BOTTOM, STRAIGHT))
				x(-1).anyGlass()
				x(+0).copperBlock()
				x(+1).anyGlass()
				x(+2).anyStairs(stairs(SELF, BOTTOM, STRAIGHT))
			}
			y(+1) {
				x(-2).anyGlassPane(pane(SELF, RIGHT))
				x(-1).anyGlass()
				x(+0).anyGlassPane(pane(LEFT, RIGHT))
				x(+1).anyGlass()
				x(+2).anyGlassPane(pane(SELF, LEFT))
			}
			y(+2) {
				x(-2).anyGlassPane(pane(SELF, RIGHT))
				x(-1).anyGlass()
				x(+0).anyGlassPane(pane(LEFT, RIGHT))
				x(+1).anyGlass()
				x(+2).anyGlassPane(pane(SELF, LEFT))
			}
		}
		z(1) {
			y(-2) {
				x(-2).titaniumBlock()
				x(-1).redstoneBlock()
				x(+0).sponge()
				x(+1).redstoneBlock()
				x(+2).titaniumBlock()
			}
			y(-1) {
				x(-2).titaniumBlock()
				x(-1).redstoneBlock()
				x(+0).copperBlock()
				x(+1).redstoneBlock()
				x(+2).titaniumBlock()
			}
			y(+0) {
				x(-2).titaniumBlock()
				x(-1).copperGrate()
				x(+0).copperBlock()
				x(+1).copperGrate()
				x(+2).titaniumBlock()
			}
			y(+1) {
				x(-2).anyGlass()
				x(-1).copperGrate()
				x(+0).anyGlass()
				x(+1).copperGrate()
				x(+2).anyGlass()
			}
			y(+2) {
				x(-2).anyGlass()
				x(-1).copperGrate()
				x(+0).anyGlass()
				x(+1).copperGrate()
				x(+2).anyGlass()
			}
		}
		z(2) {
			y(-2) {
				x(-2).anyStairs(stairs(OPPOSITE, TOP, STRAIGHT))
				x(-1).titaniumBlock()
				x(+0).anyStairs(stairs(OPPOSITE, TOP, STRAIGHT))
				x(+1).titaniumBlock()
				x(+2).anyStairs(stairs(OPPOSITE, TOP, STRAIGHT))
			}
			y(-1) {
				x(-2).copperBlock()
				x(-1).titaniumBlock()
				x(+0).copperBlock()
				x(+1).titaniumBlock()
				x(+2).copperBlock()
			}
			y(+0) {
				x(-2).anyStairs(stairs(OPPOSITE, BOTTOM, STRAIGHT))
				x(-1).titaniumBlock()
				x(+0).anyStairs(stairs(OPPOSITE, BOTTOM, STRAIGHT))
				x(+1).titaniumBlock()
				x(+2).anyStairs(stairs(OPPOSITE, BOTTOM, STRAIGHT))
			}
			y(+1) {
				x(-2).anyGlassPane(pane(OPPOSITE, RIGHT))
				x(-1).anyGlass()
				x(+0).anyGlassPane(pane(LEFT, RIGHT))
				x(+1).anyGlass()
				x(+2).anyGlassPane(pane(OPPOSITE, LEFT))
			}
			y(+2) {
				x(-2).anyGlassPane(pane(OPPOSITE, RIGHT))
				x(-1).anyGlass()
				x(+0).anyGlassPane(pane(LEFT, RIGHT))
				x(+1).anyGlass()
				x(+2).anyGlassPane(pane(OPPOSITE, LEFT))
			}
		}
	}

	override val maxPower: Int = 100_000

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): ElectrolysisMultiblockEntity {
		return ElectrolysisMultiblockEntity(
			manager,
			data,
			x,
			y,
			z,
			world,
			structureDirection,
			data.getAdditionalDataOrDefault(NamespacedKeys.POWER, PersistentDataType.INTEGER, 0)
		)
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		val entity = getMultiblockEntity(sign)

		player.information(entity.toString())
	}

	class ElectrolysisMultiblockEntity(
		manager: MultiblockManager,
		data: PersistentMultiblockData,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace,
		override var powerUnsafe: Int
	) : MultiblockEntity(
		manager,
		ElectrolysisMultiblock,
		x,
		y,
		z,
		world,
		structureDirection
	), AsyncTickingMultiblockEntity, FluidStoringEntity, UpdatedPowerDisplayEntity {
		override val tickInterval: Int = 4
		override var currentTick: Int = 0
		override var sleepTicks: Int = 0

		override val maxPower: Int = ElectrolysisMultiblock.maxPower
		override val displayUpdates: MutableList<(UpdatedPowerDisplayEntity) -> Unit> = mutableListOf()

		override val capacities: Array<StorageContainer> = arrayOf(
			loadStoredResource(data, "water_tank", text("Water Tank"), TANK_1, SingleFluidStorage(1000, WATER)),
			loadStoredResource(data, "oxygen_tank", text("Oxygen Tank"), TANK_2, SingleFluidStorage(10000, OXYGEN)),
			loadStoredResource(data, "hydrogen_tank", text("Hydrogen Tank"), TANK_3, SingleFluidStorage(10000, HYDROGEN))
		)

		private val hydrogenStorage by lazy { getNamedStorage("hydrogen_tank") }
		private val oxygenStorage by lazy { getNamedStorage("oxygen_tank") }
		private val waterStorage by lazy { getNamedStorage("water_tank") }

		private val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			PowerEntityDisplay(this, +0.0, +0.0, +0.0, 0.45f),
			SimpleFluidDisplay(waterStorage, +0.0, -0.10, +0.0, 0.45f),
			ComplexFluidDisplay(hydrogenStorage, text("Hydrogen"), +1.0, +0.0, +0.0, 0.5f),
			ComplexFluidDisplay(oxygenStorage, text("Oxygen"), -1.0, +0.0, +0.0, 0.5f)
		).register()

		override suspend fun tickAsync() {
			val remainder = waterStorage.storage.remove(WATER_INCREMENT)
			val removed = WATER_INCREMENT - remainder

			oxygenStorage.storage.addAmount(3 * removed)
			hydrogenStorage.storage.addAmount(6 * removed)
		}

		override fun storeAdditionalData(store: PersistentMultiblockData) {
			val rawStorage = store.getAdditionalDataRaw()
			storeFluidData(rawStorage, rawStorage.adapterContext)
			store.addAdditionalData(NamespacedKeys.POWER, PersistentDataType.INTEGER, getPower())
		}

		override fun onLoad() {
			displayHandler.update()
		}

		override fun onUnload() {
			displayHandler.remove()
		}

		override fun handleRemoval() {
			displayHandler.remove()
		}

		override fun displaceAdditional(movement: StarshipMovement) {
			displayHandler.displace(movement)
		}

		override fun toString(): String = "Structure direction $structureDirection, display direction ${displayHandler.facing}"

		companion object {
			const val WATER_INCREMENT = 5
		}
	}
}
