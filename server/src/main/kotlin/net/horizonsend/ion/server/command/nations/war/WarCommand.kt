package net.horizonsend.ion.server.command.nations.war

import co.aikar.commands.annotation.CommandAlias
import net.horizonsend.ion.server.command.SLCommand

@CommandAlias("war")
object WarCommand : SLCommand() {
	fun onDeclare(): Nothing = TODO()

	fun onSurrender(): Nothing = TODO()

	/** Used by the defending nation to set their war goal. */
	fun onSetGoal(): Nothing = TODO()

	fun onRequestStalemate(): Nothing = TODO()
	fun onAcceptStalemate(): Nothing = TODO()

	fun onInfo(): Nothing = TODO()

	/** Gets all wars that the specified nation participated in */
	fun onList(): Nothing = TODO()
}
