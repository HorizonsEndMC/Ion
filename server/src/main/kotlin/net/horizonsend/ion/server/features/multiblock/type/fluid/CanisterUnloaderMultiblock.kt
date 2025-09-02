package net.horizonsend.ion.server.features.multiblock.type.fluid

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.MATCH_SIGN_FONT_SIZE
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.fluid.SimpleFluidDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.getLinePos
import net.horizonsend.ion.server.features.custom.items.type.GasCanister
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.FurnaceBasedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidInputMetadata
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidRestriction
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.fluid.CanisterUnloaderMultiblock.CanisterUnloaderMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.GAS_UNITS_TO_LITERS
import net.horizonsend.ion.server.features.transport.inputs.IOData
import net.horizonsend.ion.server.features.transport.inputs.IOPort
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.persistence.PersistentDataAdapterContext
import kotlin.math.ceil

object CanisterUnloaderMultiblock : Multiblock(), EntityMultiblock<CanisterUnloaderMultiblockEntity> {
	override val name: String = "unloader"
	override val signText: Array<Component?> = createSignText(
		Component.text("Canister", NamedTextColor.GOLD),
		Component.text(" Unloader", HEColorScheme.Companion.HE_MEDIUM_GRAY),
		null,
		null
	)

	override fun MultiblockShape.buildStructure() {
		z(2) {
			y(-1) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(0).fluidPort()
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(0) {
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT))
				x(0).ironBlock()
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.LEFT))
			}
		}
		z(1) {
			y(-1) {
				x(-1).titaniumBlock()
				x(0).type(Material.WAXED_COPPER_GRATE)
				x(1).titaniumBlock()
			}
			y(0) {
				x(-1).titaniumBlock()
				x(0).type(Material.WAXED_COPPER_GRATE)
				x(1).titaniumBlock()
			}
		}
		z(0) {
			y(-1) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(0).powerInput()
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(0) {
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT))
				x(0).machineFurnace()
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.LEFT))
			}
		}
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): CanisterUnloaderMultiblockEntity {
		return CanisterUnloaderMultiblockEntity(data, manager, world, x, y, z, structureDirection)
	}

	class CanisterUnloaderMultiblockEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		world: World,
		x: Int,
		y: Int,
		z: Int,
		structureDirection: BlockFace
	) : MultiblockEntity(manager, CanisterUnloaderMultiblock, world, x, y, z, structureDirection),
		DisplayMultiblockEntity,
		FluidStoringMultiblock,
		SyncTickingMultiblockEntity,
		AsyncTickingMultiblockEntity,
		PoweredMultiblockEntity,
		FurnaceBasedMultiblockEntity
	{
		override val maxPower: Int = 25_000

		override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(5)

		override val ioData: IOData = IOData.Companion.builder(this)
			// Input
			.addPowerInput(0, -1, 0)
			// Output
			.addPort(IOType.FLUID, 0, -1, 2) { IOPort.RegisteredMetaDataInput<FluidInputMetadata>(this, FluidInputMetadata(connectedStore = mainStorage, inputAllowed = false, outputAllowed = true)) }
			.build()

		val mainStorage = FluidStorageContainer(data, "main_storage", Component.text("Main Storage"), NamespacedKeys.MAIN_STORAGE, 1000.0, FluidRestriction.Unlimited)

		override val powerStorage: PowerStorage = loadStoredPower(data)

		override val displayHandler: TextDisplayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			{ PowerEntityDisplayModule(it, this) },
			{ SimpleFluidDisplayModule(handler = it, storage = mainStorage, offsetLeft = 0.0, offsetUp = getLinePos(4), offsetBack = 0.0, scale = MATCH_SIGN_FONT_SIZE) },
		)

		override fun getStores(): List<FluidStorageContainer> {
			return listOf(mainStorage)
		}

		override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
			saveStorageData(store)
			savePowerData(store)
		}

		override fun tick() {
			val delta = deltaTMS / 1000.0

			if (powerStorage.getPower() < 10) return

			val furnaceInventory = getFurnaceInventory() ?: return

			val result = furnaceInventory.result
			if (result != null && !result.isEmpty) return

			val smelting = furnaceInventory.smelting ?: return
			val customItem = smelting.customItem ?: return

			if (customItem.key == CustomItemKeys.GAS_CANISTER_EMPTY) {
				moveItems(furnaceInventory)
				return
			}

			if (customItem !is GasCanister) return

			val amount = customItem.getFill(smelting)
			if (amount <= 0) {
				moveItems(furnaceInventory)
				return
			}

			val fluid = customItem.gas.fluid
			if (!mainStorage.canAdd(fluid.key)) {
				return
			}

			val toRemove = ceil(minOf(amount.toDouble() * delta, REMOVE_RATE * delta, mainStorage.getRemainingRoom())).toInt()
			val stack = FluidStack(fluid.key, toRemove.toDouble() * GAS_UNITS_TO_LITERS)

			if (!mainStorage.canAdd(stack)) return

			val newAmount = amount - toRemove
			customItem.setFill(smelting, newAmount)
			mainStorage.addFluid(stack, location)
			powerStorage.removePower(10)

			if (newAmount == 0) moveItems(furnaceInventory)
		}

		fun moveItems(furnaceInventory: FurnaceInventory) {
			furnaceInventory.smelting = null
			furnaceInventory.result = CustomItemKeys.GAS_CANISTER_EMPTY.getValue().constructItemStack()
		}

		companion object {
			private const val REMOVE_RATE = 500.0
		}

		override fun tickAsync() {
			bootstrapFluidNetwork()
		}
	}
}
