package net.horizonsend.ion.server.features.gas

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.GAS_CANISTER_EMPTY
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.type.GasCanister
import net.horizonsend.ion.server.features.gas.type.Gas
import net.horizonsend.ion.server.features.gas.type.GasFuel
import net.horizonsend.ion.server.features.gas.type.GasOxidizer
import net.horizonsend.ion.server.features.transport.fluids.FluidRegistry
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

@Suppress("UNUSED")
object Gasses : IonServerComponent(false) {
	private val gasses = mutableMapOf<String, Gas>()

	// Fuels
	val HYDROGEN: GasFuel = registerGas(
		object : GasFuel(
			identifier = "HYDROGEN",
			displayName = text("Hydrogen", NamedTextColor.RED),
			containerIdentifier = "GAS_CANISTER_HYDROGEN",
			powerPerUnit = ConfigurationFiles.globalGassesConfiguration().gasses.hydrogen.powerPerUnit,
			cooldown = ConfigurationFiles.globalGassesConfiguration().gasses.hydrogen.cooldown,
			configurationSupplier = ConfigurationFiles.globalGassesConfiguration().gasses::hydrogen,
			transportedFluidSupplier = Supplier { FluidRegistry.HYDROGEN_GAS }
		) {}
	)
	val NITROGEN: GasFuel = registerGas(
		object : GasFuel(
			identifier = "NITROGEN",
			displayName = text("Nitrogen", NamedTextColor.RED),
			containerIdentifier = "GAS_CANISTER_NITROGEN",
			powerPerUnit = ConfigurationFiles.globalGassesConfiguration().gasses.nitrogen.powerPerUnit,
			cooldown = ConfigurationFiles.globalGassesConfiguration().gasses.nitrogen.cooldown,
			configurationSupplier = ConfigurationFiles.globalGassesConfiguration().gasses::nitrogen,
			transportedFluidSupplier = { FluidRegistry.NITROGEN_GAS }
		) {}
	)
	val METHANE: GasFuel = registerGas(
		object : GasFuel(
			identifier = "METHANE",
			displayName = text("Methane", NamedTextColor.RED),
			containerIdentifier = "GAS_CANISTER_METHANE",
			powerPerUnit = ConfigurationFiles.globalGassesConfiguration().gasses.methane.powerPerUnit,
			cooldown = ConfigurationFiles.globalGassesConfiguration().gasses.methane.cooldown,
			configurationSupplier = ConfigurationFiles.globalGassesConfiguration().gasses::methane,
			transportedFluidSupplier = { FluidRegistry.METHANE_GAS }
		) {}
	)

	// Oxidizers
	val OXYGEN: GasOxidizer = registerGas(
		object : GasOxidizer(
			identifier = "OXYGEN",
			displayName = text("Oxygen", NamedTextColor.YELLOW),
			containerIdentifier = "GAS_CANISTER_OXYGEN",
			powerMultiplier = ConfigurationFiles.globalGassesConfiguration().gasses.oxygen.powerMultiplier,
			configurationSupplier = ConfigurationFiles.globalGassesConfiguration().gasses::oxygen,
			transportedFluidSupplier = { FluidRegistry.OXYGEN_GAS }
		) {}
	)
	val CHLORINE: GasOxidizer = registerGas(
		object : GasOxidizer(
			identifier = "CHLORINE",
			displayName = text("Chlorine", NamedTextColor.YELLOW),
			containerIdentifier = "GAS_CANISTER_CHLORINE",
			powerMultiplier = ConfigurationFiles.globalGassesConfiguration().gasses.chlorine.powerMultiplier,
			configurationSupplier = ConfigurationFiles.globalGassesConfiguration().gasses::chlorine,
			transportedFluidSupplier = { FluidRegistry.CHLORINE_GAS }
		) {}
	)
	val FLUORINE: GasOxidizer = registerGas(
		object : GasOxidizer(
			identifier = "FLUORINE",
			displayName = text("Fluorine", NamedTextColor.YELLOW),
			containerIdentifier = "GAS_CANISTER_FLUORINE",
			powerMultiplier = ConfigurationFiles.globalGassesConfiguration().gasses.fluorine.powerMultiplier,
			configurationSupplier = ConfigurationFiles.globalGassesConfiguration().gasses::fluorine,
			transportedFluidSupplier = { FluidRegistry.FLUORINE_GAS }
		) {}
	)

	// Other
	val HELIUM: Gas = registerGas(
		object : Gas(
			identifier = "HELIUM",
			displayName = text("Helium", NamedTextColor.BLUE),
			containerIdentifier = "GAS_CANISTER_HELIUM",
			configurationSupplier = ConfigurationFiles.globalGassesConfiguration().gasses::helium,
			transportedFluidSupplier = { FluidRegistry.HELIUM_GAS }
		) {}
	)
	val CARBON_DIOXIDE: Gas = registerGas(object : Gas(
			identifier = "CARBON_DIOXIDE",
			displayName = text("Carbon Dioxide", NamedTextColor.BLUE),
			containerIdentifier = "GAS_CANISTER_CARBON_DIOXIDE",
			configurationSupplier = ConfigurationFiles.globalGassesConfiguration().gasses::carbonDioxide,
			transportedFluidSupplier = { FluidRegistry.CARBON_DIOXIDE_GAS }
		) {}
	)

	private fun <T: Gas> registerGas(gas: T): T {
		gasses[gas.identifier] = gas
		return gas
	}

	val EMPTY_CANISTER: ItemStack = GAS_CANISTER_EMPTY.constructItemStack()

	fun isEmptyCanister(itemStack: ItemStack?): Boolean {
		return itemStack?.customItem?.identifier == GAS_CANISTER_EMPTY.identifier
	}

	fun isCanister(itemStack: ItemStack?): Boolean = isEmptyCanister(itemStack) || itemStack?.customItem is GasCanister

	operator fun get(identifier: String): Gas = gasses[identifier] ?: throw NoSuchElementException()

	operator fun get(itemStack: ItemStack?): Gas? {
		if (itemStack == null) return null

		val customItem = itemStack.customItem ?: return  null

		if (customItem !is GasCanister) return null

		return customItem.gas
	}

	fun all() = gasses

	fun findAvailableGasses(location: Location): List<Gas> {
		return location.world.ion.configuration.gasConfiguration.gasses.filter { it.canBeFound(location) }.map { it.gas }
	}
}
