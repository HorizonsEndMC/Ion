package net.horizonsend.ion.server.core.registries

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.server.features.custom.blocks.CustomBlockRegistry
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModRegistry
import net.horizonsend.ion.server.features.starship.type.StarshipTypeRegistry
import net.horizonsend.ion.server.features.transport.fluids.FluidTypeRegistry

object IonRegistries : IonComponent() {
	private val allRegistries = ObjectOpenHashSet<Registry<*>>()
	private val byId = Object2ObjectOpenHashMap<String, Registry<*>>()

	override fun onEnable() {
		allRegistries.forEach { value ->
			value.boostrap()
		}
	}

	val CUSTOM_ITEMS = register(CustomItemRegistry())
	val CUSTOM_BLOCKS = register(CustomBlockRegistry())
	val ITEM_MODIFICATIONS = register(ItemModRegistry())
	val STARSHIP_TYPE = register(StarshipTypeRegistry())
	val FLUID_TYPE = register(FluidTypeRegistry())

	fun <T : Registry<*>> register(registry: T): T {
		byId[registry.id] = registry
		allRegistries.add(registry)
		return registry
	}

	operator fun get(registryID: String): Registry<*> = byId[registryID]!!
}
