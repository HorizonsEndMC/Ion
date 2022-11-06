package net.horizonsend.ion.server.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.server.customitems.CustomItemList
import net.horizonsend.ion.server.utilities.feedback.FeedbackType
import net.horizonsend.ion.server.utilities.feedback.sendFeedbackMessage
import org.bukkit.entity.Player

@CommandAlias("IonCustomItem")
@Suppress("unused")
class IonCustomItemCommands() : BaseCommand(){
	@Default
	@CommandPermission("ion.customItem.get")
	fun onCustomItemGet(sender:Player, customItemList: CustomItemList, ammount: Int?){
		val itemToAdd = customItemList.itemStack
		if (ammount != null) {
			itemToAdd.amount = ammount
		}else itemToAdd.amount = 1
		sender.inventory.addItem(itemToAdd)
		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Successfully obtained ${itemToAdd.amount} ${customItemList.name}")
	}
}