package net.horizonsend.ion.server.command.nations.war

import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.WarCache
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.war.War
import net.horizonsend.ion.common.database.schema.nations.war.WarGoal
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.common.utils.text.lineBreak
import net.horizonsend.ion.common.utils.text.lineBreakWithCenterText
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.repeatString
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.nations.Wars
import net.horizonsend.ion.server.features.nations.Wars.warMessageTemplate
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.litote.kmongo.setValue

@CommandAlias("war|n war")
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
			val arg = context.popFirstArg()

			WarCache.all().firstOrNull { it.name.replace(" ", "_") == arg } ?: fail { "War $arg does not exist!" }
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

		if (!Wars.hasStalemateRequest(war.id, senderNation)) fail { "The other party has not requested a white peace!" }

		Wars.acceptStalemateRequest(sender, war.id, senderNation)
	}

	@Subcommand("info")
	@CommandCompletion("@activeWars")
	@Suppress("unused")
	fun onInfo(sender: CommandSender, war: WarCache.WarData) = asyncCommand(sender) {
		val name = Wars.importantWarMessageTemplate(war.name)
		val attackerAndGoal = warMessageTemplate("Aggressor: {0}. Goal: {1}", NationCache[war.aggressor].name, war.aggressorGoal)
		val defenderAndGoal = warMessageTemplate("Defender: {0}. Goal: {1}", NationCache[war.defender].name, war.defenderGoal)

		val aggressorCached = NationCache[war.aggressor]
		val aggressorColor = TextColor.color(aggressorCached.color)
		val defenderCached = NationCache[war.defender]
		val defenderColor = TextColor.color(defenderCached.color)

		val pointsNormalized = (war.points.toDouble() + 1000.0) / 2000.0

		val transition = "<transition:${aggressorColor.asHexString()}:${defenderColor.asHexString()}:$pointsNormalized>"

		val points = war.points.toCreditComponent() //TODO
		val started = war.points.toCreditComponent() //TODO
		val timeout = war.points.toCreditComponent() //TODO

		val text = ofChildren(
			lineBreakWithCenterText(name), newline(),
			miniMessage().deserialize("$transition${repeatString("█", 37)}</transition>\n"),
			attackerAndGoal, newline(),
			defenderAndGoal, newline(),
			points, newline(),
			started, newline(),
			timeout, newline(),
			lineBreak(52)
		)

		sender.sendMessage(text)
	}

	/** Gets all active wars */
	@Subcommand("list")
	@CommandCompletion("1|2|3|4|5")
	fun onList(sender: CommandSender, @Optional page: Int?) = asyncCommand(sender) {
		val active = WarCache.all().filter { it.result == null }

		sendWarList(sender, active, "Active Wars", page)
	}

	/** Gets all wars that the specified nation is participating in */
	@Subcommand("list")
	@CommandCompletion("@nations 1|2|3|4|5")
	fun onList(sender: Player, nation: String, @Optional page: Int?) = asyncCommand(sender) {
		val nationId = resolveNation(nation)
		val name = getNationName(nationId)

		val active = WarCache.all().filter {
			it.result == null &&
			(it.aggressor == nationId || it.defender == nationId)
		}

		sendWarList(sender, active, "$name's Active Wars", page)
	}

	/** Gets all wars that the specified nation has participated in */
	@Subcommand("history")
	@CommandCompletion("@nations 1|2|3|4|5")
	@Suppress("unused")
	fun onHistory(sender: Player, nation: String, @Optional page: Int?) = asyncCommand(sender) {
		val nationId = resolveNation(nation)
		val name = getNationName(nationId)

		val wars = WarCache.all().filter { (it.aggressor == nationId || it.defender == nationId) }

		sendWarList(sender, wars, "$name's Past Wars", page)
	}

	private fun sendWarList(
		sender: CommandSender,
		wars: List<WarCache.WarData>,
		message: String,
		page: Int?
	) {
		if ((page ?: 1) < 1) fail { "Can't have a negative page" }

		val builder = text()

		builder.append(lineBreakWithCenterText(warMessageTemplate(message)), newline())

		val menu = formatPaginatedMenu(
			wars.size,
			"/war list",
			page ?: 1,
			maxPerPage = 4,
			) {
			val war = wars[it]

			val name = Wars.importantWarMessageTemplate(war.name)
			val attackerAndGoal = warMessageTemplate("Aggressor: {0}. Goal: {1}", NationCache[war.aggressor].name, war.aggressorGoal)
			val defenderAndGoal = warMessageTemplate("Defender: {0}. Goal: {1}", NationCache[war.defender].name, war.defenderGoal)

			ofChildren(
				name, newline(),
				attackerAndGoal, newline(),
				defenderAndGoal, newline()
			)
		}

		builder.append(menu)

		sender.sendMessage(builder.build())
	}

	fun requireAttacker(war: Oid<War>, nation: Oid<Nation>) = failIf(WarCache[war].aggressor != nation) { "Your nation must be the aggressor in this war to do this!" }
	fun requireDefender(war: Oid<War>, nation: Oid<Nation>) = failIf(WarCache[war].defender != nation) { "Your nation must be the defender in this war to do this!" }
	fun requireParticipation(war: Oid<War>, nation: Oid<Nation>) = failIf(WarCache[war].defender != nation && WarCache[war].aggressor != nation) {
		"Your nation must participate in this war to do this!"
	}
}
