package net.horizonsend.ion.server

import net.horizonsend.ion.server.commands.AsteroidCommand
import net.horizonsend.ion.server.commands.BountyCommands
import net.horizonsend.ion.server.legacy.commands.AchievementsCommand

val commands = arrayOf(
	BountyCommands(),
	AsteroidCommand(),
	AchievementsCommand()
)
