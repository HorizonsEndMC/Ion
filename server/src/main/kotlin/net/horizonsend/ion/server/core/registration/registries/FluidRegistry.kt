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

		register(FluidTypeKeys.HYDROGEN, GasFluid(FluidTypeKeys.HYDROGEN, AtmosphericGasKeys.HYDROGEN, Color.YELLOW))
		register(FluidTypeKeys.NITROGEN, GasFluid(FluidTypeKeys.NITROGEN, AtmosphericGasKeys.NITROGEN, Color.YELLOW))
		register(FluidTypeKeys.METHANE, GasFluid(FluidTypeKeys.METHANE, AtmosphericGasKeys.METHANE, Color.YELLOW))
		register(FluidTypeKeys.OXYGEN, GasFluid(FluidTypeKeys.OXYGEN, AtmosphericGasKeys.OXYGEN, Color.YELLOW))
		register(FluidTypeKeys.CHLORINE, GasFluid(FluidTypeKeys.CHLORINE, AtmosphericGasKeys.CHLORINE, Color.YELLOW))
		register(FluidTypeKeys.FLUORINE, GasFluid(FluidTypeKeys.FLUORINE, AtmosphericGasKeys.FLUORINE, Color.YELLOW))
		register(FluidTypeKeys.HELIUM, GasFluid(FluidTypeKeys.HELIUM, AtmosphericGasKeys.HELIUM, Color.YELLOW))
		register(FluidTypeKeys.CARBON_DIOXIDE, GasFluid(FluidTypeKeys.CARBON_DIOXIDE, AtmosphericGasKeys.CARBON_DIOXIDE, Color.YELLOW))

		register(FluidTypeKeys.WATER, SimpleFluid(FluidTypeKeys.WATER, text("Water", BLUE), arrayOf()))
	}
}
