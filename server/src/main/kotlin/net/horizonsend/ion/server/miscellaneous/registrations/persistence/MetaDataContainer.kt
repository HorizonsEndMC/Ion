package net.horizonsend.ion.server.miscellaneous.registrations.persistence

import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

data class MetaDataContainer<C : Any, S : PDCSerializers.RegisteredSerializer<C>>(
	val persistentDataType: S,
	val data: C
) {
	companion object : PersistentDataType<PersistentDataContainer, MetaDataContainer<*, *>> {
		override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java
		override fun getComplexType(): Class<MetaDataContainer<*, *>> = MetaDataContainer::class.java

		override fun toPrimitive(
			complex: MetaDataContainer<*, *>,
			context: PersistentDataAdapterContext,
		): PersistentDataContainer {
			val pdc = context.newPersistentDataContainer()
			pdc.set(NamespacedKeys.SERIALIZATION_TYPE, PersistentDataType.STRING, complex.persistentDataType.identifier)
			pdc.set(NamespacedKeys.META_DATA, PersistentDataType.TAG_CONTAINER, complex.persistentDataType.serialize(complex.data, context))

			return pdc
		}

		override fun fromPrimitive(
			primitive: PersistentDataContainer,
			context: PersistentDataAdapterContext,
		): MetaDataContainer<*, *> {
			val typeId = primitive.get(NamespacedKeys.SERIALIZATION_TYPE, PersistentDataType.STRING)!!
			val type = PDCSerializers[typeId]

			return type.loadMetaDataContainer(
				primitive.get(NamespacedKeys.META_DATA, PersistentDataType.TAG_CONTAINER)!!,
				context
			)
		}
	}
}
