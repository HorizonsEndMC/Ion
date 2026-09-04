package net.horizonsend.ion.server.features.transport.fluids.properties

import com.manya.pdc.DataTypes
import com.manya.pdc.base.EnumDataType
import net.horizonsend.ion.server.features.transport.fluids.FluidType

enum class FluidCategory {
	GAS,
	STEAM;

	private val members = mutableListOf<FluidType>()

	fun getMembers(): List<FluidType> = members

	fun addMember(fluidType: FluidType) = members.add(fluidType)

	companion object {
		val persistentDataType = EnumDataType(FluidCategory::class.java)
		val listDataType = DataTypes.list(persistentDataType)
	}
}
