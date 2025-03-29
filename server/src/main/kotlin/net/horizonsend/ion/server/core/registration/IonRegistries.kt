package net.horizonsend.ion.server.core.registration

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.server.core.registration.registries.AtmosphericGasRegistry
import net.horizonsend.ion.server.core.registration.registries.CustomBlockRegistry
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry
import net.horizonsend.ion.server.core.registration.registries.FluidTypeRegistry
import net.horizonsend.ion.server.core.registration.registries.ItemModRegistry
import net.horizonsend.ion.server.core.registration.registries.Registry
import net.horizonsend.ion.server.features.starship.type.StarshipTypeRegistry

object IonRegistries : IonComponent() {
	private val allRegistries = ObjectOpenHashSet<Registry<*>>()
	private val byId = Object2ObjectOpenHashMap<String, Registry<*>>()

	override fun onEnable() {
		allRegistries.forEach { value ->
			value.boostrap()
		}
	}

	val FLUID_TYPE = register(FluidTypeRegistry())
	val ATMOSPHERIC_GAS = register(AtmosphericGasRegistry())
	val CUSTOM_ITEMS = register(CustomItemRegistry())
	val CUSTOM_BLOCKS = register(CustomBlockRegistry())
	val ITEM_MODIFICATIONS = register(ItemModRegistry())
	val STARSHIP_TYPE = register(StarshipTypeRegistry())

	fun <T : Registry<*>> register(registry: T): T {
		byId[registry.id] = registry
		allRegistries.add(registry)
		return registry
	}

	operator fun get(registryID: String): Registry<*> = byId[registryID]!!
}
