package net.horizonsend.ion.server.features.transport.fluids.properties

import com.manya.pdc.DataTypes
import com.manya.pdc.base.EnumDataType
import net.horizonsend.ion.server.features.transport.fluids.Fluid

enum class FluidCategory {
	GAS;

	private val members = mutableListOf<Fluid>()

	fun getMembers(): List<Fluid> = members

	fun addMember(fluid: Fluid) = members.add(fluid)

	companion object {
		val persistentDataType = EnumDataType(FluidCategory::class.java)
		val listDataType = DataTypes.list(persistentDataType)
	}
}
