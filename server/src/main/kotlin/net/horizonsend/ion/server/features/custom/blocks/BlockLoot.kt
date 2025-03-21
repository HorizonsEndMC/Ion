package net.horizonsend.ion.server.features.custom.blocks

import net.horizonsend.ion.server.core.registries.IonRegistryKey
import net.horizonsend.ion.server.core.registries.keys.CustomItemKeys
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.miscellaneous.utils.getMatchingMaterials
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

data class BlockLoot(
    val requiredTool: Supplier<Tool>? = Supplier { Tool.PICKAXE },
    val drops: Supplier<Collection<ItemStack>>,
    val silkTouchDrops: Supplier<Collection<ItemStack>> = drops,
) {
	fun getDrops(tool: ItemStack?, silkTouch: Boolean): Collection<ItemStack> {
		if (tool != null && requiredTool != null) {
			if (!requiredTool.get().matches(tool)) return listOf()
		}

		if (silkTouch) return silkTouchDrops.get().map { it.clone() }

		return drops.get().map { it.clone() }
	}

	companion object ToolPredicate {
		fun matchMaterial(material: Material): (ItemStack) -> Boolean {
			return { it.type == material }
		}

		fun matchAnyMaterial(materials: Iterable<Material>): (ItemStack) -> Boolean {
			return { materials.contains(it.type) }
		}

		fun customItem(customItem: IonRegistryKey<CustomItem>): (ItemStack) -> Boolean {
			return { it.customItem?.key == customItem }
		}
	}

	enum class Tool(vararg val checks: (ItemStack) -> Boolean) {
		PICKAXE(
			customItem(CustomItemKeys.POWER_DRILL_BASIC),
			matchAnyMaterial(getMatchingMaterials { it.name.endsWith("PICKAXE") })
		),
		SHOVEL(
			customItem(CustomItemKeys.POWER_DRILL_BASIC),
			matchAnyMaterial(getMatchingMaterials { it.name.endsWith("SHOVEL") })

		),
		AXE(
			customItem(CustomItemKeys.POWER_DRILL_BASIC),
			matchAnyMaterial(getMatchingMaterials { it.name.endsWith("AXE") })

		),
		SHEARS(
			customItem(CustomItemKeys.POWER_DRILL_BASIC),
			matchMaterial(Material.SHEARS)
		);

		fun matches(itemStack: ItemStack): Boolean = checks.any { it.invoke(itemStack) }
	}
}
