package net.horizonsend.ion.server.core.registration.registries

import net.horizonsend.ion.server.core.registration.keys.AtmosphericGasKeys
import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.horizonsend.ion.server.features.transport.fluids.types.GasFluid
import net.horizonsend.ion.server.features.transport.fluids.types.SimpleFluid
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import org.bukkit.Color

class FluidTypeRegistry : Registry<FluidType>(RegistryKeys.FLUID_TYPE) {
	override fun getKeySet(): KeyRegistry<FluidType> = FluidTypeKeys

	override fun boostrap() {
		register(FluidTypeKeys.EMPTY, object : FluidType(FluidTypeKeys.EMPTY) {
			override val displayName: Component = text("Empty", WHITE)
			override val categories: Array<FluidCategory> = arrayOf()
		})

		register(FluidTypeKeys.HYDROGEN, GasFluid(FluidTypeKeys.HYDROGEN, AtmosphericGasKeys.HYDROGEN, Color.fromRGB(103, 145, 145)))
		register(FluidTypeKeys.NITROGEN, GasFluid(FluidTypeKeys.NITROGEN, AtmosphericGasKeys.NITROGEN, Color.fromRGB(59, 59, 239)))
		register(FluidTypeKeys.METHANE, GasFluid(FluidTypeKeys.METHANE, AtmosphericGasKeys.METHANE, Color.fromRGB(107, 107, 158)))
		register(FluidTypeKeys.OXYGEN, GasFluid(FluidTypeKeys.OXYGEN, AtmosphericGasKeys.OXYGEN, Color.fromRGB(216, 52, 52)))
		register(FluidTypeKeys.CHLORINE, GasFluid(FluidTypeKeys.CHLORINE, AtmosphericGasKeys.CHLORINE, Color.fromRGB(33, 196, 33)))
		register(FluidTypeKeys.FLUORINE, GasFluid(FluidTypeKeys.FLUORINE, AtmosphericGasKeys.FLUORINE, Color.fromRGB(173, 38, 123)))
		register(FluidTypeKeys.HELIUM, GasFluid(FluidTypeKeys.HELIUM, AtmosphericGasKeys.HELIUM, Color.fromRGB(196, 131, 145)))
		register(FluidTypeKeys.CARBON_DIOXIDE, GasFluid(FluidTypeKeys.CARBON_DIOXIDE, AtmosphericGasKeys.CARBON_DIOXIDE, Color.fromRGB(127, 43, 43)))

		register(FluidTypeKeys.WATER, SimpleFluid(FluidTypeKeys.WATER, text("Water", BLUE), arrayOf()))
	}
}
