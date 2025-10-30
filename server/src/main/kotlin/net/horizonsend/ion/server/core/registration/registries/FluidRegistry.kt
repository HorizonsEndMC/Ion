package net.horizonsend.ion.server.core.registration.registries

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_BLUE
import net.horizonsend.ion.server.core.registration.keys.AtmosphericGasKeys
import net.horizonsend.ion.server.core.registration.keys.FluidPropertyTypeKeys
import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty
import net.horizonsend.ion.server.features.transport.fluids.types.GasFluid
import net.horizonsend.ion.server.features.transport.fluids.types.Lava
import net.horizonsend.ion.server.features.transport.fluids.types.LegacyGasFluid
import net.horizonsend.ion.server.features.transport.fluids.types.SimpleFluid
import net.horizonsend.ion.server.features.transport.fluids.types.Water
import net.horizonsend.ion.server.features.transport.fluids.types.steam.Steam
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
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

		register(FluidTypeKeys.LOW_PRESSURE_STEAM, SimpleFluid(
			FluidTypeKeys.LOW_PRESSURE_STEAM,
			text("Low Pressure Steam"),
			categories = arrayOf(FluidCategory.GAS),
			heatCapacity = 2.0,
			molarMass = 18.01528,
			density = 0.479,
		))
		register(FluidTypeKeys.DENSE_STEAM, Steam(
			key = FluidTypeKeys.DENSE_STEAM,
			prefix = text("Dense", HE_LIGHT_BLUE),
			color = Color.WHITE,
			heatCapacity = 2.030,
			pressureBars = 5.0,
			conversionResult = FluidTypeKeys.SUPER_DENSE_STEAM,
			turbineWorkPerLiter = 2675.8,
			turbineResult = FluidTypeKeys.LOW_PRESSURE_STEAM,
			conversionCost = 2257.0 * 2,
			conversionTemperature = 450.0
		))
		register(FluidTypeKeys.SUPER_DENSE_STEAM, Steam(
			key = FluidTypeKeys.SUPER_DENSE_STEAM,
			prefix = text("Super Dense"),
			color = Color.WHITE,
			heatCapacity = 2.030,
			pressureBars = 20.0,
			conversionResult = FluidTypeKeys.ULTRA_DENSE_STEAM,
			turbineWorkPerLiter = 3358.2,
			turbineResult = FluidTypeKeys.DENSE_STEAM,
			conversionCost = 2257.0 * 4,
			conversionTemperature = 600.0
		))
		register(FluidTypeKeys.ULTRA_DENSE_STEAM, Steam(
			key = FluidTypeKeys.ULTRA_DENSE_STEAM,
			prefix = text("Ultra Dense"),
			color = Color.WHITE,
			heatCapacity = 2.030,
			pressureBars = 45.0,
			// Does not convert
			conversionResult = FluidTypeKeys.EMPTY,
			turbineResult = FluidTypeKeys.SUPER_DENSE_STEAM,
			turbineWorkPerLiter = 3670.9,
			conversionCost = Double.MAX_VALUE,
			conversionTemperature = Double.MAX_VALUE
		))
		register(FluidTypeKeys.CRUDE_OIL, SimpleFluid(
			FluidTypeKeys.CRUDE_OIL,
			text("Crude Oil", NamedTextColor.BLACK),
			categories = arrayOf(FluidCategory.GAS),
			heatCapacity = 2.0, // TODO
			molarMass = 18.01528, // TODO
			density = 0.479, // TODO
			defaultProperties = mapOf(
				FluidPropertyTypeKeys.FLAMMABILITY to FluidProperty.Flammability(
					joulesPerLiter = 39600000.0,
					resultFluid = FluidTypeKeys.POLLUTION,
					resultVolumeMultiplier = 5.0 //TODO
				)
			)
		))
		register(FluidTypeKeys.POLLUTION, object : GasFluid(
			FluidTypeKeys.POLLUTION,
			Color.GRAY,
			heatCapacity = 2.0,
			molarMass = 18.01528
		) {
			override fun getDisplayName(stack: FluidStack): Component = text("Pollution", NamedTextColor.GRAY)
			override val plumeMultiplier: Double = 3.5
		})
	}
}
