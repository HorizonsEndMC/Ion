package net.horizonsend.ion.server.core.registration.registries

import net.horizonsend.ion.server.core.registration.keys.AtmosphericGasKeys
import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.horizonsend.ion.server.features.transport.fluids.types.Lava
import net.horizonsend.ion.server.features.transport.fluids.types.LegacyGasFluid
import net.horizonsend.ion.server.features.transport.fluids.types.Water
import net.horizonsend.ion.server.features.transport.fluids.types.steam.DenseSteam
import net.horizonsend.ion.server.features.transport.fluids.types.steam.SuperDenseSteam
import net.horizonsend.ion.server.features.transport.fluids.types.steam.UltraDenseSteam
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class FluidTypeRegistry : Registry<FluidType>(RegistryKeys.FLUID_TYPE) {
	override fun getKeySet(): KeyRegistry<FluidType> = FluidTypeKeys

	override fun boostrap() {
		register(FluidTypeKeys.EMPTY, object : FluidType(FluidTypeKeys.EMPTY) {
			override fun getDisplayName(stack: FluidStack): Component = text("Empty", WHITE)
			override val categories: Array<FluidCategory> = arrayOf()

			override fun displayInPipe(world: World, origin: Vector, destination: Vector) {}
			override fun playLeakEffects(world: World, leakingNode: FluidNode, leakingDirection: BlockFace) {}
			override fun getIsobaricHeatCapacity(stack: FluidStack): Double = Double.MAX_VALUE
			override fun getDensity(stack: FluidStack, location: Location?): Double = 0.0
			override fun getMolarMass(): Double = 0.0
		})

		register(FluidTypeKeys.HYDROGEN, LegacyGasFluid(FluidTypeKeys.HYDROGEN, AtmosphericGasKeys.HYDROGEN, Color.fromRGB(103, 145, 145), heatCapacity = 14.300, molarMass = 2.01568))
		register(FluidTypeKeys.NITROGEN, LegacyGasFluid(FluidTypeKeys.NITROGEN, AtmosphericGasKeys.NITROGEN, Color.fromRGB(59, 59, 239), heatCapacity = 1.040, molarMass = 28.01))
		register(FluidTypeKeys.METHANE, LegacyGasFluid(FluidTypeKeys.METHANE, AtmosphericGasKeys.METHANE, Color.fromRGB(107, 107, 158), heatCapacity = 2.191, molarMass = 16.04))
		register(FluidTypeKeys.OXYGEN, LegacyGasFluid(FluidTypeKeys.OXYGEN, AtmosphericGasKeys.OXYGEN, Color.fromRGB(216, 52, 52), heatCapacity = 0.918, molarMass = 32.0))
		register(FluidTypeKeys.CHLORINE, LegacyGasFluid(FluidTypeKeys.CHLORINE, AtmosphericGasKeys.CHLORINE, Color.fromRGB(33, 196, 33), heatCapacity = 0.479, molarMass = 70.91))
		register(FluidTypeKeys.FLUORINE, LegacyGasFluid(FluidTypeKeys.FLUORINE, AtmosphericGasKeys.FLUORINE, Color.fromRGB(173, 38, 123), heatCapacity = 0.824, molarMass = 37.99681))
		register(FluidTypeKeys.HELIUM, LegacyGasFluid(FluidTypeKeys.HELIUM, AtmosphericGasKeys.HELIUM, Color.fromRGB(196, 131, 145), heatCapacity = 5.193, molarMass = 4.0))
		register(FluidTypeKeys.CARBON_DIOXIDE, LegacyGasFluid(FluidTypeKeys.CARBON_DIOXIDE, AtmosphericGasKeys.CARBON_DIOXIDE, Color.fromRGB(127, 43, 43), heatCapacity = 0.839, molarMass = 44.01))

		register(FluidTypeKeys.WATER, Water)
		register(FluidTypeKeys.LAVA, Lava)
		
		register(FluidTypeKeys.DENSE_STEAM, DenseSteam)
		register(FluidTypeKeys.SUPER_DENSE_STEAM, SuperDenseSteam)
		register(FluidTypeKeys.ULTRA_DENSE_STEAM, UltraDenseSteam)
	}
}
