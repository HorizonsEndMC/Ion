package net.horizonsend.ion.server.core.registration

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.server.core.registration.registries.AtmosphericGasRegistry
import net.horizonsend.ion.server.core.registration.registries.CustomBlockRegistry
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry
import net.horizonsend.ion.server.core.registration.registries.FluidPropertyTypeRegistry
import net.horizonsend.ion.server.core.registration.registries.FluidTypeRegistry
import net.horizonsend.ion.server.core.registration.registries.ItemModRegistry
import net.horizonsend.ion.server.core.registration.registries.Registry
import net.horizonsend.ion.server.core.registration.registries.TransportNetworkNodeTypeRegistry
import net.horizonsend.ion.server.core.registration.registries.WeatherTypeRegistry
import net.horizonsend.ion.server.core.registration.registries.WorldGenerationFeatureRegistry
import net.horizonsend.ion.server.core.registration.registries.WrappedListenerRegistry
import net.horizonsend.ion.server.core.registration.registries.WreckStructureRegistry
import net.horizonsend.ion.server.features.multiblock.crafting.MultiblockRecipeRegistry

object IonRegistries : IonComponent() {
	private val allRegistries = mutableListOf<Registry<*>>()
	private val byId = Object2ObjectOpenHashMap<IonBindableResourceKey<out Registry<*>>, Registry<*>>()

	override fun onEnable() {
		allRegistries.forEach { registry ->
			log.info("Bootstrapping ${registry.id.key}")
			registry.boostrap()
			registry.getKeySet().allkeys().forEach { key -> key.checkBound() }
		}
	}

	val FLUID_TYPE = register(FluidTypeRegistry())
	val ATMOSPHERIC_GAS = register(AtmosphericGasRegistry())
	val CUSTOM_ITEMS = register(CustomItemRegistry())
	val CUSTOM_BLOCKS = register(CustomBlockRegistry())
	val ITEM_MODIFICATIONS = register(ItemModRegistry())
	val MULTIBLOCK_RECIPE = register(MultiblockRecipeRegistry())
	val TRANSPORT_NETWORK_NODE_TYPE = register(TransportNetworkNodeTypeRegistry())
	val FLUID_PROPERTY_TYPE = register(FluidPropertyTypeRegistry())
	val WRAPPED_LISTENER_TYPE = register(WrappedListenerRegistry())
	val WEATHER_TYPE = register(WeatherTypeRegistry())
	val WORLD_GENERATION_FEATURES = register(WorldGenerationFeatureRegistry())
	val WRECK_STRUCTURES = register(WreckStructureRegistry())

	private fun <T : Registry<*>> register(registry: T): T {
		byId[registry.id] = registry
		allRegistries.add(registry)
		return registry
	}

	operator fun get(registryID: IonBindableResourceKey<out Registry<*>>): Registry<*> = byId[registryID]!!
}
