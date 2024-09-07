package net.horizonsend.ion.server.miscellaneous.utils

import net.horizonsend.ion.common.utils.text.BOLD
import net.horizonsend.ion.common.utils.text.plainText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

/** Since empty items can either be null or air, extra work is needed to see if it's empty */
fun isEmpty(itemStack: ItemStack?): Boolean = itemStack == null || itemStack.type == Material.AIR

fun ItemStack.updateMeta(block: (ItemMeta) -> Unit): ItemStack = apply {
	itemMeta = requireNotNull(itemMeta) { "No item meta for $type!" }.apply(block)
}

@Deprecated("use components", ReplaceWith("setDisplayNameAndGet(component)"))
fun ItemStack.setDisplayNameAndGet(name: String): ItemStack = updateMeta { it.setDisplayName(name) }

fun ItemStack.setDisplayNameSimple(name: String): ItemStack = updateMeta { it.displayName(text(name).decoration(BOLD, false)) }

fun ItemStack.setDisplayNameAndGet(name: Component): ItemStack = updateMeta { it.displayName(name) }

val ItemStack.displayNameComponent: Component get() = if (hasItemMeta() && itemMeta.hasDisplayName()) { itemMeta.displayName() ?: displayName().hoverEvent(null) } else displayName().hoverEvent(null)
val ItemStack.displayNameString get() = displayNameComponent.plainText()

@Deprecated("use components")
fun ItemStack.setLoreAndGetString(lines: List<String>): ItemStack = apply { this.lore = lines }

fun ItemStack.setLoreAndGet(lines: List<Component>): ItemStack = apply { this.lore(lines) }

val leftArrow
	get() = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
		it.displayName(Component.empty())
		it.setCustomModelData(105)
	}

val rightArrow
	get() = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
		it.displayName(Component.empty())
		it.setCustomModelData(103)
	}
