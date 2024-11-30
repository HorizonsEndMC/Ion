package net.horizonsend.ion.server.features.transport.filters

import com.manya.pdc.base.UuidDataType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

class FilterData(
	val uuid: UUID,
) {
	companion object : PersistentDataType<PersistentDataContainer, FilterData> {
		override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java
		override fun getComplexType(): Class<FilterData> = FilterData::class.java

		override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): FilterData {
			return FilterData(primitive.get(NamespacedKeys.USER, UuidDataType())!!)
		}

		override fun toPrimitive(complex: FilterData, context: PersistentDataAdapterContext): PersistentDataContainer {
			val data = context.newPersistentDataContainer()
			data.set(NamespacedKeys.USER,UuidDataType(), complex.uuid)
			return data
		}
	}
}
