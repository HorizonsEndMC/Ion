package net.horizonsend.ion.server.features.customitems

import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.CUSTOM_ITEM
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.STRING

abstract class CustomBlockItem(
    identifier: String,

    private val material: Material,
    private val customModelData: Int,
    val displayName: Component
) : CustomItem(identifier) {

    override fun constructItemStack(): ItemStack {
        return ItemStack(material).updateMeta {
            it.setCustomModelData(customModelData)
            it.displayName(displayName)
            it.persistentDataContainer.set(CUSTOM_ITEM, STRING, identifier)
        }
    }
}