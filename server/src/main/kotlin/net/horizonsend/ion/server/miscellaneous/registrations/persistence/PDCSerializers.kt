package net.horizonsend.ion.server.miscellaneous.registrations.persistence

import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.reflect.KClass

object PDCSerializers {
	private val registeredSerializers = mutableMapOf<String, RegisteredSerializer<*>>()

	fun <T: RegisteredSerializer<*>> register(serializer: T): T {
		registeredSerializers[serializer.identifier] = serializer
		return serializer
	}

	operator fun get(identifier: String) : RegisteredSerializer<*> = registeredSerializers[identifier]!!

	abstract class RegisteredSerializer<C : Any>(val identifier: String, val complexType: KClass<C>) : PersistentDataType<PersistentDataContainer, C> {
		fun serialize(obj: Any, context: PersistentDataAdapterContext): PersistentDataContainer {
			require(complexType.isInstance(obj))
			@Suppress("UNCHECKED_CAST")
			return toPrimitive(obj as C, context)
		}

		fun deserialize(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): C {
			return fromPrimitive(primitive, context)
		}

		fun loadMetaDataContainer(data: PersistentDataContainer, context: PersistentDataAdapterContext): MetaDataContainer<C, *> {
			return MetaDataContainer(this, deserialize(data, context))
		}
	}
}
