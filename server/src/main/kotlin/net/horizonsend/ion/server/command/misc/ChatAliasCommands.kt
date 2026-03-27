package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import net.horizonsend.ion.server.command.SLCommand
import org.bukkit.entity.Player

/**
 * Registers chat channel aliases as real commands so clients receive them in Brigadier command trees.
 * No more annoying "Incomplete or unknown command." errors! :D
 *
 * Actual channel switching/message forwarding is handled by ChannelSelections via
 * PlayerCommandPreprocessEvent, which runs before normal command execution.
 */
@Description("Switch/send messages in chat channels")
@Suppress("Unused")
object ChatAliasCommands : SLCommand() {
	@CommandAlias(
		"global|g|local|l|planetchat|pchat|pc|systemchat|system|sy|" +
			"schat|sc|settlementchat|nchat|nc|nationchat|achat|ac|allychat|" +
			"crew|c|fleetchat|fc|fchat"
	)
	fun onAliasCommand(sender: Player) {
		// Intentionally empty, ChannelSelections handles the logic behind the chat channels.
	}

	@CommandPermission("group.staff")
	@CommandAlias(
		"admin|adminchat|staff|staffchat|mod|modchat|dev|devchat|" +
			"contentdesign|cd|slcd|vip|vipchat"
	)
	fun onStaffAliasCommand(sender: Player) {}
}

