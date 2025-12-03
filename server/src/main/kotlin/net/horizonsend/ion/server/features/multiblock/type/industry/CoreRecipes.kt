package net.horizonsend.ion.server.features.multiblock.type.industry

import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ATAVUM
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.SCORDITE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.URANIUM_BLOCK
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.VANADIUM
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ZIRCON
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object CoreRecipes {
	val miniReactorRecipe: Map<ItemStack, Int> = mapOf(
		ItemStack(Material.IRON_BLOCK) to 32,
		ItemStack(URANIUM_BLOCK.getValue().constructItemStack()) to 32,
		ItemStack(Material.REDSTONE_BLOCK) to 8,
		ItemStack(SCORDITE.getValue().constructItemStack()) to 8
	)
	val smallReactorRecipe: Map<ItemStack, Int> = mapOf(
		ItemStack(Material.IRON_BLOCK) to 48,
		ItemStack(URANIUM_BLOCK.getValue().constructItemStack()) to 48,
		ItemStack(Material.REDSTONE_BLOCK) to 48,
		ItemStack(SCORDITE.getValue().constructItemStack()) to 16,
		ItemStack(VANADIUM.getValue().constructItemStack()) to 4
	)
	val mediumReactorRecipe: Map<ItemStack, Int> = mapOf(
		ItemStack(Material.IRON_BLOCK) to 48,
		ItemStack(URANIUM_BLOCK.getValue().constructItemStack()) to 48,
		ItemStack(Material.REDSTONE_BLOCK) to 48,
		ItemStack(SCORDITE.getValue().constructItemStack()) to 32,
		ItemStack(VANADIUM.getValue().constructItemStack()) to 8,
		ItemStack(ZIRCON.getValue().constructItemStack()) to 4
	)
	val largeReactorRecipe: Map<ItemStack, Int> = mapOf(
		ItemStack(Material.IRON_BLOCK) to 64,
		ItemStack(URANIUM_BLOCK.getValue().constructItemStack(64)) to 64,
		ItemStack(Material.REDSTONE_BLOCK) to 64,
		ItemStack(SCORDITE.getValue().constructItemStack()) to 32,
		ItemStack(VANADIUM.getValue().constructItemStack()) to 12,
		ItemStack(ZIRCON.getValue().constructItemStack()) to 8,
		ItemStack(ATAVUM.getValue().constructItemStack()) to 2
	)
	val cruiserReactorRecipe: Map<ItemStack, Int> = mapOf(
		ItemStack(Material.IRON_BLOCK) to 1280,
		ItemStack(URANIUM_BLOCK.getValue().constructItemStack()) to 640,
		ItemStack(Material.REDSTONE_BLOCK) to 320,
		ItemStack(SCORDITE.getValue().constructItemStack()) to 8,
		ItemStack(VANADIUM.getValue().constructItemStack()) to 2
	)
	val battlecruiserReactorRecipe: Map<ItemStack, Int> = mapOf(
		ItemStack(Material.IRON_BLOCK) to 1600,
		ItemStack(URANIUM_BLOCK.getValue().constructItemStack()) to 960,
		ItemStack(Material.REDSTONE_BLOCK) to 640,
		ItemStack(SCORDITE.getValue().constructItemStack()) to 16,
		ItemStack(VANADIUM.getValue().constructItemStack()) to 4
	)
}
