package net.starlegacy.command.misc

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import net.horizonsend.ion.server.extensions.FeedbackType
import net.horizonsend.ion.server.extensions.sendFeedbackMessage
import net.starlegacy.command.SLCommand
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

object GlobalGameRuleCommand : SLCommand() {
	@Suppress("Unused")
	@CommandAlias("globalgamerule")
	@CommandPermission("slcore.globalgamerule")
	@CommandCompletion("@gamerules @nothing")
	fun onGlobalGameRule(sender: CommandSender, rule: String, value: String) {
		val worlds = Bukkit.getWorlds()

		worlds.forEach {
			if (!it.isGameRule(rule)) {
				throw InvalidCommandArgument("$rule is not a gamerule!")
			}

			if (!it.setGameRuleValue(rule, value)) {
				throw InvalidCommandArgument("$rule doesn't accept value $value!")
			}
		}

		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Set gamerule {0} to {1} in {2} worlds", rule, value, worlds.size)
	}

	override fun supportsVanilla(): Boolean {
		return true
	}
}
