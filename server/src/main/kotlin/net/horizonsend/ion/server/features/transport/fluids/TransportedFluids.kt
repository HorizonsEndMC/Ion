package net.horizonsend.ion.server.features.transport.fluids

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.gas.Gasses
import net.horizonsend.ion.server.features.transport.fluids.types.GasPipedFluid
import java.util.concurrent.ConcurrentHashMap

//
object TransportedFluids : IonServerComponent() {
	private val pipedFluids = mutableListOf<PipedFluid>()
	private val byIdentifier = ConcurrentHashMap<String, PipedFluid>()

	val HYDROGEN = register(GasPipedFluid("HYDROGEN") { Gasses.HYDROGEN })
	val NITROGEN = register(GasPipedFluid("NITROGEN") { Gasses.NITROGEN })
	val METHANE = register(GasPipedFluid("METHANE") { Gasses.METHANE })
	val OXYGEN = register(GasPipedFluid("OXYGEN") { Gasses.OXYGEN })
	val CHLORINE = register(GasPipedFluid("CHLORINE") { Gasses.CHLORINE })
	val FLUORINE = register(GasPipedFluid("FLUORINE") { Gasses.FLUORINE })
	val HELIUM = register(GasPipedFluid("FLUORINE") { Gasses.FLUORINE })
	val CARBON_DIOXIDE = register(GasPipedFluid("FLUORINE") { Gasses.FLUORINE })
	val WATER = register(GasPipedFluid("WATER") { Gasses.FLUORINE })

	init {
	    pipedFluids.associateByTo(byIdentifier) { it.identifier }
	}

	fun <T: PipedFluid> register(fluid: T): T {
		pipedFluids.add(fluid)

		fluid.categories.forEach { it.addMember(fluid) }

		return fluid
	}

	operator fun get(identifier: String): PipedFluid? {
		return byIdentifier[identifier]
	}

	fun getAll() = pipedFluids
}
