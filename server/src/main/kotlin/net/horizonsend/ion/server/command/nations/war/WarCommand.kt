package net.horizonsend.ion.server.command.nations.war

import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.cache.nations.WarCache
import net.horizonsend.ion.common.database.schema.nations.war.WarGoal
import net.horizonsend.ion.server.command.SLCommand
import org.bukkit.entity.Player

@CommandAlias("war")
object WarCommand : SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandCompletions.registerAsyncCompletion("@activeWars") { _ ->
			WarCache.all()
				.filter { it.result == null }
				.map { it.name }
		}

		manager.commandCompletions.registerAsyncCompletion("@allWars") { _ ->
			WarCache.all().map { it.name }
		}

		manager.commandContexts.registerContext(WarCache.WarData::class.java) { context ->
			WarCache.all().firstOrNull { it.name == context.firstArg } ?: fail { "War ${context.firstArg} does not exist!" }
		}
	}

	@Subcommand("declare")
	@CommandCompletion("@nations")
	fun onDeclare(sender: Player, nation: String, goal: WarGoal, @Optional confirm: String?) = asyncCommand(sender) {


		if (confirm?.lowercase() != "confirm") fail { "To start the war, you must confirm. Run /war declare $nation $goal confirm to declare the war." }
	}

	@Subcommand("surrender")
	@CommandCompletion("@activeWars")
	fun onSurrender(sender: Player, war: WarCache.WarData) = asyncCommand(sender) {

	}

	@Subcommand("set goal")
	@CommandCompletion("@activeWars")
	/** Used by the defending nation to set their war goal. */
	fun onSetGoal(sender: Player) = asyncCommand(sender) {

	}

	@Subcommand("request stalemate")
	@CommandCompletion("@activeWars")
	fun onRequestStalemate(sender: Player) = asyncCommand(sender) {

	}

	@Subcommand("accept stalemate")
	@CommandCompletion("@activeWars")
	fun onAcceptStalemate(sender: Player) = asyncCommand(sender) {

	}

	@Subcommand("info")
	@CommandCompletion("@activeWars")
	fun onInfo(sender: Player) = asyncCommand(sender) {

	}

	/** Gets all active wars */
	@Subcommand("list")
	fun onList(sender: Player) = asyncCommand(sender) {

	}

	/** Gets all wars that the specified nation is participating in */
	@Subcommand("list")
	@CommandCompletion("@nations")
	fun onList(sender: Player, nation: String) = asyncCommand(sender) {

	}

	/** Gets all wars that the specified nation has participated in */
	@Subcommand("history")
	@CommandCompletion("@nations")
	fun onHistory(sender: Player, nation: String) = asyncCommand(sender) {

	}

	fun requireParticipation(): Nothing = TODO()
}
