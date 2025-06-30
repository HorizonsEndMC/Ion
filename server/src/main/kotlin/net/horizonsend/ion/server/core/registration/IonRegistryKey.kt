package net.horizonsend.ion.server.core.registration

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys
import net.horizonsend.ion.server.core.registration.registries.Registry
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import kotlin.reflect.KClass

class IonRegistryKey<T : Any, Z : T>(val registry: Registry<T>, val clazz: KClass<out Z>, key: String) : IonResourceKey<Z>(key) {
	override fun toString(): String {
		return "RegistryKey[${registry.id}:$key]"
	}

	override fun getValue(): Z {
		val stored =  registry[this]

		if (!clazz.isInstance(stored)) {
			error("The stored value at key $this is not of matching type ${clazz.simpleName}")
		}

		@Suppress("UNCHECKED_CAST")
		return stored as Z
	}

	fun isBound(): Boolean {
		return registry.isBound(this)
	}

	/** Throws an error if this registry value is not bound */
	fun checkBound() {
		if (!isBound()) error("Unbound registry key $this")
	}

	val ionNapespacedKey = NamespacedKey(IonServer, key)

	companion object : KSerializer<IonRegistryKey<*, *>> {
		override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ion.server.core.registries.IonRegistryKey") {
			element<String>("registry")
			element<String>("key")
		}

		override fun serialize(encoder: Encoder, value: IonRegistryKey<*, *>) {
			encoder.encodeStructure(descriptor) {
				encodeStringElement(descriptor, 0, value.registry.id.key)
				encodeStringElement(descriptor, 1, value.key)
			}
		}

		override fun deserialize(decoder: Decoder): IonRegistryKey<*, *> {
			return decoder.decodeStructure(descriptor) {
				var registryId = ""
				var registryKey = ""

				while (true) {
					when (val index = decodeElementIndex(descriptor)) {
						0 -> registryId = decodeStringElement(descriptor, 0)
						1 -> registryKey = decodeStringElement(descriptor, 1)
						CompositeDecoder.DECODE_DONE -> break
						else -> error("Unexpected index: $index")
					}
				}

				RegistryKeys[registryId]!!.getValue().getKeySet().getOrTrow(registryKey)
			}
		}
	}

	class Serializer<T : Any>(val keyRegistry: KeyRegistry<T>) : PersistentDataType<String, IonRegistryKey<T, out T>> {
		override fun getPrimitiveType(): Class<String> = String::class.java
		@Suppress("UNCHECKED_CAST")
		override fun getComplexType(): Class<IonRegistryKey<T, out T>> = IonRegistryKey::class.java as Class<IonRegistryKey<T, out T>>

		override fun toPrimitive(
			complex: IonRegistryKey<T, out T>,
			context: PersistentDataAdapterContext,
		): String {
			return complex.key
		}

		override fun fromPrimitive(
			primitive: String,
			context: PersistentDataAdapterContext,
		): IonRegistryKey<T, out T> {
			return keyRegistry.get(primitive)!!
		}
	}
}
