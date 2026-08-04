package net.horizonsend.ion.server.features.multiblock.type.industry

import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ATAVUM_BLOCK
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.REACTIVE_CHASSIS
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.REACTIVE_COMPONENT
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.REACTIVE_PLATING
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.SCORDITE_BLOCK
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.STEEL_ASSEMBLY
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.STEEL_MODULE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.URANIUM_CORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.VANADIUM_BLOCK
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ZIRCON_BLOCK
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object CoreRecipes {
	val miniReactorRecipe: Map<ItemStack, Int> = mapOf(
		ItemStack(Material.IRON_BLOCK) to 192,
		REACTIVE_PLATING.getValue().constructItemStack() to 4,
		SCORDITE_BLOCK.getValue().constructItemStack() to 12
	)
	val smallReactorRecipe: Map<ItemStack, Int> = mapOf(
		ItemStack(Material.IRON_BLOCK) to 576,
		REACTIVE_COMPONENT.getValue().constructItemStack() to 8,
		SCORDITE_BLOCK.getValue().constructItemStack() to 32,
		VANADIUM_BLOCK.getValue().constructItemStack() to 24
	)
	val mediumReactorRecipe: Map<ItemStack, Int> = mapOf(
		STEEL_MODULE.getValue().constructItemStack() to 48,
		REACTIVE_PLATING.getValue().constructItemStack() to 64,
		URANIUM_CORE.getValue().constructItemStack() to 24,
		SCORDITE_BLOCK.getValue().constructItemStack() to 96,
		VANADIUM_BLOCK.getValue().constructItemStack() to 48,
		ZIRCON_BLOCK.getValue().constructItemStack() to 24
	)
	val largeReactorRecipe: Map<ItemStack, Int> = mapOf(
		STEEL_ASSEMBLY.getValue().constructItemStack() to 48,
		REACTIVE_CHASSIS.getValue().constructItemStack() to 128,
		URANIUM_CORE.getValue().constructItemStack() to 128,
		SCORDITE_BLOCK.getValue().constructItemStack() to 256,
		VANADIUM_BLOCK.getValue().constructItemStack() to 160,
		ZIRCON_BLOCK.getValue().constructItemStack() to 48,
		ATAVUM_BLOCK.getValue().constructItemStack() to 16
	)
	val tier1ReactorRecipe: Map<ItemStack, Int> = mapOf(
		STEEL_MODULE.getValue().constructItemStack() to 48,
		REACTIVE_PLATING.getValue().constructItemStack() to 64,
		URANIUM_CORE.getValue().constructItemStack() to 24,
		SCORDITE_BLOCK.getValue().constructItemStack() to 96,
		VANADIUM_BLOCK.getValue().constructItemStack() to 48,
		ZIRCON_BLOCK.getValue().constructItemStack() to 24
	)
}
