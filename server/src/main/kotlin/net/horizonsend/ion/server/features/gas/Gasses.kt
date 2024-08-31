package net.horizonsend.ion.server.features.gas

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.custom.items.CustomItems.GAS_CANISTER_EMPTY
import net.horizonsend.ion.server.features.custom.items.CustomItems.customItem
import net.horizonsend.ion.server.features.custom.items.GasCanister
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
	val HYDROGEN = registerGas(
		object : GasFuel(
			identifier = "HYDROGEN",
			displayName = text("Hydrogen", NamedTextColor.RED),
			containerIdentifier = "GAS_CANISTER_HYDROGEN",
			powerPerUnit = IonServer.globalGassesConfiguration.gasses.hydrogen.powerPerUnit,
			cooldown = IonServer.globalGassesConfiguration.gasses.hydrogen.cooldown,
			configurationSupplier = IonServer.globalGassesConfiguration.gasses::hydrogen
		) {}
	)
	val NITROGEN = registerGas(
		object : GasFuel(
			identifier = "NITROGEN",
			displayName = text("Nitrogen", NamedTextColor.RED),
			containerIdentifier = "GAS_CANISTER_NITROGEN",
			powerPerUnit = IonServer.globalGassesConfiguration.gasses.nitrogen.powerPerUnit,
			cooldown = IonServer.globalGassesConfiguration.gasses.nitrogen.cooldown,
			configurationSupplier = IonServer.globalGassesConfiguration.gasses::nitrogen
		) {}
	)
	val METHANE = registerGas(
		object : GasFuel(
			identifier = "METHANE",
			displayName = text("Methane", NamedTextColor.RED),
			containerIdentifier = "GAS_CANISTER_METHANE",
			powerPerUnit = IonServer.globalGassesConfiguration.gasses.methane.powerPerUnit,
			cooldown = IonServer.globalGassesConfiguration.gasses.methane.cooldown,
			configurationSupplier = IonServer.globalGassesConfiguration.gasses::methane
		) {}
	)

	// Oxidizers
	val OXYGEN = registerGas(
		object : GasOxidizer(
			identifier = "OXYGEN",
			displayName = text("Oxygen", NamedTextColor.YELLOW),
			containerIdentifier = "GAS_CANISTER_OXYGEN",
			powerMultiplier = IonServer.globalGassesConfiguration.gasses.oxygen.powerMultiplier,
			configurationSupplier = IonServer.globalGassesConfiguration.gasses::oxygen
		) {}
	)
	val CHLORINE = registerGas(
		object : GasOxidizer(
			identifier = "CHLORINE",
			displayName = text("Chlorine", NamedTextColor.YELLOW),
			containerIdentifier = "GAS_CANISTER_CHLORINE",
			powerMultiplier = IonServer.globalGassesConfiguration.gasses.chlorine.powerMultiplier,
			configurationSupplier = IonServer.globalGassesConfiguration.gasses::chlorine
		) {}
	)
	val FLUORINE = registerGas(
		object : GasOxidizer(
			identifier = "FLUORINE",
			displayName = text("Fluorine", NamedTextColor.YELLOW),
			containerIdentifier = "GAS_CANISTER_FLUORINE",
			powerMultiplier = IonServer.globalGassesConfiguration.gasses.fluorine.powerMultiplier,
			configurationSupplier = IonServer.globalGassesConfiguration.gasses::fluorine
		) {}
	)

	// Other
	val HELIUM = registerGas(
		object : Gas(
			identifier = "HELIUM",
			displayName = text("Helium", NamedTextColor.BLUE),
			containerIdentifier = "GAS_CANISTER_HELIUM",
			configurationSupplier = IonServer.globalGassesConfiguration.gasses::helium
		) {}
	)
	val CARBON_DIOXIDE = registerGas(
		object : Gas(
			identifier = "CARBON_DIOXIDE",
			displayName = text("Carbon Dioxide", NamedTextColor.BLUE),
			containerIdentifier = "GAS_CANISTER_CARBON_DIOXIDE",
			configurationSupplier = IonServer.globalGassesConfiguration.gasses::carbonDioxide
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

		return gasses[customItem.gasIdentifier]!!
	}

	fun all() = gasses

	fun findAvailableGasses(location: Location): List<Gas> {
		return location.world.ion.configuration.gasConfiguration.gasses.filter { it.canBeFound(location) }.map { it.gas }
	}
}
