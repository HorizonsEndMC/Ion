package net.horizonsend.ion.server.core.registries

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry

object IonRegistries : IonComponent() {
	private val allRegistries = ObjectOpenHashSet<Registry<*>>()
	private val byId = Object2ObjectOpenHashMap<String, Registry<*>>()

	override fun onEnable() {
		allRegistries.forEach { value ->
			value.boostrap()
		}
	}

	val CUSTOM_ITEMS = register(CustomItemRegistry())

	fun <T : Registry<*>> register(registry: T): T {
		byId[registry.id] = registry
		allRegistries.add(registry)
		return registry
	}
}
