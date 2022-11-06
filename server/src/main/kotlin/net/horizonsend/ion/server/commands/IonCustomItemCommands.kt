package net.horizonsend.ion.server.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
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