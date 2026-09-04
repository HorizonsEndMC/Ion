package net.horizonsend.ion.server.core.registration.registries

import net.horizonsend.ion.server.core.registration.keys.AtmosphericGasKeys
import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys
import net.horizonsend.ion.server.features.transport.fluids.DisplayProperties
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.horizonsend.ion.server.features.transport.fluids.types.AtmosphericGasFluid
import net.horizonsend.ion.server.features.transport.fluids.types.GasFluid
import net.horizonsend.ion.server.features.transport.fluids.types.Lava
import net.horizonsend.ion.server.features.transport.fluids.types.SimpleFluid
import net.horizonsend.ion.server.features.transport.fluids.types.Water
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import org.bukkit.Color
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class FluidTypeRegistry : Registry<FluidType>(RegistryKeys.FLUID_TYPE) {
	override fun getKeySet(): KeyRegistry<FluidType> = FluidTypeKeys

	override fun boostrap() {
		register(FluidTypeKeys.EMPTY, object : FluidType(FluidTypeKeys.EMPTY) {
			override val displayProperties: DisplayProperties = DisplayProperties(Color.WHITE, "none")
			override fun getDisplayName(stack: FluidStack): Component = text("Empty", WHITE)
			override val categories: Array<FluidCategory> = arrayOf()

			override fun displayInPipe(world: World, origin: Vector, destination: Vector) {}
			override fun playLeakEffects(world: World, leakingNode: FluidNode, leakingDirection: BlockFace) {}
		})

		register(FluidTypeKeys.HYDROGEN, AtmosphericGasFluid(FluidTypeKeys.HYDROGEN, AtmosphericGasKeys.HYDROGEN, DisplayProperties(Color.fromRGB(103, 145, 145), "transparent_gas")))
		register(FluidTypeKeys.XENON, AtmosphericGasFluid(FluidTypeKeys.XENON, AtmosphericGasKeys.XENON, DisplayProperties(Color.fromRGB(123, 104, 238), "transparent_gas")))
		register(FluidTypeKeys.NITROGEN, AtmosphericGasFluid(FluidTypeKeys.NITROGEN, AtmosphericGasKeys.NITROGEN, DisplayProperties(Color.fromRGB(59, 59, 239), "transparent_gas")))
		register(FluidTypeKeys.METHANE, AtmosphericGasFluid(FluidTypeKeys.METHANE, AtmosphericGasKeys.METHANE, DisplayProperties(Color.fromRGB(107, 107, 158), "transparent_gas")))
		register(FluidTypeKeys.OXYGEN, AtmosphericGasFluid(FluidTypeKeys.OXYGEN, AtmosphericGasKeys.OXYGEN, DisplayProperties(Color.fromRGB(216, 52, 52), "transparent_gas")))
		register(FluidTypeKeys.CHLORINE, AtmosphericGasFluid(FluidTypeKeys.CHLORINE, AtmosphericGasKeys.CHLORINE, DisplayProperties(Color.fromRGB(33, 196, 33), "transparent_gas")))
		register(FluidTypeKeys.FLUORINE, AtmosphericGasFluid(FluidTypeKeys.FLUORINE, AtmosphericGasKeys.FLUORINE, DisplayProperties(Color.fromRGB(173, 38, 123), "transparent_gas")))
		register(FluidTypeKeys.HELIUM, AtmosphericGasFluid(FluidTypeKeys.HELIUM, AtmosphericGasKeys.HELIUM, DisplayProperties(Color.fromRGB(196, 131, 145), "transparent_gas")))
		register(FluidTypeKeys.CARBON_DIOXIDE, AtmosphericGasFluid(FluidTypeKeys.CARBON_DIOXIDE, AtmosphericGasKeys.CARBON_DIOXIDE, DisplayProperties(Color.fromRGB(127, 43, 43), "transparent_gas")))

		register(FluidTypeKeys.WATER, Water)
		register(FluidTypeKeys.LAVA, Lava)

		register(FluidTypeKeys.LOW_PRESSURE_STEAM, SimpleFluid(FluidTypeKeys.LOW_PRESSURE_STEAM, text("Low Pressure Steam")))
		register(FluidTypeKeys.DENSE_STEAM, SimpleFluid(FluidTypeKeys.DENSE_STEAM, text("Dense Steam")))
		register(FluidTypeKeys.SUPER_DENSE_STEAM, SimpleFluid(FluidTypeKeys.SUPER_DENSE_STEAM, text("Super Dense Steam")))
		register(FluidTypeKeys.ULTRA_DENSE_STEAM, SimpleFluid(FluidTypeKeys.ULTRA_DENSE_STEAM, text("Ultra Dense Steam")))

		register(FluidTypeKeys.POLLUTION, object : GasFluid(
			FluidTypeKeys.POLLUTION,
			DisplayProperties(Color.GRAY, "transparent_gas")
		) {
			override fun getDisplayName(stack: FluidStack): Component = text("Pollution", NamedTextColor.GRAY)
			override val plumeMultiplier: Double = 3.5
		})
	}
}
