package net.horizonsend.ion.server.core.registration.registries

import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.core.registration.keys.AtmosphericGasKeys
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.features.custom.items.type.GasCanister
import net.horizonsend.ion.server.features.gas.type.Gas
import net.horizonsend.ion.server.features.gas.type.GasFuel
import net.horizonsend.ion.server.features.gas.type.GasOxidizer
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.inventory.ItemStack

class AtmosphericGasRegistry : Registry<Gas>("ATMOSPHERIC_GASSES") {
	override val keySet: KeyRegistry<Gas> = AtmosphericGasKeys

	override fun boostrap() {
		register(
            AtmosphericGasKeys.HYDROGEN, GasFuel(
                identifier = "HYDROGEN",
                displayName = Component.text("Hydrogen", NamedTextColor.RED),
                containerIdentifier = CustomItemKeys.GAS_CANISTER_HYDROGEN,
                powerPerUnit = ConfigurationFiles.globalGassesConfiguration().gasses.hydrogen.powerPerUnit,
                cooldown = ConfigurationFiles.globalGassesConfiguration().gasses.hydrogen.cooldown,
                configurationSupplier = ConfigurationFiles.globalGassesConfiguration().gasses::hydrogen,
                transportedFluidSupplier = FluidTypeKeys.HYDROGEN
            )
        )
		register(
            AtmosphericGasKeys.NITROGEN, GasFuel(
                identifier = "NITROGEN",
                displayName = Component.text("Nitrogen", NamedTextColor.RED),
                containerIdentifier = CustomItemKeys.GAS_CANISTER_NITROGEN,
                powerPerUnit = ConfigurationFiles.globalGassesConfiguration().gasses.nitrogen.powerPerUnit,
                cooldown = ConfigurationFiles.globalGassesConfiguration().gasses.nitrogen.cooldown,
                configurationSupplier = ConfigurationFiles.globalGassesConfiguration().gasses::nitrogen,
                transportedFluidSupplier = FluidTypeKeys.NITROGEN
            )
        )
		register(
            AtmosphericGasKeys.METHANE, GasFuel(
                identifier = "METHANE",
                displayName = Component.text("Methane", NamedTextColor.RED),
                containerIdentifier = CustomItemKeys.GAS_CANISTER_METHANE,
                powerPerUnit = ConfigurationFiles.globalGassesConfiguration().gasses.methane.powerPerUnit,
                cooldown = ConfigurationFiles.globalGassesConfiguration().gasses.methane.cooldown,
                configurationSupplier = ConfigurationFiles.globalGassesConfiguration().gasses::methane,
                transportedFluidSupplier = FluidTypeKeys.METHANE
            )
        )
		register(
            AtmosphericGasKeys.OXYGEN, GasOxidizer(
                identifier = "OXYGEN",
                displayName = Component.text("Oxygen", NamedTextColor.YELLOW),
                containerIdentifier = CustomItemKeys.GAS_CANISTER_OXYGEN,
                powerMultiplier = ConfigurationFiles.globalGassesConfiguration().gasses.oxygen.powerMultiplier,
                configurationSupplier = ConfigurationFiles.globalGassesConfiguration().gasses::oxygen,
                transportedFluidSupplier = FluidTypeKeys.OXYGEN
            )
        )
		register(
            AtmosphericGasKeys.CHLORINE, GasOxidizer(
                identifier = "CHLORINE",
                displayName = Component.text("Chlorine", NamedTextColor.YELLOW),
                containerIdentifier = CustomItemKeys.GAS_CANISTER_CHLORINE,
                powerMultiplier = ConfigurationFiles.globalGassesConfiguration().gasses.chlorine.powerMultiplier,
                configurationSupplier = ConfigurationFiles.globalGassesConfiguration().gasses::chlorine,
                transportedFluidSupplier = FluidTypeKeys.CHLORINE
            )
        )
		register(
            AtmosphericGasKeys.FLUORINE, GasOxidizer(
                identifier = "FLUORINE",
                displayName = Component.text("Fluorine", NamedTextColor.YELLOW),
                containerIdentifier = CustomItemKeys.GAS_CANISTER_FLUORINE,
                powerMultiplier = ConfigurationFiles.globalGassesConfiguration().gasses.fluorine.powerMultiplier,
                configurationSupplier = ConfigurationFiles.globalGassesConfiguration().gasses::fluorine,
                transportedFluidSupplier = FluidTypeKeys.FLUORINE
            )
        )
		register(
            AtmosphericGasKeys.HELIUM, Gas(
                identifier = "HELIUM",
                displayName = Component.text("Helium", NamedTextColor.BLUE),
                containerKey = CustomItemKeys.GAS_CANISTER_HELIUM,
                configurationSupplier = ConfigurationFiles.globalGassesConfiguration().gasses::helium,
                transportedFluidSupplier = FluidTypeKeys.HELIUM
            )
        )
		register(
            AtmosphericGasKeys.CARBON_DIOXIDE, Gas(
                identifier = "CARBON_DIOXIDE",
                displayName = Component.text("Carbon Dioxide", NamedTextColor.BLUE),
                containerKey = CustomItemKeys.GAS_CANISTER_CARBON_DIOXIDE,
                configurationSupplier = ConfigurationFiles.globalGassesConfiguration().gasses::carbonDioxide,
                transportedFluidSupplier = FluidTypeKeys.CARBON_DIOXIDE
            )
        )
	}

	companion object {
		val EMPTY_CANISTER: ItemStack = CustomItemKeys.GAS_CANISTER_EMPTY.getValue().constructItemStack()

		fun isEmptyCanister(itemStack: ItemStack?): Boolean {
			return itemStack?.customItem?.key == CustomItemKeys.GAS_CANISTER_EMPTY
		}

		fun isCanister(itemStack: ItemStack?): Boolean = isEmptyCanister(itemStack) || itemStack?.customItem is GasCanister

		operator fun get(itemStack: ItemStack?): Gas? {
			if (itemStack == null) return null

			val customItem = itemStack.customItem ?: return  null

			if (customItem !is GasCanister) return null

			return customItem.gas
		}

		fun findAvailableGasses(location: Location): List<Gas> {
			return location.world.ion.configuration.gasConfiguration.gasses.filter { it.canBeFound(location) }.map { it.gas }
		}
	}
}
