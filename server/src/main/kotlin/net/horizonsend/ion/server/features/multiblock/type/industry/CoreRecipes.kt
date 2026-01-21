package net.horizonsend.ion.server.features.multiblock.type.industry

import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ATAVUM_BLOCK
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.SCORDITE_BLOCK
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.URANIUM_BLOCK
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.VANADIUM_BLOCK
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ZIRCON_BLOCK
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object CoreRecipes {
	val miniReactorRecipe: Map<ItemStack, Int> = mapOf(
		ItemStack(Material.IRON_BLOCK) to 32,
		URANIUM_BLOCK.getValue().constructItemStack() to 32,
		ItemStack(Material.REDSTONE_BLOCK) to 8,
		SCORDITE_BLOCK.getValue().constructItemStack() to 8
	)
	val smallReactorRecipe: Map<ItemStack, Int> = mapOf(
		ItemStack(Material.IRON_BLOCK) to 48,
		URANIUM_BLOCK.getValue().constructItemStack() to 48,
		ItemStack(Material.REDSTONE_BLOCK) to 48,
		SCORDITE_BLOCK.getValue().constructItemStack() to 16,
		VANADIUM_BLOCK.getValue().constructItemStack() to 4
	)
	val mediumReactorRecipe: Map<ItemStack, Int> = mapOf(
		ItemStack(Material.IRON_BLOCK) to 48,
		URANIUM_BLOCK.getValue().constructItemStack() to 48,
		ItemStack(Material.REDSTONE_BLOCK) to 48,
		SCORDITE_BLOCK.getValue().constructItemStack() to 32,
		VANADIUM_BLOCK.getValue().constructItemStack() to 8,
		ZIRCON_BLOCK.getValue().constructItemStack() to 4
	)
	val largeReactorRecipe: Map<ItemStack, Int> = mapOf(
		ItemStack(Material.IRON_BLOCK) to 64,
		URANIUM_BLOCK.getValue().constructItemStack() to 64,
		ItemStack(Material.REDSTONE_BLOCK) to 64,
		SCORDITE_BLOCK.getValue().constructItemStack() to 32,
		VANADIUM_BLOCK.getValue().constructItemStack() to 12,
		ZIRCON_BLOCK.getValue().constructItemStack() to 8,
		ATAVUM_BLOCK.getValue().constructItemStack() to 2
	)
	val cruiserReactorRecipe: Map<ItemStack, Int> = mapOf(
		ItemStack(Material.IRON_BLOCK) to 20,
		URANIUM_BLOCK.getValue().constructItemStack() to 10,
		ItemStack(Material.REDSTONE_BLOCK) to 5,
		SCORDITE_BLOCK.getValue().constructItemStack() to 8,
		VANADIUM_BLOCK.getValue().constructItemStack() to 2
	)
	val battlecruiserReactorRecipe: Map<ItemStack, Int> = mapOf(
		ItemStack(Material.IRON_BLOCK) to 25,
		URANIUM_BLOCK.getValue().constructItemStack() to 15,
		ItemStack(Material.REDSTONE_BLOCK) to 5,
		SCORDITE_BLOCK.getValue().constructItemStack() to 16,
		VANADIUM_BLOCK.getValue().constructItemStack() to 4
	)
}
