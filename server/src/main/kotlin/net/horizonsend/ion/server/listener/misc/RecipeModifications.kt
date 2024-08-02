package net.horizonsend.ion.server.listener.misc

import net.horizonsend.ion.server.listener.SLEventListener

object RecipeModifications : SLEventListener() {
	private val TOOL_UPGRADE_RECIPES = arrayOf(
		"power_drill_enhanced",
		"power_drill_advanced",
		"power_chainsaw_enhanced",
		"power_chainsaw_advanced",
		"power_hoe_enhanced",
		"power_hoe_advanced"
	)

	//TODO for when I figure out how to make the recipe take modded items
//	fun onToolUpgrade(event: CraftItemEvent) {
//		val recipe = (event.recipe as? CraftingRecipe) ?: return
//		val key = recipe.key.key
//
//		if (!TOOL_UPGRADE_RECIPES.contains(key)) return
//
//		val inventory = event.inventory
//		// Always in the center
//		val oldTool = inventory.getItem(4) ?: return
//		val oldCustomItem = oldTool.customItem
//
//		if (oldCustomItem !is ModdedPowerItem) return
//
//		val mods = oldCustomItem.getMods(oldTool)
//
//		val result = event.inventory.result ?: return
//		val resultCustom = result.customItem ?: return
//
//		if (resultCustom !is ModdedPowerItem) return
//
//		resultCustom.setMods(result, mods)
//	}
}
