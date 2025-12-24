package net.horizonsend.ion.server.command.nations

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.FrontierNationCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.FrontierNation
import net.horizonsend.ion.common.database.schema.nations.FrontierNationRole
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.common.utils.text.lineBreak
import net.horizonsend.ion.common.utils.text.lineBreakWithCenterText
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.repeatString
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.chat.Discord
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.distance
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextColor.color
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Color
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.litote.kmongo.EMPTY_BSON
import org.litote.kmongo.ne
import java.util.Date
import kotlin.math.roundToInt

@CommandAlias("frontiernation|fn")
object FrontierNationCommand : SLCommand() {
	private val nationsMessageColor = TextColor.fromHexString("#FC3200")
	private val nationsImportantMessageColor = TextColor.fromHexString("#FC9300")
	private fun nationMessageFormat(text: String, vararg args: Any?) = template(text(text, nationsMessageColor), false, *args)
	private fun nationImportantMessageFormat(text: String, vararg args: Any?) = template(text(text, nationsImportantMessageColor), false, *args)

	override fun onEnable(manager: PaperCommandManager) {
		registerAsyncCompletion(manager, "frontierNations") { _ -> FrontierNationCache.all().map { it.name } }
	}

	private fun validateName(name: String, nationId: Oid<FrontierNation>?) {
		if (!"\\w*".toRegex().matches(name)) {
			throw InvalidCommandArgument("Name must be alphanumeric")
		}

		if (name.length < 3) {
			throw InvalidCommandArgument("Name cannot be less than 3 characters")
		}

		if (name.length > 40) {
			throw InvalidCommandArgument("Name cannot be more than 40 characters")
		}

		val existingNation: Oid<FrontierNation>? = FrontierNationCache.getByName(name)
		if (existingNation != null && (nationId == null || nationId != existingNation)) {
			throw InvalidCommandArgument("A nation named $name already exists.")
		}
	}

	private fun validateColor(red: Int, green: Int, blue: Int, nationId: Oid<FrontierNation>?): Color {
		failIf(sequenceOf(red, green, blue).any { it !in 0..255 }) { "Red, green, and blue must be integers within 0-255" }

		val color = Color.fromRGB(red, green, blue)

		val query = if (nationId == null) EMPTY_BSON else FrontierNation::_id ne nationId

		for (results in FrontierNation.findProps(query, FrontierNation::name, FrontierNation::color)) {
			val nationName = results[FrontierNation::name]
			val nationColor = Color.fromRGB(results[FrontierNation::color])

			val r1 = color.red.toDouble()
			val g1 = color.green.toDouble()
			val b1 = color.blue.toDouble()
			val r2 = nationColor.red.toDouble()
			val g2 = nationColor.green.toDouble()
			val b2 = nationColor.blue.toDouble()
			val distance = distance(r1, g1, b1, r2, g2, b2)

			failIf(distance < 10) { "That color is too similar to the color of the nation $nationName! Distance: $distance" }

			log.info("Distance from $nationName: $distance")
		}

		return color
	}

	@Subcommand("create")
	@CommandCompletion("@nothing @range:1-255 @range:0-255 @range:0-255")
	@Description("Create a nation. Color values must be RGB color values, each from 0-255")
	fun onCreate(sender: Player, name: String, red: Int, green: Int, blue: Int) = asyncCommand(sender) {
		requireEconomyEnabled()

		requireNotInFrontierNation(sender)
		validateName(name, null)
		val color = validateColor(red, green, blue, nationId = null)

		FrontierNation.create(name, sender.slPlayerId, color.asRGB())

		Notify.chatAndGlobal(nationImportantMessageFormat(
			"{0} founded the frontier nation {1}!",
			sender.name,
			name
		))

		val embed = Embed(
			title = "${sender.name} founded the frontier nation $name!",
			color = color.asRGB(),
		)

		Discord.sendEmbed(ConfigurationFiles.discordSettings().eventsChannel, embed)
	}

	@Subcommand("disband")
	@Description("Disband your nation (this cannot be undone!)")
	fun onDisband(sender: Player, @Optional name: String?) = asyncCommand(sender) {
		val nation = requireFrontierNationIn(sender)
		requireFrontierNationLeader(sender, nation)

		val nationName = getFrontierNationName(nation)
		failIf(name != nationName) { "To disband your nation, you must confirm by specifying the name. Run the command: /fn disband $nationName" }

		FrontierNation.delete(nation)

		Notify.chatAndEvents(nationImportantMessageFormat(
			"The nation {0} has been disbanded by its leader {1}",
			nationName,
			sender.name
		))
	}

	@Subcommand("invite")
	@CommandCompletion("@players")
	@Description("Invite a player to your nation")
	fun onInvite(sender: Player, otherPlayer: String) = asyncCommand(sender) {
		val nationId = requireFrontierNationIn(sender)
		requireFrontierNationPermission(sender, nationId, FrontierNationRole.Permission.PLAYER_INVITE)


		val playerId = resolveOfflinePlayer(otherPlayer)
		val nationName = getFrontierNationName(nationId)

		if (!FrontierNation.isInvited(nationId, playerId.slPlayerId)) {
			FrontierNation.addInvite(nationId, playerId.slPlayerId)
			sender.success("Invited $otherPlayer to your nation")

			Notify.playerCrossServer(
				player = playerId,
				message = nationImportantMessageFormat(
					"You have been invited to the nation {0}! To accept, use {1}",
					nationName,
					text("/frontiernation join $nationName", YELLOW, TextDecoration.ITALIC)
						.hoverEvent(text("Click to run /frontiernation join $nationName"))
						.clickEvent(ClickEvent.runCommand("/frontiernation join $nationName"))
				)
			)
		} else {
			FrontierNation.removeInvite(nationId, playerId.slPlayerId)
			sender.success("Cancelled invite for $otherPlayer to your nation")

			Notify.playerCrossServer(
				player = playerId,
				message = nationImportantMessageFormat(
					"Your invitaiton to join the nation {0} has been revoked by {1}",
					nationName,
					sender.name
				)
			)
		}
	}

	@Subcommand("invites")
	fun onInvites(sender: Player, @Optional page: Int?) = asyncCommand(sender) {
		val nationId = requireFrontierNationIn(sender)
		requireFrontierNationPermission(sender, nationId, FrontierNationRole.Permission.PLAYER_INVITE)

		val invitedPlayers = FrontierNationCache[nationId].invites.toList()

		val body = formatPaginatedMenu(
			entries = invitedPlayers.count(),
			command = "/frontiernation invites",
			currentPage = page ?: 1,
		) {
			val playerId = invitedPlayers[it]
			val player = SLPlayer[playerId]?.lastKnownName ?: "Unknown"

			text(player, YELLOW)
		}

		sender.sendMessage(ofChildren(
			lineBreakWithCenterText(text("Invites", YELLOW)), Component.newline(),
			body
		))
	}

	@Subcommand("join")
	@CommandCompletion("@frontierNations")
	@Description("Join a nation which you're invited to")
	fun onJoin(sender: Player, nation: String) = asyncCommand(sender) {
		requireNotInFrontierNation(sender)
		val nationId: Oid<FrontierNation> = resolveFrontierNation(nation)

		val nationName = getFrontierNationName(nationId)

		failIf(!FrontierNation.isInvited(nationId, sender.slPlayerId)) { "You are not invited to $nationName" }

		FrontierNation.removeInvite(nationId, sender.slPlayerId)
		SLPlayer.joinFrontierNation(sender.slPlayerId, nationId)

		Notify.chatAndEvents(nationImportantMessageFormat("{0} joined the nation {1}", sender.name, nationName))
	}

	@Subcommand("leave")
	@Description("Leave the nation you're in")
	fun onLeave(sender: Player, @Optional nation: String?) = asyncCommand(sender) {
		val nationId = requireFrontierNationIn(sender)
		val nationName = getFrontierNationName(nationId)

		failIf(nationName != nation) { "You need to confirm using the name of the nation. Run the command: /fn leave $nationName" }

		SLPlayer.leaveFrontierNation(sender.slPlayerId)

		Notify.chatAndEvents(nationImportantMessageFormat("{0} left the nation {1}!", sender.name, nationName))
	}

	@Subcommand("kick")
	@Description("Kick a player from your nation")
	@CommandCompletion("@players")
	fun onKick(sender: Player, otherPlayer: String) = asyncCommand(sender) {
		val nationId = requireFrontierNationIn(sender)
		requireFrontierNationPermission(sender, nationId, FrontierNationRole.Permission.PLAYER_KICK)

		val playerId = resolveOfflinePlayer(otherPlayer)

		failIf(PlayerCache[playerId.slPlayerId].frontierNationOid != nationId) { "$otherPlayer is not in your nation" }

		SLPlayer.leaveFrontierNation(playerId.slPlayerId)

		Notify.chatAndEvents(nationImportantMessageFormat("{0} kicked {1} from the nation {2}!", sender.name, otherPlayer, getFrontierNationName(nationId)))
	}

	@Subcommand("set name")
	@Description("Rename your nation")
	fun onSetName(sender: Player, newName: String) = asyncCommand(sender) {
		val nationId = requireFrontierNationIn(sender)
		requireFrontierNationLeader(sender, nationId)
		validateName(newName, nationId)

		val oldName = getFrontierNationName(nationId)
		failIf(oldName == newName) { "Your nation is already named $oldName"}

		FrontierNation.setName(nationId, newName)

		Notify.chatAndEvents(nationMessageFormat("{0} renamed their nation {1} to {2}!", sender.name, oldName, newName))
	}

	@Subcommand("set color")
	@Description("Change the color of your nation")
	fun onSetColor(sender: Player, red: Int, green: Int, blue: Int) = asyncCommand(sender) {
		val nationId = requireFrontierNationIn(sender)
		requireFrontierNationLeader(sender, nationId)
		val color: Color = validateColor(red, green, blue, nationId)

		FrontierNation.setColor(nationId, color.asRGB())

		sender.sendMessage(nationMessageFormat("Updated nation color to {0}", text("█████████████", color(red, green, blue))))
	}

	@Subcommand("top|list")
	@Description("View the top nations on Horizon's End")
	fun onTop(sender: CommandSender, @Optional page: Int?): Unit = asyncCommand(sender) {
		val nations = FrontierNation.allIds()

		val nationMembers: Map<Oid<FrontierNation>, List<SLPlayerId>> =
			nations.associateWith { FrontierNation.getMembers(it).toList() }

		val sortedNations: List<Oid<FrontierNation>> = nations.toList()
			.sortedByDescending { nationMembers[it]?.size ?: 0 }

		val nameColor = NamedTextColor.GOLD
		val leaderColor = NamedTextColor.AQUA
		val membersColor = NamedTextColor.BLUE
		val settlementsColor = DARK_AQUA
		val outpostsColor = YELLOW
		val split = text(" | ", HEColorScheme.HE_MEDIUM_GRAY)

		val headerLine = (ofChildren(
			text("Name", nameColor),
			split, text("Leader", leaderColor),
			split, text("Members", membersColor),
			split, text("Settlements", settlementsColor),
			split, text("Outposts", outpostsColor),
		))

		val menu = formatPaginatedMenu(
			sortedNations.size,
			"/fn top",
			page ?: 1
		) { index ->
			val nation = sortedNations[index]
			val data: FrontierNationCache.FrontierNationData = FrontierNationCache[nation]

			val members = nationMembers[nation]!!

			val name = data.name
			val leaderName = SLPlayer.getName(data.leader)!!

			text()
				.hoverEvent(text("Click for more info"))
				.clickEvent(ClickEvent.runCommand("/fn info $name"))

				.append(text("    $name ", nameColor))
				.append(text(leaderName, leaderColor))
				.append(text(" ${members.count()} ", membersColor))
				.build()

		}

		val fullMessage = ofChildren(
			lineBreakWithCenterText(nationMessageFormat("Top Nations"), 17), newline(),
			headerLine, newline(),
			menu, newline(),
			lineBreak(47)
		)

		sender.sendMessage(fullMessage)
	}

	@Subcommand("info")
	fun onInfo(sender: CommandSender, @Optional nation: String?): Unit = asyncCommand(sender) {
		val nationId: Oid<FrontierNation> = when (sender) {
			is Player -> {
				when (nation) {
					null -> PlayerCache[sender].frontierNationOid ?: fail { "You need to specify a nation. /n info <nation>" }
					else -> resolveFrontierNation(nation)
				}
			}

			else -> resolveFrontierNation(nation ?: fail { "Non-players must specify a nation" })
		}

		val senderNationId: Oid<FrontierNation>? = when (sender) {
			is Player -> PlayerCache[sender].frontierNationOid
			else -> null
		}

		val message = text().color(TextColor.fromHexString("#b8e0d4"))

		val lineWidth = 45
		val lineBreak = lineBreak(lineWidth)

		val data = FrontierNation.findById(nationId) ?: fail { "Failed to load data" }
		val cached = FrontierNationCache[nationId]

		message.append(lineBreak)

		val leftPad = (((lineWidth * (3.0 / 2.0)) - cached.name.length) / 2) + 3 // = is 3/2 the size of a space
		message.append(text(repeatString(" ", leftPad.roundToInt()) + cached.name).color(color(cached.color)).decorate(TextDecoration.BOLD))
		message.append(newline())

		val players: List<SLPlayerId> = FrontierNation.getMembers(nationId).toList()

		val playerText = ofChildren(
			text("Settlements ("),
			text(players.size).color(NamedTextColor.WHITE),
			text("): "),
			newline()
		)

		for (player in players) {
			val playerData = SLPlayer[player]

			playerText.append(text(playerData?.lastKnownName ?: "Unknown")).color(NamedTextColor.WHITE)

			val isLast = players.indexOf(player) == (players.size - 1)
			if (!isLast) playerText.append(text(", "))
		}

		message.append(playerText)
		message.append(newline())
		message.append(ofChildren(
			text("Balance: "),
			text(data.balance).color(NamedTextColor.WHITE)
		))
	}
}
