package net.horizonsend.ion.server.core.registries

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.common.IonComponent

object IonRegistries : IonComponent() {
	private val allRegistries = ObjectOpenHashSet<Registry<*>>()
	private val byId = Object2ObjectOpenHashMap<String, Registry<*>>()

	override fun onEnable() {
		allRegistries.forEach { value ->
			value.boostrap()
		}
	}

	fun <T : Registry<*>> register(registry: T): T {
		byId[registry.id] = registry
		return registry
	}
}
