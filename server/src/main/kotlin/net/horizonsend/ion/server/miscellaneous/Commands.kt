package net.horizonsend.ion.server.miscellaneous

import net.horizonsend.ion.server.IonCommand
import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.horizonsend.ion.server.configuration.ConfigurationCommands
import net.horizonsend.ion.server.features.achievements.AchievementsCommand
import net.horizonsend.ion.server.features.blasters.SettingsCommand
import net.horizonsend.ion.server.features.bounties.BountyCommands
import net.horizonsend.ion.server.features.client.whereisit.SearchCommand
import net.horizonsend.ion.server.features.customItems.commands.ConvertCommand
import net.horizonsend.ion.server.features.customItems.commands.CustomItemCommand
import net.horizonsend.ion.server.features.generation.AsteroidCommand
import net.horizonsend.ion.server.features.whereisit.SearchCommand
import net.horizonsend.ion.server.miscellaneous.commands.CalcExpCommand

val commands = arrayOf(
	AsteroidCommand(Ion.configuration),
	BountyCommands(),
	ConfigurationCommands(),
	ConvertCommand(),
	CustomItemCommand(),
	SettingsCommand(),
	IonCommand(),
	SearchCommand(),
	CalcExpCommand(),

	AchievementsCommand()
)
