package net.horizonsend.ion.server.features.multiblock.type.gas

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.custom.CustomItemRegistry
import net.horizonsend.ion.server.features.custom.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.GasCanister
import net.horizonsend.ion.server.features.gas.Gasses
import net.horizonsend.ion.server.features.gas.collection.CollectedGas
import net.horizonsend.ion.server.features.gas.type.Gas
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.leftFace
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.horizonsend.ion.server.miscellaneous.utils.weightedRandomOrNull
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Furnace
import org.bukkit.block.Hopper
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.roundToInt

object GasCollectorMultiblock : Multiblock(), FurnaceMultiblock, InteractableMultiblock {
	override val name = "gascollector"

	override val signText = createSignText(
		line1 = "&cGas &6Collector",
		line2 = null,
		line3 = null,
		line4 = null
	)

	override fun MultiblockShape.buildStructure() {
		at(0, 0, 0).machineFurnace()
		at(0, 0, 1).hopper()
	}

	val configuration = ConfigurationFiles.globalGassesConfiguration()

	override fun onFurnaceTick(
		event: FurnaceBurnEvent,
		furnace: Furnace,
		sign: Sign
	) {
		event.isBurning = false
		event.burnTime = 0
		event.isCancelled = true
		val smelting = furnace.inventory.smelting

		if (smelting == null || smelting.type != Material.PRISMARINE_CRYSTALS) return

		if (!Gasses.isCanister(furnace.inventory.fuel)) return

		event.isBurning = false
		event.burnTime = configuration.collectorTickInterval
		furnace.cookTime = (-1000).toShort()
		event.isCancelled = false

		Tasks.async { tickCollector(sign) }
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		val available = Gasses.findAvailableGasses(sign.location).joinToString { it.identifier }

		player.information("Available gasses: $available")
	}

	private fun tickCollector(collectorSign: Sign) {
		val attachedFace = collectorSign.getFacing().oppositeFace

		val world = collectorSign.world
		if (!world.isChunkLoaded((collectorSign.x + attachedFace.modX) shr 4, (collectorSign.z + attachedFace.modZ) shr 4)) return

		val furnace = collectorSign.block.getRelativeIfLoaded(attachedFace) ?: return
		val hopper = furnace.getRelativeIfLoaded(attachedFace) ?: return

		// Weight gas output based on the number of lightning rods
		val weight = arrayOf(attachedFace.rightFace, attachedFace.leftFace, BlockFace.UP, BlockFace.DOWN).count { face ->
			val lightningRod = furnace.getRelativeIfLoaded(face) ?: return@count false

			lightningRod.type == Material.LIGHTNING_ROD
		}.toDouble() / 4.0

		val worldConfiguration = collectorSign.world.ion.configuration.gasConfiguration.gasses.shuffled(ThreadLocalRandom.current())
		val availableGasses = worldConfiguration.map { it.tryCollect(collectorSign.location) }.filter { it.amount > 0 }

		val random = availableGasses.weightedRandomOrNull { result: CollectedGas.CollectionResult ->
			result.amount.toDouble()
		} ?: return

		val delta = ConfigurationFiles.globalGassesConfiguration().collectorTickInterval / 20L
		val amount = (random.amount * weight) * (delta)

		Tasks.sync {
			tryHarvestGas(furnace, hopper, random.gas, amount.roundToInt())
		}
	}

	private fun tryHarvestGas(furnaceBlock: Block, hopperBlock: Block, gas: Gas, amount: Int): Boolean {
		val furnace = furnaceBlock.getState(false) as Furnace
		val hopper = hopperBlock.getState(false) as Hopper

		val canisterItem = furnace.inventory.fuel ?: return false
		val customItem = canisterItem.customItem ?: return false

		return when (customItem) {
			CustomItemRegistry.GAS_CANISTER_EMPTY -> fillEmptyCanister(furnace, gas, amount)

			is GasCanister -> fillGasCanister(canisterItem, furnace, hopper, amount) // Don't even bother with the gas

			else -> false
		}
	}

	private fun fillEmptyCanister(furnace: Furnace, gas: Gas, amount: Int): Boolean {
		val newType = CustomItemRegistry.getByIdentifier(gas.containerIdentifier) as? GasCanister ?: return false
		val newCanister = newType.createWithFill(amount)

		furnace.inventory.fuel = newCanister

		return true
	}

	private fun fillGasCanister(canisterItem: ItemStack, furnace: Furnace, hopper: Hopper, amount: Int): Boolean {
		val type = canisterItem.customItem ?: return false
		if (type !is GasCanister) return  false

		val currentFill = type.getFill(canisterItem)
		val newFill = currentFill + amount

		// If the canister would be filled
		return if (newFill >= type.maximumFill) {
			// Try to add a full canister to the hopper
			val canAdd = hopper.inventory.addItem(type.constructItemStack())

			// If it can be added
			if (canAdd.isEmpty()) {
				// Clear it from the furnace
				furnace.inventory.fuel = null
			} else {
				// Put a full one in its spot
				furnace.inventory.fuel = type.constructItemStack()

				return false
			}

			true
		} else {
			// If it's completely not filled, just fill it to the new level
			type.setFill(canisterItem, newFill)

			true
		}
	}
}
