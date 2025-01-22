package net.horizonsend.ion.server.miscellaneous.registrations.persistence

import org.bukkit.persistence.ListPersistentDataType
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object ListMetaDataContainerType : ListPersistentDataType<PersistentDataContainer, MetaDataContainer<*, *>> {
	@Suppress("UNCHECKED_CAST")
	override fun getPrimitiveType(): Class<List<PersistentDataContainer>> = List::class.java as Class<List<PersistentDataContainer>>
	@Suppress("UNCHECKED_CAST")
	override fun getComplexType(): Class<List<MetaDataContainer<*, *>>> = List::class.java as Class<List<MetaDataContainer<*, *>>>

	override fun toPrimitive(
		complex: List<MetaDataContainer<*, *>>,
		context: PersistentDataAdapterContext,
	): List<PersistentDataContainer> {
		return PersistentDataType.LIST.dataContainers().toPrimitive(
			complex.map { container -> MetaDataContainer.toPrimitive(container, context) },
			context
		)
	}

	override fun fromPrimitive(
		primitive: List<PersistentDataContainer>,
		context: PersistentDataAdapterContext,
	): List<MetaDataContainer<*, *>> {
		return PersistentDataType.LIST.dataContainers().fromPrimitive(
			primitive,
			context
		).map { container -> MetaDataContainer.fromPrimitive(container, context) }
	}

	override fun elementType(): PersistentDataType<PersistentDataContainer, MetaDataContainer<*, *>> {
		return MetaDataContainer.Companion
	}
}
