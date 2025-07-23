package net.horizonsend.ion.server.command.admin

import co.aikar.commands.BukkitCommandExecutionContext
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.core.registries.keys.CustomItemKeys
import net.horizonsend.ion.server.features.custom.items.CustomItem
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("customitem|legacycustomitem")
object CustomItemCommand : SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandCompletions.registerCompletion("customItem") { _ -> CustomItemKeys.allStrings() }
		manager.commandContexts.registerContext(CustomItem::class.java) { c: BukkitCommandExecutionContext ->
			val arg = c.popFirstArg()
			return@registerContext CustomItemKeys[arg]?.getValue() ?: throw InvalidCommandArgument("No custom item $arg found!")
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
		val player = target?.player ?: sender as? Player ?: fail { "Console must specify a target player" }

		val itemStack = CustomItemKeys[customItem]?.getValue()?.constructItemStack()

		if (itemStack == null) {
			player.userError("No custom item $customItem found!")
			return
		}

		if (amount != null && amount <= 0) return player.userError("Amount cannot be less than 0!")

		itemStack.amount = amount ?: 1

		player.inventory.addItem(itemStack)
		player.information("Added ${itemStack.amount}x $customItem to inventory")
	}
}
