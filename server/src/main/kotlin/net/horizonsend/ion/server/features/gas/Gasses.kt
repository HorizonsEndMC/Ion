package net.horizonsend.ion.server.features.gas

import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.core.registries.keys.CustomItemKeys.GAS_CANISTER_EMPTY
import net.horizonsend.ion.server.core.registries.keys.FluidTypeKeys
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.features.custom.items.type.GasCanister
import net.horizonsend.ion.server.features.gas.type.Gas
import net.horizonsend.ion.server.features.gas.type.GasFuel
import net.horizonsend.ion.server.features.gas.type.GasOxidizer
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.inventory.ItemStack

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
			transportedFluidSupplier = FluidTypeKeys.HYDROGEN
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
			transportedFluidSupplier = FluidTypeKeys.NITROGEN
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
			transportedFluidSupplier = FluidTypeKeys.METHANE
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
			transportedFluidSupplier = FluidTypeKeys.OXYGEN
		) {}
	)
	val CHLORINE: GasOxidizer = registerGas(
		object : GasOxidizer(
			identifier = "CHLORINE",
			displayName = text("Chlorine", NamedTextColor.YELLOW),
			containerIdentifier = "GAS_CANISTER_CHLORINE",
			powerMultiplier = ConfigurationFiles.globalGassesConfiguration().gasses.chlorine.powerMultiplier,
			configurationSupplier = ConfigurationFiles.globalGassesConfiguration().gasses::chlorine,
			transportedFluidSupplier = FluidTypeKeys.CHLORINE
		) {}
	)
	val FLUORINE: GasOxidizer = registerGas(
		object : GasOxidizer(
			identifier = "FLUORINE",
			displayName = text("Fluorine", NamedTextColor.YELLOW),
			containerIdentifier = "GAS_CANISTER_FLUORINE",
			powerMultiplier = ConfigurationFiles.globalGassesConfiguration().gasses.fluorine.powerMultiplier,
			configurationSupplier = ConfigurationFiles.globalGassesConfiguration().gasses::fluorine,
			transportedFluidSupplier = FluidTypeKeys.FLUORINE
		) {}
	)

	// Other
	val HELIUM: Gas = registerGas(
		object : Gas(
			identifier = "HELIUM",
			displayName = text("Helium", NamedTextColor.BLUE),
			containerIdentifier = "GAS_CANISTER_HELIUM",
			configurationSupplier = ConfigurationFiles.globalGassesConfiguration().gasses::helium,
			transportedFluidSupplier = FluidTypeKeys.HELIUM
		) {}
	)
	val CARBON_DIOXIDE: Gas = registerGas(object : Gas(
			identifier = "CARBON_DIOXIDE",
			displayName = text("Carbon Dioxide", NamedTextColor.BLUE),
			containerIdentifier = "GAS_CANISTER_CARBON_DIOXIDE",
			configurationSupplier = ConfigurationFiles.globalGassesConfiguration().gasses::carbonDioxide,
			transportedFluidSupplier = FluidTypeKeys.CARBON_DIOXIDE
		) {}
	)

	private fun <T: Gas> registerGas(gas: T): T {
		gasses[gas.identifier] = gas
		return gas
	}

	val EMPTY_CANISTER: ItemStack = GAS_CANISTER_EMPTY.getValue().constructItemStack()

	fun isEmptyCanister(itemStack: ItemStack?): Boolean {
		return itemStack?.customItem?.key == GAS_CANISTER_EMPTY
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
