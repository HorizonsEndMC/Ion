package net.horizonsend.ion.server.miscellaneous

import net.horizonsend.ion.server.IonCommand
import net.horizonsend.ion.server.configuration.ConfigurationCommands
import net.horizonsend.ion.server.features.achievements.AchievementsCommand
import net.horizonsend.ion.server.features.blasters.SettingsCommand
import net.horizonsend.ion.server.features.client.whereisit.SearchCommand
import net.horizonsend.ion.server.features.customItems.commands.ConvertCommand
import net.horizonsend.ion.server.features.customItems.commands.CustomItemCommand
import net.horizonsend.ion.server.features.customitems.commands.ConvertCommand
import net.horizonsend.ion.server.features.customitems.commands.CustomItemCommand
import net.horizonsend.ion.server.miscellaneous.commands.CalcExpCommand
import net.horizonsend.ion.server.miscellaneous.commands.CheckProtectionCommand

val commands = arrayOf(
	ConfigurationCommands(),
	ConvertCommand(),
	CustomItemCommand(),
	SettingsCommand(),
	IonCommand(),
	SearchCommand(),
	CalcExpCommand(),
	CheckProtectionCommand(),

	AchievementsCommand()
)
