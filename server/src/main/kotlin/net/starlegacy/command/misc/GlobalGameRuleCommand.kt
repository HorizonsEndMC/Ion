package net.starlegacy.command.misc

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import net.horizonsend.ion.common.extensions.success
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

		sender.success("Set gamerule $rule to $value in ${worlds.size} worlds")
	}

	override fun supportsVanilla(): Boolean {
		return true
	}
}
