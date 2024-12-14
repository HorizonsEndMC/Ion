package net.horizonsend.ion.server.miscellaneous.utils

import io.papermc.paper.datacomponent.DataComponentType
import io.papermc.paper.datacomponent.DataComponentTypes
import net.horizonsend.ion.common.utils.text.BOLD
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

/** Since empty items can either be null or air, extra work is needed to see if it's empty */
fun isEmpty(itemStack: ItemStack?): Boolean = itemStack == null || itemStack.type == Material.AIR

@Deprecated("Use Paper's ItemComponent API or helper functions")
fun ItemStack.updateMeta(block: (ItemMeta) -> Unit): ItemStack = apply {
	itemMeta = requireNotNull(itemMeta) { "No item meta for $type!" }.apply(block)
}

fun <T : Any> ItemStack.applyData(type: DataComponentType.Valued<T>, data: T): ItemStack = apply { setData(type, data) }

fun ItemStack.setModel(model: Key) = applyData(DataComponentTypes.ITEM_MODEL, model)
fun ItemStack.setModel(model: String) = applyData(DataComponentTypes.ITEM_MODEL, NamespacedKeys.packKey(model))

@Deprecated("use components", ReplaceWith("setDisplayNameAndGet(component)"))
fun ItemStack.setDisplayNameAndGet(name: String): ItemStack = setDisplayNameAndGet(LegacyComponentSerializer.legacyAmpersand().deserialize(name))

fun ItemStack.setDisplayNameSimple(name: String): ItemStack = setDisplayNameAndGet(text(name).decoration(BOLD, false))

fun ItemStack.setDisplayNameAndGet(name: Component): ItemStack {
	setData(DataComponentTypes.CUSTOM_NAME, name)
	return this
}

val ItemStack.displayNameComponent: Component get() = if (hasItemMeta() && itemMeta.hasDisplayName()) { itemMeta.displayName() ?: displayName().hoverEvent(null) } else displayName().hoverEvent(null)
val ItemStack.displayNameString get() = displayNameComponent.plainText()

@Deprecated("use components")
fun ItemStack.setLoreAndGetString(lines: List<String>): ItemStack = apply { this.lore = lines }

fun ItemStack.setLoreAndGet(lines: List<Component>): ItemStack = apply { this.lore(lines) }
