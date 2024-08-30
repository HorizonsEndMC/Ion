package net.horizonsend.ion.server.features.transport.fluids.properties

import com.manya.pdc.DataTypes
import com.manya.pdc.base.EnumDataType
import net.horizonsend.ion.server.features.transport.fluids.PipedFluid

enum class FluidCategory {
	GAS;

	private val members = mutableListOf<PipedFluid>()

	fun getMembers(): List<PipedFluid> = members

	fun addMember(fluid: PipedFluid) = members.add(fluid)

	companion object {
		val persistentDataType = EnumDataType(FluidCategory::class.java)
		val listDataType = DataTypes.list(persistentDataType)
	}
}
