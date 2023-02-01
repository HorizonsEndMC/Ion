package net.starlegacy.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import net.horizonsend.ion.server.legacy.feedback.FeedbackType
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackMessage
import net.starlegacy.command.SLCommand
import net.starlegacy.feature.misc.CustomItem
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object CustomItemCommand : SLCommand() {
	@Suppress("Unused")
	@CommandAlias("customitem")
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
			sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Gave {0}x {1} to {2}", amount, customItem.displayName, player.name)
		} else {
			val extra = result.values.sumOf { it.amount }
			sender.sendFeedbackMessage(FeedbackType.USER_ERROR, "Could not fit {0} out of the {1} items in {2}'s inventory!", extra, amount, player.name)
		}
	}
}
