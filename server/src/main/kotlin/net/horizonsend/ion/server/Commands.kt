package net.horizonsend.ion.server

import net.horizonsend.ion.server.commands.BountyCommands
import net.horizonsend.ion.server.commands.IonCustomItemCommands
import net.horizonsend.ion.server.commands.PatreonCommands
import net.horizonsend.ion.server.commands.UtilityCommands
import net.horizonsend.ion.server.legacy.commands.AchievementsCommand

val commands = arrayOf(
	BountyCommands(),
	IonCustomItemCommands(),
	PatreonCommands(),
	UtilityCommands(),

	AchievementsCommand()
)
