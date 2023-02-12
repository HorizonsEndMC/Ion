package net.starlegacy.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import net.starlegacy.command.SLCommand
import net.starlegacy.feature.misc.CustomItem
import net.starlegacy.util.green
import net.starlegacy.util.msg
import net.starlegacy.util.plus
import net.starlegacy.util.red
import net.starlegacy.util.white
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object CustomItemCommand : SLCommand() {
	@CommandAlias("legacycustomitem")
	@CommandPermission("machinery.customitem")
	@CommandCompletion("@legacycustomitems 1|16|64 @players")
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
