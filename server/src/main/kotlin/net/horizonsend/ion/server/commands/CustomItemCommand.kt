package net.horizonsend.ion.server.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Values
import net.horizonsend.ion.server.extensions.sendInformation
import net.horizonsend.ion.server.items.CustomItems
import org.bukkit.entity.Player

@CommandAlias("customitem")
class CustomItemCommand : BaseCommand() {
	@Default
	@Suppress("Unused")
	@CommandCompletion("@customItem")
	@CommandPermission("ion.customitem")
	fun onCustomItemCommand(sender: Player, @Values("@customItem") customItem: String, @Optional amount: Int?) {
		val itemStack = CustomItems.getByIdentifier(customItem)?.constructItemStack() ?: return
		itemStack.amount = amount ?: 1

		sender.inventory.addItem(itemStack)
		sender.sendInformation("Added ${itemStack.amount}x $customItem to inventory")
	}
}