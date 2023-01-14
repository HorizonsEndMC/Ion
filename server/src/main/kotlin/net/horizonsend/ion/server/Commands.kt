package net.horizonsend.ion.server

import net.horizonsend.ion.server.commands.BountyCommands
import net.horizonsend.ion.server.commands.ControlCommands
import net.horizonsend.ion.server.legacy.commands.AchievementsCommand

val commands = arrayOf(
	BountyCommands(),
	ControlCommands(),
	AchievementsCommand()
)
