package net.horizonsend.ion.server.features.customitems.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.customitems.CustomItems
import net.horizonsend.ion.server.features.customitems.CustomItems.getByIdentifier
import net.starlegacy.command.SLCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("customitem")
object CustomItemCommand : SLCommand() {
	override fun onEnable(commandManager: PaperCommandManager) {
		commandManager.commandCompletions.registerCompletion("customItem") { context ->
			CustomItems.identifiers.filter { context.player.hasPermission("ion.customitem.$it") }
		}
	}

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

		val itemStack = getByIdentifier(customItem)?.constructItemStack()
		if (itemStack == null) {
			player.userError("No custom item $customItem found!")
			return
		}

		if (amount != null && amount <= 0) {
			player.userError("Amount cannot be less than 0!")
			return
		}
		itemStack.amount = amount ?: 1

		player.inventory.addItem(itemStack)
		player.information("Added ${itemStack.amount}x $customItem to inventory")
	}
}
