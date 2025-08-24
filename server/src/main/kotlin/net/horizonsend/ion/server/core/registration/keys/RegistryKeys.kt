package net.horizonsend.ion.server.core.registration.keys

import net.horizonsend.ion.server.core.registration.IonBindableResourceKey
import net.horizonsend.ion.server.core.registration.IonRegistries
import net.horizonsend.ion.server.core.registration.registries.Registry
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import net.horizonsend.ion.server.features.gas.type.Gas
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.MultiblockRecipe
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.horizonsend.ion.server.features.transport.fluids.properties.type.FluidPropertyType
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNodeType

object RegistryKeys {
	private val keys = mutableMapOf<String, RegistryId<*>>()

	val FLUID_TYPE = registryId<FluidType>("FLUID_TYPE")
	val ATMOSPHERIC_GAS = registryId<Gas>("ATMOSPHERIC_GAS")
	val CUSTOM_ITEMS = registryId<CustomItem>("CUSTOM_ITEMS")
	val CUSTOM_BLOCKS = registryId<CustomBlock>("CUSTOM_BLOCKS")
	val ITEM_MODIFICATIONS = registryId<ItemModification>("ITEM_MODIFICATIONS")
	val MULTIBLOCK_RECIPE = registryId<MultiblockRecipe<*>>("MULTIBLOCK_RECIPE")
	val TRANSPORT_NETWORK_NODE_TYPE = registryId<TransportNodeType<*>>("TRANSPORT_NETWORK_NODE_TYPE")
	val FLUID_PROPERTY_TYPE = registryId<FluidPropertyType<*>>("FLUID_PROPERTY_TYPE")

	fun <T: Any> registryId(key: String): RegistryId<T> {
		val id = RegistryId<T>(key)
		keys[key] = id
		return id
	}

	class RegistryId<T: Any>(key: String) : IonBindableResourceKey<Registry<T>>(key) {
		@Suppress("UNCHECKED_CAST")
		override fun getValue(): Registry<T> = IonRegistries[this] as Registry<T>
	}

	operator fun get(key: String): RegistryId<*>? = keys[key]
}
