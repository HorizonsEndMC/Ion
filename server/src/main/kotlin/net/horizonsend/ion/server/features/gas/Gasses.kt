package net.horizonsend.ion.server.features.gas

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.customitems.CustomItems
import net.horizonsend.ion.server.features.customitems.CustomItems.EMPTY_GAS_CANISTER
import net.horizonsend.ion.server.features.customitems.CustomItems.customItem
import net.horizonsend.ion.server.features.customitems.GasCanister
import net.horizonsend.ion.server.features.gas.type.Gas
import net.horizonsend.ion.server.features.gas.type.GasFuel
import net.horizonsend.ion.server.features.gas.type.GasOxidizer
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.leftFace
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Furnace
import org.bukkit.block.Hopper
import org.bukkit.block.Sign
import org.bukkit.block.data.Directional
import org.bukkit.inventory.ItemStack

object Gasses : IonServerComponent(false) {
	private val gasses = mutableMapOf<String, Gas>()

	// Fuels
	val HYDROGEN = registerGas(
		object : GasFuel(
			identifier = "HYDROGEN",
			displayName = text("Hydrogen", NamedTextColor.RED),
			containerIdentifier = "HYDROGEN_GAS_CANISTER",
			powerPerUnit = 2,
			cooldown = 150,
			factorSupplier = IonServer.gasConfiguration.hydrogen::formattedFactors
		) {}
	)
	val NITROGEN = registerGas(
		object : GasFuel(
			identifier = "NITROGEN",
			displayName = text("Nitrogen", NamedTextColor.RED),
			containerIdentifier = "NITROGEN_GAS_CANISTER",
			powerPerUnit = 1,
			cooldown = 100,
			factorSupplier = IonServer.gasConfiguration.nitrogen::formattedFactors
		) {}
	)
	val METHANE = registerGas(
		object : GasFuel(
			identifier = "METHANE",
			displayName = text("Methane", NamedTextColor.RED),
			containerIdentifier = "METHANE_GAS_CANISTER",
			powerPerUnit = 3,
			cooldown = 200,
			factorSupplier = IonServer.gasConfiguration.methane::formattedFactors
		) {}
	)

	// Oxidizers
	val OXYGEN = registerGas(
		object : GasOxidizer(
			identifier = "OXYGEN",
			displayName = text("Oxygen", NamedTextColor.YELLOW),
			containerIdentifier = "OXYGEN_GAS_CANISTER",
			powerMultipler = 1.0,
			factorSupplier = IonServer.gasConfiguration.oxygen::formattedFactors
		) {}
	)
	val CHLORINE = registerGas(
		object : GasOxidizer(
			identifier = "CHLORINE",
			displayName = text("Chlorine", NamedTextColor.YELLOW),
			containerIdentifier = "CHLORINE_GAS_CANISTER",
			powerMultipler = 1.5,
			factorSupplier = IonServer.gasConfiguration.chlorine::formattedFactors
		) {}
	)
	val FLUORINE = registerGas(
		object : GasOxidizer(
			identifier = "FLUORINE",
			displayName = text("Fluorine", NamedTextColor.YELLOW),
			containerIdentifier = "FLUORINE_GAS_CANISTER",
			powerMultipler = 2.0,
			factorSupplier = IonServer.gasConfiguration.fluorine::formattedFactors
		) {}
	)

	// Other
	val HELIUM = registerGas(
		object : Gas(
			identifier = "HELIUM",
			displayName = text("Helium", NamedTextColor.BLUE),
			containerIdentifier = "HELIUM_GAS_CANISTER",
			factorSupplier = IonServer.gasConfiguration.chlorine::formattedFactors
		) {}
	)
	val CARBON_DIOXIDE = registerGas(
		object : Gas(
			identifier = "CARBON_DIOXIDE",
			displayName = text("Carbon Dioxide", NamedTextColor.BLUE),
			containerIdentifier = "CARBON_DIOXIDE_GAS_CANISTER",
			factorSupplier = IonServer.gasConfiguration.fluorine::formattedFactors
		) {}
	)

	private fun <T: Gas> registerGas(gas: T): T {
		gasses[gas.identifier] = gas
		return gas
	}

	fun tickCollectorAsync(collector: Sign) = Tasks.async { tickCollector(collector) }

	private fun tickCollector(collector: Sign) {
		val attachedFace = collector.getFacing().oppositeFace

		val world = collector.world
		if (!world.isChunkLoaded((collector.x + attachedFace.modX) shr 4, (collector.z + attachedFace.modZ) shr 4)) return

		println("Collector 1")

		val furnace = collector.block.getRelativeIfLoaded(attachedFace) ?: return
		val hopper = furnace.getRelativeIfLoaded(attachedFace) ?: return

		println("Collector 2")

		for (face in arrayOf(
			attachedFace.rightFace,
			attachedFace.leftFace,
			BlockFace.UP,
			BlockFace.DOWN
		)) {
			val lightningRod = furnace.getRelativeIfLoaded(face) ?: continue
			println("Collector 3 $face")
			if (lightningRod.type != Material.LIGHTNING_ROD) continue
			println("Collector 4 $face")
			val blockFace = (lightningRod.blockData as Directional).facing
			println("Collector 5 $face")
			if (blockFace != face && blockFace != face.oppositeFace) continue
			println("Collector 6 $face")

			val location = lightningRod.getRelativeIfLoaded(face)?.location ?: continue
			val availableGasses = findGas(location)

			println("Collector 7 $availableGasses")

			Tasks.sync {
				val gas = availableGasses.firstOrNull { it.tryCollect(location) } ?: return@sync

				val result = tryHarvestGas(furnace, hopper, gas)
				val sound = if (result) Sound.ITEM_BOTTLE_FILL_DRAGONBREATH else Sound.ITEM_BOTTLE_FILL
				lightningRod.world.playSound(lightningRod.location, sound, 10.0f, 0.5f)

//				harvestGasses(availableGasses, furnace, hopper, lightningRod)
			}
		}
	}

	private fun harvestGasses(availableGas: List<Gas>, furnace: Block, hopper: Block, rod: Block) {
		if (!furnace.location.isChunkLoaded || !hopper.location.isChunkLoaded || !rod.location.isChunkLoaded) return

		for (gas in availableGas) {
			val result = tryHarvestGas(furnace, hopper, gas)
			val sound = if (result) Sound.ITEM_BOTTLE_FILL_DRAGONBREATH else Sound.ITEM_BOTTLE_FILL
			rod.world.playSound(rod.location, sound, 10.0f, 0.5f)
		}
	}

	fun isEmptyCanister(itemStack: ItemStack?): Boolean {
		return itemStack?.customItem?.identifier == EMPTY_GAS_CANISTER.identifier
	}

	private fun tryHarvestGas(furnaceBlock: Block, hopperBlock: Block, gas: Gas): Boolean {
		val furnace = furnaceBlock.getState(false) as Furnace
		val hopper = hopperBlock.getState(false) as Hopper

		println("tryHarvest 1")

		val canisterItem = furnace.inventory.fuel ?: return false
		println("tryHarvest 2")
		val customItem = canisterItem.customItem ?: return false
		println("tryHarvest 3")

		return when (customItem) {
			EMPTY_GAS_CANISTER -> fillEmptyCanister(canisterItem, furnace, gas)

			is GasCanister -> fillGasCanister(canisterItem, furnace, hopper, gas)

			else -> false
		}
	}

	private const val FILL_PER_COLLECTION = 30

	private fun fillEmptyCanister(canisterItem: ItemStack, furnace: Furnace, gas: Gas): Boolean {
		println("fillEmpty 1")
		val newType = CustomItems.getByIdentifier(gas.containerIdentifier) as? GasCanister ?: return false
		val newCanister = newType.constructItemStack()
		println("fillEmpty 2")

		furnace.inventory.fuel = newCanister

		newType.setFill(newCanister, furnace.inventory, FILL_PER_COLLECTION)

		println("fillEmpty 3")
		return true
	}

	private fun fillGasCanister(canisterItem: ItemStack, furnace: Furnace, hopper: Hopper, gas: Gas): Boolean {
		val type = canisterItem.customItem ?: return false
		println("fillCan 1")
		if (type !is GasCanister) return  false
		println("fillCan 2")

		val currentFill = type.getFill(canisterItem)
		val newFill = currentFill + FILL_PER_COLLECTION

		return if (newFill >= type.maximumFill) {
			val canAdd = hopper.inventory.addItem(type.constructItemStack())

			if (canAdd.isNotEmpty()) {
				furnace.inventory.fuel = null
			} else {
				furnace.inventory.fuel = type.constructItemStack()

				return false
			}

			true
		} else {
			type.setFill(canisterItem, furnace.inventory, newFill)

			true
		}
	}

	fun findGas(location: Location) = gasses.values.filter { it.tryCollect(location) }
	fun findAvailableGasses(location: Location) = gasses.values.filter { it.canBeFound(location) }

	operator fun get(identifier: String) = gasses[identifier]

	operator fun get(itemStack: ItemStack?): Gas? {
		if (itemStack == null) return null

		val customItem = itemStack.customItem ?: return  null

		if (customItem !is GasCanister) return null

		return gasses[customItem.gasIdentifier]!!
	}

	fun all() = gasses
}
