package net.horizonsend.ion.server.features.multiblock.type.industry

import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ATAVUM
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.SCORDITE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.URANIUM_BLOCK
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.VANADIUM
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ZIRCON
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ATAVUM
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object CoreRecipes {
	val miniReactorRecipe: Map<ItemStack, Int> = mapOf(
		ItemStack(Material.IRON_BLOCK) to 32,
		URANIUM_BLOCK.getValue().constructItemStack() to 32,
		ItemStack(Material.REDSTONE_BLOCK) to 8,
		SCORDITE.getValue().constructItemStack() to 8
	)
	val smallReactorRecipe: Map<ItemStack, Int> = mapOf(
		ItemStack(Material.IRON_BLOCK) to 48,
		URANIUM_BLOCK.getValue().constructItemStack() to 48,
		ItemStack(Material.REDSTONE_BLOCK) to 48,
		SCORDITE.getValue().constructItemStack() to 16,
		VANADIUM.getValue().constructItemStack() to 4
	)
	val mediumReactorRecipe: Map<ItemStack, Int> = mapOf(
		ItemStack(Material.IRON_BLOCK) to 48,
		URANIUM_BLOCK.getValue().constructItemStack() to 48,
		ItemStack(Material.REDSTONE_BLOCK) to 48,
		SCORDITE.getValue().constructItemStack() to 32,
		VANADIUM.getValue().constructItemStack() to 8,
		ZIRCON.getValue().constructItemStack() to 4
	)
	val largeReactorRecipe: Map<ItemStack, Int> = mapOf(
		ItemStack(Material.IRON_BLOCK) to 64,
		URANIUM_BLOCK.getValue().constructItemStack() to 64,
		ItemStack(Material.REDSTONE_BLOCK) to 64,
		SCORDITE.getValue().constructItemStack() to 32,
		VANADIUM.getValue().constructItemStack() to 12,
		ZIRCON.getValue().constructItemStack() to 8,
		ATAVUM.getValue().constructItemStack() to 2
	)
	val cruiserReactorRecipe: Map<ItemStack, Int> = mapOf(
		ItemStack(Material.IRON_BLOCK) to 1280,
		URANIUM_BLOCK.getValue().constructItemStack() to 640,
		ItemStack(Material.REDSTONE_BLOCK) to 320,
		SCORDITE.getValue().constructItemStack() to 8,
		VANADIUM.getValue().constructItemStack() to 2
	)
	val battlecruiserReactorRecipe: Map<ItemStack, Int> = mapOf(
		ItemStack(Material.IRON_BLOCK) to 1600,
		URANIUM_BLOCK.getValue().constructItemStack() to 960,
		ItemStack(Material.REDSTONE_BLOCK) to 640,
		SCORDITE.getValue().constructItemStack() to 16,
		VANADIUM.getValue().constructItemStack() to 4,
		ATAVUM.getValue().constructItemStack() to 2
	)
}
