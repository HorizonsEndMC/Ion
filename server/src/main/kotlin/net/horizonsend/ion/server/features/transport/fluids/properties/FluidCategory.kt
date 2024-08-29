package net.horizonsend.ion.server.features.transport.fluids.properties

import net.horizonsend.ion.server.features.transport.fluids.PipedFluid

enum class FluidCategory {
	GAS;

	private val members = mutableListOf<PipedFluid>()

	fun getMembers(): List<PipedFluid> = members

	fun addMember(fluid: PipedFluid) = members.add(fluid)
}
