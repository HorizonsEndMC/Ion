package net.horizonsend.ion.server.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.utilities.feedback.FeedbackType
import net.horizonsend.ion.common.utilities.feedback.sendFeedbackMessage
import net.horizonsend.ion.server.IonServer
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@CommandAlias("item")
class ItemCommand: BaseCommand() {
	@Default
	@Suppress("Unused")
	@CommandCompletion("@customItems")
	fun onCommand(sender: Player, itemName: String) {
		val item = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK)

		val customModelData = IonServer.customItems.values.find { it::class.simpleName == itemName }?.customModelData

		if (customModelData == null) {
			sender.sendFeedbackMessage(FeedbackType.USER_ERROR, "Item {0} does not exist.", itemName)
			return
		}

		item.itemMeta = item.itemMeta.apply { setCustomModelData(customModelData) }

		sender.inventory.addItem(item)
	}
}