package net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.item

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.multiblock.crafting.input.RecipeEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.RecipeRequirement
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

fun interface ItemRequirement : RecipeRequirement<ItemStack?> {
	override fun ensureAvailable(resource: ItemStack?): Boolean {
		return matches(resource)
	}

	fun matches(item: ItemStack?): Boolean

	fun consume(item: ItemStack, environment: RecipeEnviornment) {
		item.amount--
	}

	open fun asItemStack(): ItemStack? = null

	class CustomItemRequirement(val customItem: CustomItem) : ItemRequirement {
		override fun matches(item: ItemStack?): Boolean {
			return item?.customItem == customItem && item.amount >= 1
		}

		override fun asItemStack(): ItemStack = customItem.constructItemStack()
	}

	class MaterialRequirement(val material: Material, val count: Int = 1) : ItemRequirement {
		override fun matches(item: ItemStack?): Boolean {
			return item?.type == material && item.amount >= count
		}

		override fun consume(item: ItemStack, environment: RecipeEnviornment) {
			item.amount -= count
		}

		override fun asItemStack(): ItemStack = ItemStack(material)
	}

	class ItemStackRequirement(val itemStack: ItemStack) : ItemRequirement {
		override fun matches(item: ItemStack?): Boolean {
			return item != null && itemStack.isSimilar(item) && item.amount >= 1
		}

		override fun asItemStack(): ItemStack = itemStack.clone()
	}

	companion object {
		/** Gets an item requirement that requires a slot to be empty */
		fun empty() = EmptyRequirement

		/** Gets an item requirement is always true */
		fun ignore() = ItemRequirement { true }

		fun prismarine() = MaterialRequirement(Material.PRISMARINE_CRYSTALS, count = 0)

		/** Gets a composite requirement where it could be prismarine crystals or empty */
		fun legacy() = any(prismarine(), empty())

		/** Gets a composite requirement where any condition could be met */
		fun any(vararg requirements: ItemRequirement) = AnyRequirement(*requirements)

		/** Gets a composite requirement where all conditions must be met */
		fun all(vararg requirements: ItemRequirement) = AllRequirements(*requirements)

		class AnyRequirement(vararg  val requirements: ItemRequirement) : ItemRequirement {
			init {
			    check(requirements.isNotEmpty())
			}

			override fun matches(item: ItemStack?): Boolean {
				return requirements.any { requirement -> requirement.matches(item) }
			}

			override fun consume(item: ItemStack, environment: RecipeEnviornment) {
				requirements.first { requirement -> requirement.matches(item) }.consume(item, environment)
			}

			override fun asItemStack(): ItemStack? = requirements.first().asItemStack()
		}

		class AllRequirements(vararg  val requirements: ItemRequirement) : ItemRequirement {
			init {
				check(requirements.isNotEmpty())
			}

			override fun matches(item: ItemStack?): Boolean {
				return requirements.all { requirement -> requirement.matches(item) }
			}

			override fun consume(item: ItemStack, environment: RecipeEnviornment) {
				requirements.first { requirement -> requirement.matches(item) }.consume(item, environment)
			}

			override fun asItemStack(): ItemStack? = requirements.first().asItemStack()
		}

		data object EmptyRequirement: ItemRequirement {
			override fun matches(item: ItemStack?): Boolean {
				if (item == null) return true
				return item.isEmpty
			}

			override fun consume(item: ItemStack, environment: RecipeEnviornment) {
				return
			}
		}
	}
}
