package net.horizonsend.ion.server.miscellaneous

import net.horizonsend.ion.server.IonCommand
import net.horizonsend.ion.server.configuration.ConfigurationCommands
import net.horizonsend.ion.server.features.achievements.AchievementsCommand
import net.horizonsend.ion.server.features.blasters.SettingsCommand
import net.horizonsend.ion.server.features.bounties.BountyCommands
import net.horizonsend.ion.server.features.customItems.commands.ConvertCommand
import net.horizonsend.ion.server.features.customItems.commands.CustomItemCommand

val commands = arrayOf(
	BountyCommands(),
	ConfigurationCommands(),
	ConvertCommand(),
	CustomItemCommand(),
	SettingsCommand(),
	IonCommand(),

	AchievementsCommand()
)
