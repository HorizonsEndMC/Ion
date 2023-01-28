package net.horizonsend.ion.server.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Values
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import net.horizonsend.ion.server.extensions.sendInformation
import net.horizonsend.ion.server.items.CustomItems
import org.bukkit.entity.Player

@CommandAlias("customitem")
class CustomItemCommand : BaseCommand() {
	@Default
	@Suppress("Unused")
	@CommandCompletion("@customItem")
	@CommandPermission("ion.customitem")
	fun onCustomItemCommand(
		@Optional sender: Player,
		@Values("@customItem") customItem: String,
		@Optional amount: Int?,
		@Optional target: OnlinePlayer?
	) {
		val player = target?.player ?: sender as? Player ?: throw Throwable("Console must specify a target player")

		val itemStack = CustomItems.getByIdentifier(customItem)?.constructItemStack() ?: return
		itemStack.amount = amount ?: 1

		player.inventory.addItem(itemStack)
		player.sendInformation("Added ${itemStack.amount}x $customItem to inventory")
	}
}
