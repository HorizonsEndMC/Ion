package net.horizonsend.ion.server.features.gas

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.custom.items.CustomItems
import net.horizonsend.ion.server.features.custom.items.CustomItems.GAS_CANISTER_EMPTY
import net.horizonsend.ion.server.features.custom.items.CustomItems.customItem
import net.horizonsend.ion.server.features.custom.items.GasCanister
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
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Furnace
import org.bukkit.block.Hopper
import org.bukkit.block.Sign
import org.bukkit.block.data.Directional
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ThreadLocalRandom

@Suppress("UNUSED")
object Gasses : IonServerComponent(false) {
	private val gasses = mutableMapOf<String, Gas>()

	// Fuels
	val HYDROGEN = registerGas(
		object : GasFuel(
			identifier = "HYDROGEN",
			displayName = text("Hydrogen", NamedTextColor.RED),
			containerIdentifier = "GAS_CANISTER_HYDROGEN",
			powerPerUnit = IonServer.gassesConfiguration.gasses.hydrogen.powerPerUnit,
			cooldown = IonServer.gassesConfiguration.gasses.hydrogen.cooldown,
			factorSupplier = IonServer.gassesConfiguration.gasses::hydrogen
		) {}
	)
	val NITROGEN = registerGas(
		object : GasFuel(
			identifier = "NITROGEN",
			displayName = text("Nitrogen", NamedTextColor.RED),
			containerIdentifier = "GAS_CANISTER_NITROGEN",
			powerPerUnit = IonServer.gassesConfiguration.gasses.nitrogen.powerPerUnit,
			cooldown = IonServer.gassesConfiguration.gasses.nitrogen.cooldown,
			factorSupplier = IonServer.gassesConfiguration.gasses::nitrogen
		) {}
	)
	val METHANE = registerGas(
		object : GasFuel(
			identifier = "METHANE",
			displayName = text("Methane", NamedTextColor.RED),
			containerIdentifier = "GAS_CANISTER_METHANE",
			powerPerUnit = IonServer.gassesConfiguration.gasses.methane.powerPerUnit,
			cooldown = IonServer.gassesConfiguration.gasses.methane.cooldown,
			factorSupplier = IonServer.gassesConfiguration.gasses::methane
		) {}
	)

	// Oxidizers
	val OXYGEN = registerGas(
		object : GasOxidizer(
			identifier = "OXYGEN",
			displayName = text("Oxygen", NamedTextColor.YELLOW),
			containerIdentifier = "GAS_CANISTER_OXYGEN",
			powerMultipler = IonServer.gassesConfiguration.gasses.oxygen.powerMultiplier,
			factorSupplier = IonServer.gassesConfiguration.gasses::oxygen
		) {}
	)
	val CHLORINE = registerGas(
		object : GasOxidizer(
			identifier = "CHLORINE",
			displayName = text("Chlorine", NamedTextColor.YELLOW),
			containerIdentifier = "GAS_CANISTER_CHLORINE",
			powerMultipler = IonServer.gassesConfiguration.gasses.chlorine.powerMultiplier,
			factorSupplier = IonServer.gassesConfiguration.gasses::chlorine
		) {}
	)
	val FLUORINE = registerGas(
		object : GasOxidizer(
			identifier = "FLUORINE",
			displayName = text("Fluorine", NamedTextColor.YELLOW),
			containerIdentifier = "GAS_CANISTER_FLUORINE",
			powerMultipler = IonServer.gassesConfiguration.gasses.fluorine.powerMultiplier,
			factorSupplier = IonServer.gassesConfiguration.gasses::fluorine
		) {}
	)

	// Other
	val HELIUM = registerGas(
		object : Gas(
			identifier = "HELIUM",
			displayName = text("Helium", NamedTextColor.BLUE),
			containerIdentifier = "GAS_CANISTER_HELIUM",
			configurationSupplier = IonServer.gassesConfiguration.gasses::helium
		) {}
	)
	val CARBON_DIOXIDE = registerGas(
		object : Gas(
			identifier = "CARBON_DIOXIDE",
			displayName = text("Carbon Dioxide", NamedTextColor.BLUE),
			containerIdentifier = "GAS_CANISTER_CARBON_DIOXIDE",
			configurationSupplier = IonServer.gassesConfiguration.gasses::carbonDioxide
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

		val furnace = collector.block.getRelativeIfLoaded(attachedFace) ?: return
		val hopper = furnace.getRelativeIfLoaded(attachedFace) ?: return

		for (face in arrayOf(
			attachedFace.rightFace,
			attachedFace.leftFace,
			BlockFace.UP,
			BlockFace.DOWN
		)) {
			val lightningRod = furnace.getRelativeIfLoaded(face) ?: continue
			if (lightningRod.type != Material.LIGHTNING_ROD) continue
			val blockFace = (lightningRod.blockData as Directional).facing
			if (blockFace != face && blockFace != face.oppositeFace) continue

			val location = lightningRod.getRelativeIfLoaded(face)?.location ?: continue
			val availableGasses = findGas(location)

			Tasks.sync {
				val gas = availableGasses.firstOrNull { it.tryCollect(location) } ?: return@sync

				tryHarvestGas(furnace, hopper, gas)
			}
		}
	}

	val EMPTY_CANISTER: ItemStack = GAS_CANISTER_EMPTY.constructItemStack()

	fun isEmptyCanister(itemStack: ItemStack?): Boolean {
		return itemStack?.customItem?.identifier == GAS_CANISTER_EMPTY.identifier
	}

	fun isCanister(itemStack: ItemStack?): Boolean = isEmptyCanister(itemStack) || itemStack?.customItem is GasCanister

	private fun tryHarvestGas(furnaceBlock: Block, hopperBlock: Block, gas: Gas): Boolean {
		val furnace = furnaceBlock.getState(false) as Furnace
		val hopper = hopperBlock.getState(false) as Hopper

		val canisterItem = furnace.inventory.fuel ?: return false
		val customItem = canisterItem.customItem ?: return false

		return when (customItem) {
			GAS_CANISTER_EMPTY -> fillEmptyCanister(furnace, gas)

			is GasCanister -> fillGasCanister(canisterItem, furnace, hopper) // Don't even bother with the gas

			else -> false
		}
	}

	private fun fillEmptyCanister(furnace: Furnace, gas: Gas): Boolean {
		val newType = CustomItems.getByIdentifier(gas.containerIdentifier) as? GasCanister ?: return false
		val newCanister = newType.createWithFill(IonServer.gassesConfiguration.collectorAmount)

		furnace.inventory.fuel = newCanister

		return true
	}

	private fun fillGasCanister(canisterItem: ItemStack, furnace: Furnace, hopper: Hopper): Boolean {
		val type = canisterItem.customItem ?: return false
		if (type !is GasCanister) return  false

		val currentFill = type.getFill(canisterItem)
		val newFill = currentFill + IonServer.gassesConfiguration.collectorAmount

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

	private fun findGas(location: Location) = gasses.values.filter { it.tryCollect(location) }.shuffled(ThreadLocalRandom.current())
	fun findAvailableGasses(location: Location) = gasses.values.filter {
		it.canBeFound(location)
	}

	operator fun get(identifier: String): Gas = gasses[identifier] ?: throw NoSuchElementException()

	operator fun get(itemStack: ItemStack?): Gas? {
		if (itemStack == null) return null

		val customItem = itemStack.customItem ?: return  null

		if (customItem !is GasCanister) return null

		return gasses[customItem.gasIdentifier]!!
	}

	fun all() = gasses
}
