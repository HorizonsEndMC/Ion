package net.horizonsend.ion.server.miscellaneous.utils

import io.papermc.paper.datacomponent.DataComponentType
import io.papermc.paper.datacomponent.DataComponentTypes
import net.horizonsend.ion.common.utils.text.BOLD
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.text.itemLore
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataContainer

/** Since empty items can either be null or air, extra work is needed to see if it's empty */
fun isEmpty(itemStack: ItemStack?): Boolean = itemStack == null || itemStack.type == Material.AIR

@Deprecated("Use Paper's ItemComponent API or helper functions")
fun ItemStack.updateMeta(block: (ItemMeta) -> Unit): ItemStack = apply {
	itemMeta = requireNotNull(itemMeta) { "No item meta for $type!" }.apply(block)
}

@Suppress("DEPRECATION")
fun ItemStack.updatePersistentDataContainer(edit: PersistentDataContainer.() -> Unit) = updateMeta { meta -> edit.invoke(meta.persistentDataContainer) }

fun <T : Any> ItemStack.updateData(type: DataComponentType.Valued<T>, data: T): ItemStack = apply { setData(type, data) }
fun ItemStack.updateData(type: DataComponentType.NonValued, value: Boolean): ItemStack = apply {
	if (value) setData(type) else unsetData(type)
}

fun ItemStack.setModel(model: Key) = updateData(DataComponentTypes.ITEM_MODEL, model)
fun ItemStack.setModel(model: String) = updateData(DataComponentTypes.ITEM_MODEL, NamespacedKeys.packKey(model))

@Deprecated("use components", ReplaceWith("setDisplayNameAndGet(component)"))
fun ItemStack.updateDisplayName(name: String): ItemStack = updateDisplayName(LegacyComponentSerializer.legacyAmpersand().deserialize(name))

fun ItemStack.setDisplayNameSimple(name: String): ItemStack = updateDisplayName(text(name).decoration(BOLD, false))

fun ItemStack.updateDisplayName(name: Component): ItemStack {
	setData(DataComponentTypes.CUSTOM_NAME, name.itemName)
	return this
}

@Deprecated("use components")
fun ItemStack.setLoreAndGetString(lines: List<String>): ItemStack = apply { this.lore = lines }

fun ItemStack.updateLore(lines: List<Component>): ItemStack = apply { this.lore(lines.map { it.itemLore }) }

val ItemStack.displayNameComponent: Component get() = if (hasItemMeta() && itemMeta.hasDisplayName()) { itemMeta.displayName() ?: displayName().hoverEvent(null) } else displayName().hoverEvent(null)
val ItemStack.displayNameString get() = displayNameComponent.plainText()
