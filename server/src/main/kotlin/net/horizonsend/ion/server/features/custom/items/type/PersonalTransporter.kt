package net.horizonsend.ion.server.features.custom.items.type

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.component.Listener
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.gui.custom.item.PersonalTransporterGui
import net.horizonsend.ion.server.features.progression.Levels
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object PersonalTransporter : CustomItem(
	"PERSONAL_TRANSPORTER",
	text("Personal Transporter", GOLD).itemName,
	ItemFactory.unStackableCustomItem("throwables/personal_transporter")
) {
	override val customComponents: CustomItemComponentManager = CustomItemComponentManager().apply {
		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, Listener.rightClickListener(this@PersonalTransporter) { event, _, _ ->
			onRightClick(event.player)
		})
	}

	override fun assembleLore(itemStack: ItemStack): List<Component> {
		return listOf(text("Select a player to request to teleport to them. One-time use", GRAY))
	}

    private fun onRightClick(livingEntity: LivingEntity) {
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
