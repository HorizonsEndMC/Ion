package net.horizonsend.ion.server.features.transport.fluids

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.gas.Gasses
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.horizonsend.ion.server.features.transport.fluids.types.GasFluid
import net.horizonsend.ion.server.features.transport.fluids.types.SimpleFluid
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import java.util.concurrent.ConcurrentHashMap

object FluidRegistry : IonServerComponent() {
	private val fluids = mutableListOf<Fluid>()
	private val byIdentifier = ConcurrentHashMap<String, Fluid>()

	val EMPTY = register(object : Fluid("EMPTY") {
		override val displayName: Component = text("Empty", WHITE)
		override val categories: Array<FluidCategory> = arrayOf()
	})

	val LOW_PRESSURE_HYDROGEN_GAS = register(GasFluid("LOW_PRESSURE_HYDROGEN_GAS") { Gasses.HYDROGEN })
	val LOW_PRESSURE_NITROGEN_GAS = register(GasFluid("LOW_PRESSURE_NITROGEN_GAS") { Gasses.NITROGEN })
	val LOW_PRESSURE_METHANE_GAS = register(GasFluid("LOW_PRESSURE_METHANE_GAS") { Gasses.METHANE })
	val LOW_PRESSURE_OXYGEN_GAS = register(GasFluid("LOW_PRESSURE_OXYGEN_GAS") { Gasses.OXYGEN })
	val LOW_PRESSURE_CHLORINE_GAS = register(GasFluid("LOW_PRESSURE_CHLORINE_GAS") { Gasses.CHLORINE })
	val LOW_PRESSURE_FLUORINE_GAS = register(GasFluid("LOW_PRESSURE_FLUORINE_GAS") { Gasses.FLUORINE })
	val LOW_PRESSURE_HELIUM_GAS = register(GasFluid("LOW_PRESSURE_HELIUM_GAS") { Gasses.HELIUM })
	val LOW_PRESSURE_CARBON_DIOXIDE_GAS = register(GasFluid("LOW_PRESSURE_CARBON_DIOXIDE_GAS") { Gasses.CARBON_DIOXIDE })

	val WATER = register(SimpleFluid("WATER", text("Water", BLUE), arrayOf()))

	init {
	    fluids.associateByTo(byIdentifier) { it.identifier }
	}

	fun <T: Fluid> register(fluid: T): T {
		fluids.add(fluid)

		fluid.categories.forEach { it.addMember(fluid) }

		return fluid
	}

	operator fun get(identifier: String): Fluid? {
		return byIdentifier[identifier]
	}

	fun getAll() = fluids
}
