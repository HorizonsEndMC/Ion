package net.horizonsend.ion.server.features.ai.spawning.spawner

import net.horizonsend.ion.common.utils.configuration.Configuration
import kotlin.reflect.KClass

interface PersistentDataSpawnerComponent<T : Any> {
	val typeClass: KClass<T>
	val storageKey: String

	fun load(raw: String) {
		load(deserialize(raw))
	}

	fun load(data: T)

	/** Returns null if there is nothing to save */
	fun save(): T?

	fun write(): String? = save()?.let(::serialize)

	fun serialize(complex: T): String {
		return Configuration.write(typeClass, complex)
	}

	fun deserialize(primitive: String): T {
		return Configuration.parse(typeClass, primitive)
	}
}
