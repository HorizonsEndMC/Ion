package net.horizonsend.ion.discord.command

import net.horizonsend.ion.discord.command.commands.DiscordInfoCommand
import net.horizonsend.ion.discord.command.commands.DiscordNationInfoCommand
import net.horizonsend.ion.discord.command.commands.DiscordPlayerInfoCommand
import net.horizonsend.ion.discord.command.commands.DiscordSettlementInfoCommand
import net.horizonsend.ion.discord.command.commands.PlayerListCommand

val discordCommands = listOf<IonDiscordCommand>(
	DiscordInfoCommand,
	DiscordNationInfoCommand,
	DiscordPlayerInfoCommand,
	DiscordSettlementInfoCommand,
	PlayerListCommand,
)
