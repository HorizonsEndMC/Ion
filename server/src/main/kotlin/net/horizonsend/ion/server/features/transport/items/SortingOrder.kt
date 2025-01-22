package net.horizonsend.ion.server.features.transport.items

import com.manya.pdc.base.EnumDataType

enum class SortingOrder {
	NEAREST_FIRST,
	ROUND_ROBIN,
	FARTHEST_FIRST,
	RANDOM;

	companion object {
		val serializationType = EnumDataType(SortingOrder::class.java)
	}
}
