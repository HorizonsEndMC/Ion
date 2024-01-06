package net.horizonsend.ion.server.command.nations.war

import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.WarCache
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.war.War
import net.horizonsend.ion.common.database.schema.nations.war.WarGoal
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.nations.Wars
import org.bukkit.entity.Player
import org.litote.kmongo.setValue

@CommandAlias("war")
object WarCommand : SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandCompletions.registerAsyncCompletion("@activeWars") { _ ->
			WarCache.all()
				.filter { it.result == null }
				.map { it.name.replace(" ", "_") }
		}

		manager.commandCompletions.registerAsyncCompletion("@allWars") { _ ->
			WarCache.all().map { it.name.replace(" ", "_") }
		}

		manager.commandContexts.registerContext(WarCache.WarData::class.java) { context ->
			WarCache.all().firstOrNull { it.name.replace(" ", "_") == context.popFirstArg() } ?: fail { "War ${context.firstArg} does not exist!" }
		}
	}

	@Subcommand("declare")
	@CommandCompletion("@nations")
	@Suppress("unused")
	fun onDeclare(sender: Player, nation: String, goal: WarGoal, @Optional confirm: String?) = asyncCommand(sender) {
		val senderNation = requireNationIn(sender)
		requireNationLeader(sender, senderNation)

		val nationId = resolveNation(nation)

		failIf(senderNation == nationId) { "You can't declare war on yourself." }

		if (confirm?.lowercase() != "confirm") fail { "To start the war, you must confirm. Run \"/war declare $nation $goal confirm\" to declare the war." }

		Wars.startWar(
			declaring = senderNation,
			defender = nationId,
			goal = goal
		)
	}

	@Subcommand("surrender")
	@CommandCompletion("@activeWars")
	@Suppress("unused")
	fun onSurrender(sender: Player, war: WarCache.WarData, @Optional confirm: String?) = asyncCommand(sender) {
		val senderNation = requireNationIn(sender)
		requireNationLeader(sender, senderNation)
		requireParticipation(war.id, senderNation)

		println(confirm)
		if (confirm?.lowercase() != "confirm") fail { "To surrender in the war, you must confirm. Run \"/war surrender ${war.name} confirm\" to declare the war." }

		Wars.surrender(war.id, senderNation)
	}

	@Subcommand("set goal")
	@CommandCompletion("@activeWars")
	@Suppress("unused")
	/** Used by the defending nation to set their war goal. */
	fun onSetGoal(sender: Player, war: WarCache.WarData, newGoal: WarGoal, @Optional confirm: String?) = asyncCommand(sender) {
		val senderNation = requireNationIn(sender)
		requireNationLeader(sender, senderNation)
		requireDefender(war.id, senderNation)

		val hasChanged = War.findPropsById(war.id, War::defenderHasSetGoal) ?: fail { "War $war was not in the database!" }
		failIf(hasChanged[War::defenderHasSetGoal]) { "Your nation has already set a goal! It cannot be changed." }

		if (confirm?.lowercase() != "confirm") fail { "To change your nation's war goal, you must confirm. Run \"/war set goal ${war.name} $newGoal confirm\" to declare the war." }

		War.updateById(war.id, setValue(War::defenderGoal, newGoal))
	}

	@Subcommand("request stalemate")
	@CommandCompletion("@activeWars")
	@Suppress("unused")
	fun onRequestStalemate(sender: Player, war: WarCache.WarData) = asyncCommand(sender) {
		val senderNation = requireNationIn(sender)
		requireNationLeader(sender, senderNation)
		requireParticipation(war.id, senderNation)

		val other = Wars.resolveOtherNation(war.id, senderNation)

		Wars.requestStalemate(sender, other, war.id)
	}

	@Subcommand("accept stalemate")
	@CommandCompletion("@activeWars")
	fun onAcceptStalemate(sender: Player, war: WarCache.WarData) = asyncCommand(sender) {
		val senderNation = requireNationIn(sender)
		requireNationLeader(sender, senderNation)


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

	fun requireAttacker(war: Oid<War>, nation: Oid<Nation>) = failIf(WarCache[war].aggressor != nation) { "Your nation must be the aggressor in this war to do this!" }
	fun requireDefender(war: Oid<War>, nation: Oid<Nation>) = failIf(WarCache[war].defender != nation) { "Your nation must be the defender in this war to do this!" }
	fun requireParticipation(war: Oid<War>, nation: Oid<Nation>) = failIf(WarCache[war].defender != nation && WarCache[war].aggressor != nation) {
		"Your nation must participate in this war to do this!"
	}
}
