package net.horizonsend.ion.server.features.transport.fluids

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.gas.Gasses
import net.horizonsend.ion.server.features.transport.fluids.types.BasicFluid
import net.horizonsend.ion.server.features.transport.fluids.types.GasFluid

//
object TransportedFluids : IonServerComponent() {
	val HYDROGEN = register(GasFluid { Gasses.HYDROGEN })
	val NITROGEN = register(GasFluid { Gasses.NITROGEN })
	val METHANE = register(GasFluid { Gasses.METHANE })
	val OXYGEN = register(GasFluid { Gasses.OXYGEN })
	val CHLORINE = register(GasFluid { Gasses.CHLORINE })
	val FLUORINE = register(GasFluid { Gasses.FLUORINE })
	val HELIUM = register(GasFluid { Gasses.FLUORINE })
	val CARBON_DIOXIDE = register(GasFluid { Gasses.FLUORINE })

	private val fluids = mutableListOf<Fluid>()

	fun <T: Fluid> register(fluid: T): T {
		fluids.add(fluid)
		return fluid
	}

	fun registerBasic(): BasicFluid {
		throw NotImplementedError()
	}
}
