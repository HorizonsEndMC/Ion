package net.horizonsend.ion.server.features.transport.fluids

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.gas.Gasses
import net.horizonsend.ion.server.features.transport.fluids.types.GasPipedFluid

//
object TransportedFluids : IonServerComponent() {
	private val pipedFluids = mutableListOf<PipedFluid>()

	val HYDROGEN = register(GasPipedFluid { Gasses.HYDROGEN })
	val NITROGEN = register(GasPipedFluid { Gasses.NITROGEN })
	val METHANE = register(GasPipedFluid { Gasses.METHANE })
	val OXYGEN = register(GasPipedFluid { Gasses.OXYGEN })
	val CHLORINE = register(GasPipedFluid { Gasses.CHLORINE })
	val FLUORINE = register(GasPipedFluid { Gasses.FLUORINE })
	val HELIUM = register(GasPipedFluid { Gasses.FLUORINE })
	val CARBON_DIOXIDE = register(GasPipedFluid { Gasses.FLUORINE })

	fun <T: PipedFluid> register(fluid: T): T {
		pipedFluids.add(fluid)

		fluid.categories.forEach { it.addMember(fluid) }

		return fluid
	}
}
