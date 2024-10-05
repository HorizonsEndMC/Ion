package net.horizonsend.ion.server.features.custom.items.misc

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.gui.custom.item.PersonalTransporterGui
import net.horizonsend.ion.server.features.progression.Levels
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.CUSTOM_ITEM
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.STRING

open class PersonalTransporter(
    identifier: String,
    val customModelData: Int,
    val displayName: Component,
) : CustomItem(identifier) {
    val material = Material.WARPED_FUNGUS_ON_A_STICK

    override fun constructItemStack(): ItemStack {
        return ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
            it.setCustomModelData(customModelData)
            it.displayName(displayName.decoration(ITALIC, false))
            it.persistentDataContainer.set(CUSTOM_ITEM, STRING, identifier)
        }.apply { amount = 1 }
    }

    override fun handleSecondaryInteract(
        livingEntity: LivingEntity,
        itemStack: ItemStack,
        event: PlayerInteractEvent?
    ) {
        if (livingEntity is Player) {
            openTeleportMenu(livingEntity)
        }
    }

    private fun openTeleportMenu(player: Player) {
        if (Levels[player] > 10) {
            player.userError("Personal transporters can only be used by players that are level 10 or lower!")
            PersonalTransporterManager.removeItemFromPlayer(player)
            return
        } else {
            PersonalTransporterGui(player).openMainWindow()
        }
    }
}