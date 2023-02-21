package net.horizonsend.ion.server.features.customItems.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import net.horizonsend.ion.server.features.customItems.CustomItems
import net.horizonsend.ion.server.miscellaneous.extensions.information
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("customitem")
class CustomItemCommand : BaseCommand() {
	@Default
	@Suppress("Unused")
	@CommandPermission("ion.customitem")
	@CommandCompletion("@customItem")
	fun onCustomItemCommand(
		sender: CommandSender,
		customItem: String,
		@Optional amount: Int?,
		@Optional target: OnlinePlayer?
	) {
		val player = target?.player ?: sender as? Player ?: throw Throwable("Console must specify a target player")

		val itemStack = CustomItems.getByIdentifier(customItem)?.constructItemStack() ?: return
		itemStack.amount = amount ?: 1

		player.inventory.addItem(itemStack)
		player.information("Added ${itemStack.amount}x $customItem to inventory")
	}
}
