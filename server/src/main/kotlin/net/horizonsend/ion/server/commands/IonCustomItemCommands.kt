package net.horizonsend.ion.server.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import net.horizonsend.ion.server.customitems.CustomItemList
import net.horizonsend.ion.server.legacy.feedback.FeedbackType
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackMessage
import org.bukkit.entity.Player

@CommandAlias("IonCustomItem")
@Suppress("unused")
class IonCustomItemCommands() : BaseCommand(){
	@Default
	@CommandPermission("ion.customItem.get")
	fun onCustomItemGet(sender:Player, customItemList: CustomItemList, @Optional amount: Int?){
		val itemToAdd = customItemList.itemStack
		if (amount != null) {
			itemToAdd.amount = amount
		}else itemToAdd.amount = 1
		sender.inventory.addItem(itemToAdd)
		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Successfully obtained ${itemToAdd.amount} ${customItemList.name}")
	}
}