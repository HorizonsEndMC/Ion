package net.horizonsend.ion.server.core.registration

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.server.core.registration.registries.AtmosphericGasRegistry
import net.horizonsend.ion.server.core.registration.registries.CustomBlockRegistry
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry
import net.horizonsend.ion.server.core.registration.registries.FluidTypeRegistry
import net.horizonsend.ion.server.core.registration.registries.ItemModRegistry
import net.horizonsend.ion.server.core.registration.registries.Registry
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

	private fun <T : Registry<*>> register(registry: T): T {
		byId[registry.id] = registry
		allRegistries.add(registry)
		return registry
	}

	operator fun get(registryID: IonBindableResourceKey<out Registry<*>>): Registry<*> = byId[registryID]!!
}
