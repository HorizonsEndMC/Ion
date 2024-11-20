package net.horizonsend.ion.server.features.transport.fluids

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.gas.Gasses
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.horizonsend.ion.server.features.transport.fluids.types.GasFluid
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

	val HYDROGEN = register(GasFluid("HYDROGEN") { Gasses.HYDROGEN })
	val NITROGEN = register(GasFluid("NITROGEN") { Gasses.NITROGEN })
	val METHANE = register(GasFluid("METHANE") { Gasses.METHANE })
	val OXYGEN = register(GasFluid("OXYGEN") { Gasses.OXYGEN })
	val CHLORINE = register(GasFluid("CHLORINE") { Gasses.CHLORINE })
	val FLUORINE = register(GasFluid("FLUORINE") { Gasses.FLUORINE })
	val HELIUM = register(GasFluid("FLUORINE") { Gasses.FLUORINE })
	val CARBON_DIOXIDE = register(GasFluid("FLUORINE") { Gasses.FLUORINE })
	val WATER = register(object : Fluid("WATER") {
		override val displayName: Component = text("Water", BLUE)
		override val categories: Array<FluidCategory> = arrayOf()
	})

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
