package net.horizonsend.ion.server.features.multiblock.type.fluid.collector

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.configuration.ConfigurationFiles.globalGassesConfiguration
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplayModule
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.type.GasCanister
import net.horizonsend.ion.server.features.gas.Gasses
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
import net.horizonsend.ion.server.features.transport.nodes.inputs.InputsData
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
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
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
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

	override val displayName: Component
		get() = text("Canister Gas Collector")
	override val description: Component
		get() = text("Fills Empty Gas Canisters with a random gas on this planet.")

	override fun MultiblockShape.buildStructure() {
		at(0, 0, 0).machineFurnace()
		at(0, 0, 1).hopper()
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		val available = Gasses.findAvailableGasses(sign.location).joinToString { it.identifier }

		player.information("Available gasses: $available")
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): CanisterGasCollectorEntity {
		return CanisterGasCollectorEntity(manager, x, y, z, world, structureDirection)
	}

	class CanisterGasCollectorEntity(
		manager: MultiblockManager,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace,
	) : MultiblockEntity(manager, CanisterGasCollectorMultiblock, world, x, y, z, structureDirection), SyncTickingMultiblockEntity, FurnaceBasedMultiblockEntity, DisplayMultiblockEntity, StatusTickedMultiblockEntity {
		val configuration get() = globalGassesConfiguration()
		override val tickingManager: TickingManager = TickingManager(20)
		override val statusManager: StatusMultiblockEntity.StatusManager = StatusMultiblockEntity.StatusManager()

		override val inputsData: InputsData = none()

		override val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			{ StatusDisplayModule(it, statusManager) }
		).register()

		override fun tick() {
			val furnaceInventory = getFurnaceInventory()
			if (furnaceInventory == null) {
				sleepWithStatus(text("Not intact!", RED), configuration.collectorTickInterval)
				return
			}

			if (!Gasses.isCanister(furnaceInventory.fuel)) {
				sleepWithStatus(text("No canister.", RED), configuration.collectorTickInterval)
				return
			}

			tickingManager.sleepForTicks(configuration.collectorTickInterval)
			setBurningForTicks(configuration.collectorTickInterval)

			tickCollector(furnaceInventory)
		}

		private fun tickCollector(furnaceInventory: FurnaceInventory) {
			val hopperInventory = getInventory(0, 0, 1)
			if (hopperInventory == null) {
				sleepWithStatus(text("Not intact!", RED), configuration.collectorTickInterval)
				return
			}

			// Weight gas output based on the number of lightning rods
			val weight = arrayOf(structureDirection.rightFace, structureDirection.leftFace, BlockFace.UP, BlockFace.DOWN)
				.count { face ->
					getOrigin().getRelativeIfLoaded(face)?.type == Material.LIGHTNING_ROD
				}.toDouble().div(4.0)

			val worldConfiguration = world.ion.configuration.gasConfiguration.gasses.shuffled(ThreadLocalRandom.current())
			val availableGasses = worldConfiguration.map { it.tryCollect(location) }.filter { it.amount > 0 }

			val random = availableGasses.weightedRandomOrNull { result: CollectedGas.CollectionResult -> result.amount.toDouble() }
			if (random == null) {
				sleepWithStatus(text("No gasses available.", RED), configuration.collectorTickInterval)
				return
			}

			val delta = globalGassesConfiguration().collectorTickInterval / 20L
			val amount = (random.amount * weight) * (delta)

			Tasks.sync {
				tryHarvestGas(furnaceInventory, hopperInventory, random.gas, amount.roundToInt())
			}
		}

		private fun tryHarvestGas(furnaceInventory: FurnaceInventory, hopperInventory: Inventory, gas: Gas, amount: Int) {
			val canisterItem = furnaceInventory.fuel
			if (canisterItem == null) {
				sleepWithStatus(text("No canister.", RED), configuration.collectorTickInterval)
				return
			}

			val customItem = canisterItem.customItem
			if (customItem == null) {
				sleepWithStatus(text("Invalid canister.", RED), configuration.collectorTickInterval)
				return
			}

			when (customItem) {
				CustomItemRegistry.GAS_CANISTER_EMPTY -> fillEmptyCanister(furnaceInventory, gas, amount)
				is GasCanister -> fillGasCanister(canisterItem, furnaceInventory, hopperInventory, amount) // Don't even bother with the gas
			}
		}

		private fun fillEmptyCanister(furnaceInventory: FurnaceInventory, gas: Gas, amount: Int): Boolean {
			val newType = CustomItemRegistry.getByIdentifier(gas.containerIdentifier) as? GasCanister
			if (newType == null) {
				sleepWithStatus(text("Invalid canister.", RED), configuration.collectorTickInterval)
				return false
			}

			val newCanister = newType.createWithFill(amount)

			furnaceInventory.fuel = newCanister

			return true
		}

		private fun fillGasCanister(canisterItem: ItemStack, furnaceInventory: FurnaceInventory, hopperInventory: Inventory, amount: Int) {
			val type = canisterItem.customItem ?: return
			if (type !is GasCanister) return

			val currentFill = type.getFill(canisterItem)
			val newFill = currentFill + amount

			// If the canister would be filled
			if (newFill >= type.maximumFill) {
				// Try to add a full canister to the hopper
				val canAdd = hopperInventory.addItem(type.constructItemStack())

				// If it can be added
				if (canAdd.isEmpty()) {
					// Clear it from the furnace
					furnaceInventory.fuel = null
				} else {
					// Put a full one in its spot
					furnaceInventory.fuel = type.constructItemStack()
				}
			} else {
				// If it's completely not filled, just fill it to the new level
				type.setFill(canisterItem, newFill)
			}

			setStatus(text("Running", BLUE))
		}
	}
}
