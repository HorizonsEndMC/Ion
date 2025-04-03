package net.horizonsend.ion.server.core.registration.keys

import net.horizonsend.ion.server.core.registration.IonRegistries
import net.horizonsend.ion.server.core.registration.IonResourceKey
import net.horizonsend.ion.server.core.registration.registries.Registry
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import net.horizonsend.ion.server.features.gas.type.Gas
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.MultiblockRecipe
import net.horizonsend.ion.server.features.transport.fluids.FluidType

object RegistryKeys {
	val FLUID_TYPE = RegistryId<FluidType>("FLUID_TYPE")
	val ATMOSPHERIC_GAS = RegistryId<Gas>("ATMOSPHERIC_GAS")
	val CUSTOM_ITEMS = RegistryId<CustomItem>("CUSTOM_ITEMS")
	val CUSTOM_BLOCKS = RegistryId<CustomBlock>("CUSTOM_BLOCKS")
	val ITEM_MODIFICATIONS = RegistryId<ItemModification>("ITEM_MODIFICATIONS")
	val MULTIBLOCK_RECIPE = RegistryId<MultiblockRecipe<*>>("MULTIBLOCK_RECIPE")

	class RegistryId<T: Any>(key: String) : IonResourceKey<Registry<T>>(key) {
		@Suppress("UNCHECKED_CAST")
		override fun getValue(): Registry<T> = IonRegistries[this] as Registry<T>
	}
}
