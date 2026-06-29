package net.horizonsend.ion.server.features.multiblock.type.fluid.collector

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.configuration.ConfigurationFiles.globalGassesConfiguration
import net.horizonsend.ion.server.core.registration.IonRegistries
import net.horizonsend.ion.server.core.registration.keys.AtmosphericGasKeys
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.registries.AtmosphericGasRegistry
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplayModule
import net.horizonsend.ion.server.features.custom.items.type.GasCanister
import net.horizonsend.ion.server.features.gas.collection.CollectedGas
import net.horizonsend.ion.server.features.gas.type.Gas
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.FurnaceBasedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.StatusTickedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent.TickingManager
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.transport.inputs.IOData
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.SpaceRegion
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.leftFace
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.horizonsend.ion.server.miscellaneous.utils.weightedRandomOrNull
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.roundToInt

object CanisterGasCollectorMultiblock : Multiblock(), EntityMultiblock<CanisterGasCollectorMultiblock.CanisterGasCollectorEntity>, InteractableMultiblock, DisplayNameMultilblock {
	override val name = "gascollector"

	override val signText = createSignText(
		line1 = "&cGas &6Collector",
		line2 = null,
		line3 = null,
		line4 = null
	)

	override val displayName: Component get() = text("Canister Gas Collector")
	override val description: Component get() = text("Fills Empty Gas Canisters with the selected gas.")
	val SELECTED_GAS_KEY = NamespacedKey("ion", "gas_collector_selected_gas")

	override fun MultiblockShape.buildStructure() {
		at(0, 0, 0).machineFurnace()
		at(0, 0, 1).hopper()
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		val entity = getMultiblockEntity(sign) ?: return
		CanisterGasCollectorGui(player, entity).openGui()
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): CanisterGasCollectorEntity {
		return CanisterGasCollectorEntity(manager, x, y, z, world, structureDirection, data)
	}


	class CanisterGasCollectorEntity(
		manager: MultiblockManager,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace,
		data: PersistentMultiblockData
	) : MultiblockEntity(manager, CanisterGasCollectorMultiblock, world, x, y, z, structureDirection), SyncTickingMultiblockEntity, FurnaceBasedMultiblockEntity, DisplayMultiblockEntity, StatusTickedMultiblockEntity {

		val guiTitle: String = "Gas Collector"
		val configuration get() = globalGassesConfiguration()

		// Persist selected gas by identifier string
		var selectedGasIdentifier: String = data.getAdditionalDataOrDefault(
			SELECTED_GAS_KEY,
			PersistentDataType.STRING,
			AtmosphericGasKeys.HYDROGEN.key
		)

		val selectedGas: Gas? get() = IonRegistries.ATMOSPHERIC_GAS.getAll().firstOrNull { it.identifier == selectedGasIdentifier }

		override val tickingManager: TickingManager = TickingManager(10)
		override val statusManager: StatusMultiblockEntity.StatusManager = StatusMultiblockEntity.StatusManager()
		override val ioData: IOData = none()

		override val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			{ StatusDisplayModule(it, statusManager) }
		).register()

		override fun tick() {
			val furnaceInventory = getFurnaceInventory()
			if (furnaceInventory == null) {
				sleepWithStatus(text("Not intact!", RED), 10)
				return
			}

			if (!AtmosphericGasRegistry.isCanister(furnaceInventory.fuel)) {
				sleepWithStatus(text("No canister.", RED), 10)
				return
			}

			val gas = selectedGas
			if (gas == null) {
				sleepWithStatus(text("No gas selected.", RED), 10)
				return
			}

			// Check if selected gas is still valid for this region
			val region = world.ion.getSpaceRegion()
			val validForRegion = if (world.name == "Ilius_horizonsend_eden" && gas.identifier == "METHANE") true else when (gas.identifier) {
				"HYDROGEN" -> region == SpaceRegion.MONOLITH
				"METHANE" -> region == SpaceRegion.SPINE
				"NITROGEN" -> region == SpaceRegion.FRACTURE
				"XENON" -> false
				else -> true
			}

			if (!validForRegion) {
				sleepWithStatus(text("Gas not available in this region.", RED), 10)
				return
			}

			val hopperInventory = getInventory(0, 0, 1)
			if (hopperInventory == null) {
				sleepWithStatus(text("Not intact!", RED), 10)
				return
			}

			Tasks.sync {
				tryHarvestGas(furnaceInventory, hopperInventory, gas, 50)
			}

			setStatus(text("Running", BLUE))
		}

		private fun tryHarvestGas(furnaceInventory: FurnaceInventory, hopperInventory: Inventory, gas: Gas, amount: Int) {
			val canisterItem = furnaceInventory.fuel ?: return

			when (canisterItem.customItem) {
				CustomItemKeys.GAS_CANISTER_EMPTY.getValue() -> fillEmptyCanister(furnaceInventory, gas, amount)
				is GasCanister -> fillGasCanister(canisterItem, furnaceInventory, hopperInventory, amount)
			}
		}

		private fun fillEmptyCanister(furnaceInventory: FurnaceInventory, gas: Gas, amount: Int) {
			val newCanister = gas.containerKey.getValue().createWithFill(amount)
			furnaceInventory.fuel = newCanister
		}

		private fun fillGasCanister(canisterItem: ItemStack, furnaceInventory: FurnaceInventory, hopperInventory: Inventory, amount: Int) {
			val type = canisterItem.customItem as? GasCanister ?: return

			// Only fill if it matches the selected gas
			if (type.gas != selectedGas) {
				sleepWithStatus(text("Wrong gas type.", RED), 10)
				return
			}

			val currentFill = type.getFill(canisterItem)
			val newFill = currentFill + amount

			if (newFill >= type.maximumFill) {
				val canAdd = hopperInventory.addItem(type.constructItemStack())
				if (canAdd.isEmpty()) {
					furnaceInventory.fuel = null
				} else {
					furnaceInventory.fuel = type.constructItemStack()
				}
			} else {
				type.setFill(canisterItem, newFill)
			}

			setStatus(text("Running", BLUE))
		}

		override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
			store.addAdditionalData(SELECTED_GAS_KEY, PersistentDataType.STRING, selectedGasIdentifier)
			super.storeAdditionalData(store, adapterContext)
		}
	}
}
