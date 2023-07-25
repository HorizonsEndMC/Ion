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
import net.horizonsend.ion.server.features.misc.CustomItem
import net.horizonsend.ion.server.features.misc.CustomItems
import net.horizonsend.ion.server.miscellaneous.utils.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object CustomItemCommand : net.horizonsend.ion.server.command.SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandContexts.registerContext(CustomItem::class.java) { c: BukkitCommandExecutionContext ->
			val arg = c.popFirstArg()
			return@registerContext CustomItems[arg]
				?: throw InvalidCommandArgument("No custom item $arg found!")
		}

		registerStaticCompletion(manager, "customitems", CustomItems.all().joinToString("|") { it.id })
	}

	@CommandAlias("legacycustomitem")
	@CommandPermission("machinery.customitem")
	@CommandCompletion("@customitems 1|16|64 @players")
	fun onGive(
		sender: CommandSender,
		customItem: CustomItem,
		@Default("1") amount: Int,
		@Optional target: OnlinePlayer?
	) {
		val player = target?.player ?: sender as? Player ?: fail { "Console must specify a target player" }
		failIf(amount <= 0) { "Amount cannot be <= 0" }

		val item = customItem.itemStack(amount)
		val result = player.inventory.addItem(item)

		if (result.isEmpty()) {
			sender msg green("Gave ") +
				white("${amount}x ${customItem.displayName}") +
				green(" to ${player.name}")
		} else {
			val extra = result.values.sumOf { it.amount }
			sender msg red("Could not fit $extra out of the $amount items in ${player.name}'s inventory!")
		}
	}
}
