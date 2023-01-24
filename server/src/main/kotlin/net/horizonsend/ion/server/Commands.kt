package net.horizonsend.ion.server

import net.horizonsend.ion.server.commands.BountyCommands
import net.horizonsend.ion.server.commands.ConfigurationCommands
import net.horizonsend.ion.server.commands.ControlCommands
import net.horizonsend.ion.server.commands.ConvertCommand
import net.horizonsend.ion.server.commands.CustomItemCommand
import net.horizonsend.ion.server.commands.SettingsCommand
import net.horizonsend.ion.server.commands.UtilityCommands
import net.horizonsend.ion.server.legacy.commands.AchievementsCommand

val commands = arrayOf(
	BountyCommands(),
	ConfigurationCommands(),
	ControlCommands(),
	ConvertCommand(),
	CustomItemCommand(),
	SettingsCommand(),
	UtilityCommands(),

	AchievementsCommand()
)
