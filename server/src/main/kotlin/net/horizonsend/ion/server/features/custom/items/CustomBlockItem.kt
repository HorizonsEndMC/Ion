package net.horizonsend.ion.server.features.custom.items

import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.CUSTOM_ITEM
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.STRING

open class CustomBlockItem(
    identifier: String,

    private val material: Material,
    private val customModelData: Int,
    val displayName: Component,

    val customBlockIdentifier: String
) : net.horizonsend.ion.server.features.custom.items.CustomItem(identifier) {

    fun getCustomBlock(): CustomBlock? {
        return CustomBlocks.getByIdentifier(customBlockIdentifier)
    }

    override fun constructItemStack(): ItemStack {
        return ItemStack(material).updateMeta {
            it.setCustomModelData(customModelData)
            it.displayName(displayName)
            it.persistentDataContainer.set(CUSTOM_ITEM, STRING, identifier)
        }
    }
}
