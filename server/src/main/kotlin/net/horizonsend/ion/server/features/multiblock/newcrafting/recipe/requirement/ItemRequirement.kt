package net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.requirement

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

fun interface ItemRequirement : RecipeRequirement<ItemStack?> {
	override fun ensureAvailable(resource: ItemStack?): Boolean {
		return matches(resource)
	}

	fun matches(item: ItemStack?): Boolean

	fun consume(item: ItemStack) {
		item.amount--
	}

	class CustomItemRequirement(val customItem: CustomItem) : ItemRequirement {
		override fun matches(item: ItemStack?): Boolean {
			return item?.customItem == customItem && item.amount >= 1
		}
	}

	class MaterialRequirement(val material: Material) : ItemRequirement {
		override fun matches(item: ItemStack?): Boolean {
			return item?.type == material && item.amount >= 1
		}
	}

	class ItemStackRequirement(val itemStack: ItemStack) : ItemRequirement {
		override fun matches(item: ItemStack?): Boolean {
			return item != null && itemStack.isSimilar(item) && item.amount >= 1
		}
	}

	companion object {
		/** Gets an item requirement that requires a slot to be empty */
		fun empty() = ItemRequirement {
			if (it == null) return@ItemRequirement true
			it.isEmpty
		}
	}
}
