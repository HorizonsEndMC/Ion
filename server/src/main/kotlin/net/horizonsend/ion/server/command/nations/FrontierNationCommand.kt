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
import net.horizonsend.ion.common.database.schema.economy.BankedItem
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.FrontierNation
import net.horizonsend.ion.common.database.schema.nations.FrontierNation.Companion.getTotalPower
import net.horizonsend.ion.common.database.schema.nations.FrontierNationRole
import net.horizonsend.ion.common.database.schema.nations.FrontierTerritory
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.common.utils.text.lineBreak
import net.horizonsend.ion.common.utils.text.lineBreakWithCenterText
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.repeatString
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.command.GlobalCompletions
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.command.nations.roles.FrontierNationRoleCommand
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.chat.Discord
import net.horizonsend.ion.server.features.misc.ServerInboxes
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionFrontierTerritory
import net.horizonsend.ion.server.features.player.CombatTimer
import net.horizonsend.ion.server.features.progression.PlayerXPLevelCache
import net.horizonsend.ion.server.gui.invui.misc.FrontierNationBankMenu
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.SLTextStyle
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.actualStyle
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.distance
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
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
import org.litote.kmongo.eq
import org.litote.kmongo.ne
import org.litote.kmongo.setValue
import kotlin.math.roundToInt

@CommandAlias("frontiernation|fn")
object FrontierNationCommand : SLCommand() {
	private val nationsMessageColor = TextColor.fromHexString("#FC3200")
	private val nationsImportantMessageColor = TextColor.fromHexString("#FC9300")
	private fun nationMessageFormat(text: String, vararg args: Any?) =
		template(text(text, nationsMessageColor), false, *args)

	private fun nationImportantMessageFormat(text: String, vararg args: Any?) =
		template(text(text, nationsImportantMessageColor), false, *args)

	override fun onEnable(manager: PaperCommandManager) {
		registerAsyncCompletion(manager, "frontierNations") { _ -> FrontierNationCache.all().map { it.name } }
		registerAsyncCompletion(manager, "frontierOutposts") { c ->
			val player = c.player ?: throw InvalidCommandArgument("Players only")
			val nation = PlayerCache[player].frontierNationOid
			Regions.getAllOf<RegionFrontierTerritory>().filter { it.frontierNation == nation }.map { it.name }
		}
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
		failIf(
			sequenceOf(
				red,
				green,
				blue
			).any { it !in 0..255 }) { "Red, green, and blue must be integers within 0-255" }

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
		failIf(CombatTimer.isNpcCombatTagged(sender) || CombatTimer.isPvpCombatTagged(sender)) { "You are currently in combat!" }

		requireNotInFrontierNation(sender)
		requiredNotOnNationJoinCooldown(sender)
		validateName(name, null)
		val color = validateColor(red, green, blue, nationId = null)

		val territory = requireFrontierTerritoryIn(sender)
		requireFrontierTerritoryUnclaimed(territory)

		FrontierNation.create(name, sender.slPlayerId, color.asRGB(), territory.id)

		// scuffed role creation
		Tasks.syncDelay(60L) {
			FrontierNationRoleCommand.onCreate(sender, "Leader", SLTextStyle.GOLD, 1000)
			FrontierNationRoleCommand.onPermissionAdd(sender, "Leader", FrontierNationRole.Permission.CLAIM_CREATE)
			FrontierNationRoleCommand.onPermissionAdd(sender, "Leader", FrontierNationRole.Permission.CLAIM_DELETE)
			FrontierNationRoleCommand.onPermissionAdd(sender, "Leader", FrontierNationRole.Permission.MANAGE_ROLES)
			FrontierNationRoleCommand.onPermissionAdd(sender, "Leader", FrontierNationRole.Permission.PLAYER_INVITE)
			FrontierNationRoleCommand.onPermissionAdd(sender, "Leader", FrontierNationRole.Permission.PLAYER_KICK)
			FrontierNationRoleCommand.onPermissionAdd(sender, "Leader", FrontierNationRole.Permission.START_KOTH_SIEGE)
			FrontierNationRoleCommand.onPermissionAdd(sender, "Leader", FrontierNationRole.Permission.START_STATION_SIEGE)
			FrontierNationRoleCommand.onPermissionAdd(sender, "Leader", FrontierNationRole.Permission.BROADCAST)
			FrontierNationRoleCommand.onMemberAdd(sender, sender.name, "Leader")

			FrontierNationRoleCommand.onCreate(sender, "Officer", SLTextStyle.YELLOW, 100)
			FrontierNationRoleCommand.onPermissionAdd(sender, "Officer", FrontierNationRole.Permission.CLAIM_CREATE)
			FrontierNationRoleCommand.onPermissionAdd(sender, "Officer", FrontierNationRole.Permission.MANAGE_ROLES)
			FrontierNationRoleCommand.onPermissionAdd(sender, "Officer", FrontierNationRole.Permission.PLAYER_INVITE)
			FrontierNationRoleCommand.onPermissionAdd(sender, "Officer", FrontierNationRole.Permission.PLAYER_KICK)
			FrontierNationRoleCommand.onPermissionAdd(sender, "Officer", FrontierNationRole.Permission.START_KOTH_SIEGE)
			FrontierNationRoleCommand.onPermissionAdd(sender, "Officer", FrontierNationRole.Permission.START_STATION_SIEGE)
			FrontierNationRoleCommand.onPermissionAdd(sender, "Officer", FrontierNationRole.Permission.BROADCAST)

			FrontierNationRoleCommand.onCreate(sender, "Member", SLTextStyle.GRAY, 100)
		}

		Notify.chatAndGlobal(
			nationImportantMessageFormat(
				"{0} founded the frontier nation {1} at {2}!",
				sender.name,
				name,
				territory.name
			)
		)

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

		Notify.chatAndEvents(
			nationImportantMessageFormat(
				"The nation {0} has been disbanded by its leader {1}",
				nationName,
				sender.name
			)
		)
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

		sender.sendMessage(
			ofChildren(
				lineBreakWithCenterText(text("Invites", YELLOW)), Component.newline(),
				body
			)
		)
	}

	@Subcommand("join")
	@CommandCompletion("@frontierNations")
	@Description("Join a nation which you're invited to")
	fun onJoin(sender: Player, nation: String) = asyncCommand(sender) {
		requireNotInFrontierNation(sender)
		val nationId: Oid<FrontierNation> = resolveFrontierNation(nation)

		val nationName = getFrontierNationName(nationId)

		failIf(!FrontierNation.isInvited(nationId, sender.slPlayerId)) { "You are not invited to $nationName" }
		failIf(FrontierNation.getMembers(nationId).count() > 4) {"This nation has already reached its maximum member count of 5!"}

		requiredNotOnNationJoinCooldown(sender)

		FrontierNation.removeInvite(nationId, sender.slPlayerId)
		SLPlayer.joinFrontierNation(sender.slPlayerId, nationId)

		Notify.chatAndEvents(nationImportantMessageFormat("{0} joined the nation {1}", sender.name, nationName))
	}

	@Subcommand("leave")
	@Description("Leave the nation you're in")
	fun onLeave(sender: Player, @Optional nation: String?) = asyncCommand(sender) {
		val nationId = requireFrontierNationIn(sender)
		val nationName = getFrontierNationName(nationId)

		requiredNotOnNationJoinCooldown(sender)

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

		failIf(SLPlayer[playerId] != nationId) { "$otherPlayer is not in your nation" }

		SLPlayer.leaveFrontierNation(playerId.slPlayerId)

		Notify.chatAndEvents(
			nationImportantMessageFormat(
				"{0} kicked {1} from the nation {2}!",
				sender.name,
				otherPlayer,
				getFrontierNationName(nationId)
			)
		)
	}

	@Subcommand("set name")
	@Description("Rename your nation")
	fun onSetName(sender: Player, newName: String) = asyncCommand(sender) {
		val nationId = requireFrontierNationIn(sender)
		requireFrontierNationLeader(sender, nationId)
		validateName(newName, nationId)

		val oldName = getFrontierNationName(nationId)
		failIf(oldName == newName) { "Your nation is already named $oldName" }

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

		sender.sendMessage(
			nationMessageFormat(
				"Updated nation color to {0}",
				text("█████████████", color(red, green, blue))
			)
		)
	}

	@Subcommand("outpost claim")
	@Description("Claim a territory on a planet or a space sector")
	fun onClaim(sender: Player) = asyncCommand(sender) {
		val nationId = requireFrontierNationIn(sender)
		requireFrontierNationPermission(sender, nationId, FrontierNationRole.Permission.CLAIM_CREATE)

		failIf(CombatTimer.isNpcCombatTagged(sender) || CombatTimer.isPvpCombatTagged(sender)) { "You are currently in combat!" }

		val territory = requireFrontierTerritoryIn(sender)
		requireFrontierTerritoryUnclaimed(territory)

		val power = getTotalPower(nationId)

		failIf(power < 40) { "Your nation doesn't have enough power to claim an outpost!" }
		failIf(Regions.getAllOf<RegionFrontierTerritory>().any { it.frontierNation == nationId && !it.isCapital }) { "Nations can only have one outpost (besides the capital)" }


		FrontierTerritory.setFrontierNation(territory.id, nationId)

		val nationName = getFrontierNationName(nationId)
		Notify.chatAndEvents(nationImportantMessageFormat(
			"{0} claimed the territory {1} on {2} for their nation {3}!",
			sender.name,
			territory.name,
			territory.world,
			nationName
		))
	}

	@Subcommand("outpost unclaim")
	@Description("Unclaim a territory on a planet or a space sector")
	@CommandCompletion("@frontierOutposts")
	fun onUnclaim(sender: Player, territory: String) = asyncCommand(sender) {
		val nationId = requireFrontierNationIn(sender)
		requireFrontierNationPermission(sender, nationId, FrontierNationRole.Permission.CLAIM_DELETE)

		val regionTerritory = Regions.getAllOf<RegionFrontierTerritory>()
			.firstOrNull { it.name.replace("\n", "").equals(territory.replace("\n", ""), ignoreCase = true)}
			?: fail { "Territory $territory not found" }
		val territoryName = regionTerritory.name
		val territoryWorld = regionTerritory.world

		failIf(regionTerritory.frontierNation != nationId) { "$territoryName is not claimed by your nation" }
		failIf(regionTerritory.isCapital) { "$territoryName is your nation's capital and cannot be unclaimed (use /fn disband)" }

		FrontierTerritory.setFrontierNation(regionTerritory.id, null)

		val nationName = getFrontierNationName(nationId)
		Notify.chatAndEvents(nationImportantMessageFormat(
			"{0} unclaimed the territory {1} on {2} from their nation {3}!",
			sender.name,
			territoryName,
			territoryWorld,
			nationName
		))
	}

	@Subcommand("outpost alias")
	fun onTerritoryAlias(sender: Player, alias: String) = asyncCommand(sender) {
		validateName(alias, null)

		val nationId = requireFrontierNationIn(sender)
		requireFrontierNationPermission(sender, nationId, FrontierNationRole.Permission.CLAIM_CREATE)

		val regionTerritory = requireFrontierTerritoryIn(sender)
		failIf(regionTerritory.frontierNation != nationId) { "Your nation doesn't own that territory!" }

		val territoryName = regionTerritory.name
		val territoryWorld = regionTerritory.world

		FrontierTerritory.updateById(regionTerritory.id, setValue(FrontierTerritory::alias, alias))

		val nationName = getFrontierNationName(nationId)
		Notify.chatAndGlobal(nationImportantMessageFormat(
			"{0}, of {1} renamed their territory outpost {2} on {3} to {4}!",
			sender.name,
			nationName,
			territoryName,
			territoryWorld,
			alias
		))
	}

	@Subcommand("top|list")
	@Description("View the top nations on Horizon's End")
	fun onTop(sender: CommandSender, @Optional page: Int?): Unit = asyncCommand(sender) {
		val nations = FrontierNation.allIds()

		val nationMembers: Map<Oid<FrontierNation>, List<SLPlayerId>> =
			nations.associateWith { FrontierNation.getMembers(it).toList() }

		/*val sortedNations: List<Oid<FrontierNation>> = nations.toList()
			.sortedByDescending { nationMembers[it]?.size ?: 0 } */


		val nationPoints: Map<Oid<FrontierNation>, Int> =
			nations.associateWith { FrontierNation.findById(it)?.points ?: 0 }

		val sortedNations: List<Oid<FrontierNation>> = nations.toList()
			.sortedByDescending { nationPoints[it] }

		val nameColor = NamedTextColor.GOLD
		val leaderColor = NamedTextColor.AQUA
		val membersColor = NamedTextColor.BLUE
		val powerColor = DARK_AQUA
		val pointsColor = NamedTextColor.GREEN
		val split = text(" | ", HEColorScheme.HE_MEDIUM_GRAY)

		val headerLine = (ofChildren(
			text("Name", nameColor),
			split, text("Leader", leaderColor),
			split, text("Members", membersColor),
			split, text("Power", powerColor),
			split, text("Points", pointsColor),
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
				.append(text("${getTotalPower(nation)} / ${members.count()*20} ", powerColor))
				.append(text("${data.points}", pointsColor))
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
	@CommandCompletion("@frontierNations")
	fun onInfo(sender: CommandSender, @Optional nation: String?): Unit = asyncCommand(sender) {
		val nationId: Oid<FrontierNation> = when (sender) {
			is Player -> {
				when (nation) {
					null -> PlayerCache[sender].frontierNationOid
						?: fail { "You need to specify a nation. /fn info <nation>" }

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
		message.append(
			text(repeatString(" ", leftPad.roundToInt()) + cached.name).color(color(cached.color))
				.decorate(TextDecoration.BOLD)
		)
		message.append(newline())

		val capitalResult = Regions.getAllOf<RegionFrontierTerritory>().filter { it.frontierNation == nationId && it.isCapital }
		val capital = if (capitalResult.isNotEmpty()) capitalResult.first().name else "None"
		val capitalText = ofChildren(
			text("Capital: "),
			text(capital, NamedTextColor.WHITE),
			newline()
		)
		message.append(capitalText)

		val outposts = Regions.getAllOf<RegionFrontierTerritory>().filter { it.frontierNation == nationId }
		val outpostsText = ofChildren(
			text("Outposts ("),
			text(outposts.size, NamedTextColor.WHITE),
			text("): ")
		)

		message.append(outpostsText)
		for (outpost in outposts) {
			val outpostBuilder = text()
			val hoverText = text().color(TextColor.fromHexString("#b8e0d4"))
				.append(text(outpost.name, NamedTextColor.AQUA))
				.append(newline())
				.append(text("World: ", TextColor.fromHexString("#b8e0d4")))
				.append(text(outpost.world, NamedTextColor.WHITE))
				.append(newline())
				.append(text("Centered at ", TextColor.fromHexString("#b8e0d4"))
					.append(text(outpost.centerX, NamedTextColor.WHITE))
					.append(text(",", TextColor.fromHexString("#b8e0d4")))
					.append(text(outpost.centerZ, NamedTextColor.WHITE))
				).build().asHoverEvent()

			val isLast = outposts.indexOf(outpost) == (outposts.size - 1)

			outpostBuilder.append(
				text(outpost.name)
					.hoverEvent(hoverText)
					.color(NamedTextColor.WHITE)
			)

			if (!isLast) outpostBuilder.append(text(", "))
			val builtText = outpostBuilder.build()

			message.append(builtText)
		}
		message.append(newline())

		message.append(
			ofChildren(
				text("Balance: "),
				text(data.balance).color(NamedTextColor.WHITE),
				newline()
			)
		)

		message.append(
			ofChildren(
				text("Points: "),
				text(data.points).color(NamedTextColor.GREEN),
				newline()
			)
		)

		message.append(
			ofChildren(
				text("Total Power across all players: "),
				text(getTotalPower(nationId), NamedTextColor.WHITE),
				newline()
			)
		)

		val leaderRole = FrontierNationRole.getHighestRole(cached.leader)
		val leaderRoleComp = leaderRole?.let {
			val leaderText = text(leaderRole.name, leaderRole.color.actualStyle.textColor)

			leaderText
		} ?: text()
		val leaderText = ofChildren(
			text("Leader: "),
			leaderRoleComp,
			Component.space(),
			text(getPlayerName(cached.leader), NamedTextColor.WHITE)
		)

		message.append(leaderText)
		message.append(newline())

		val members = SLPlayer.findProps(SLPlayer::frontierNation eq nationId, SLPlayer::lastKnownName)
			.map { Pair(it[SLPlayer::_id], it[SLPlayer::lastKnownName]) }
			.sortedBy { it.second }

		val names = mutableListOf<Component>()
		for ((playerId, name) in members) {
			names.add(text(getFrontierNationTag(playerId, name), NamedTextColor.GRAY))
		}

		val playerCountBuilder = ofChildren(
			text("Members (", TextColor.fromHexString("#b8e0d4")),
			text(members.size, NamedTextColor.WHITE),
			text("):", TextColor.fromHexString("#b8e0d4"))
		)

		message.append(playerCountBuilder)
		message.append(newline())

		val limit = 10

		val namesList = text()
		val fullNamesList = text()

		for (name in names) {
			val isLast = names.indexOf(name) == (names.size - 1)
			val nameBuilder = text()
				.append(name)

			if (!isLast) nameBuilder.append(text(", ").color(TextColor.fromHexString("#b8e0d4")).asComponent())

			fullNamesList.append(nameBuilder)

			if (names.indexOf(name) >= (limit)) continue

			namesList.append(nameBuilder)
		}

		if (names.size > limit) {
			namesList.append(text("...").color(TextColor.fromHexString("#b8e0d4")))
			namesList.append(text(" [Hover for full member list]").color(DARK_AQUA))
				.hoverEvent(fullNamesList.asComponent().asHoverEvent())
		}

		message.append(namesList)
		message.append(newline())
		message.append(lineBreak)

		sender.sendMessage(message.build())
	}

	@Subcommand("role")
	fun onRole(sender: CommandSender): Unit = fail { "Use /fnrole, not /fn role (remove the space)" }

	@Subcommand("broadcast")
	fun onBroadcast(sender: Player, message: String) = asyncCommand(sender) {
		val nationId = requireFrontierNationIn(sender)
		requireFrontierNationPermission(sender, nationId, FrontierNationRole.Permission.BROADCAST)
		ServerInboxes.sendToFrontierNationMembers(nationId, message.miniMessage())
	}

	@Subcommand("points")
	@CommandCompletion("@frontierNations")
	fun getPoints(sender: CommandSender, @Optional nation: String?): Unit = asyncCommand(sender) {
		val nationId: Oid<FrontierNation> = when (sender) {
			is Player -> {
				when (nation) {
					null -> PlayerCache[sender].frontierNationOid
						?: fail { "You need to specify a nation. /fn info <nation>" }

					else -> resolveFrontierNation(nation)
				}
			}

			else -> resolveFrontierNation(nation ?: fail { "Non-players must specify a nation" })
		}
		val data = FrontierNation.findById(nationId) ?: fail { "Failed to load data" }

		sender.sendMessage(
			ofChildren(
				text("${data.name}'s Points: "),
				text(data.points).color(NamedTextColor.GREEN),
			)
		)
	}


	@Subcommand("power")
	@CommandCompletion("@players")
	fun onGetPower(sender: Player, otherPlayer: String) = asyncCommand(sender) {
		val playerId = resolveOfflinePlayer(otherPlayer)
		val power = PlayerXPLevelCache[playerId]?.power ?: fail { "Player $playerId does not exist, or somehow does not have power" }
		sender.information("$otherPlayer has $power power")
	}

	@Subcommand("bank menu")
	@Description("Displays items stored in your nation's bank")
	fun onBankMenu(sender: Player) = asyncCommand(sender) {
		val nationId = requireFrontierNationIn(sender)
		val territory = requireFrontierTerritoryIn(sender)
		if (territory.frontierNation != nationId) {
			sender.userError("You are not in a territory owned by your nation")
			return@asyncCommand
		}

		FrontierNationBankMenu(sender, nationId).openGui()
	}

	@Subcommand("bank deposit")
	@Description("Deposits the item in your hand into your nation's bank")
	fun onBankDeposit(sender: Player) = asyncCommand(sender) {
		val nationId = requireFrontierNationIn(sender)
		val territory = requireFrontierTerritoryIn(sender)
		if (territory.frontierNation != nationId) {
			sender.userError("You are not in a territory owned by your nation")
			return@asyncCommand
		}

		Tasks.sync {
			val item = sender.inventory.itemInMainHand
			if (item.isEmpty) {
				sender.userError("You are not holding an item to deposit")
				return@sync
			}

			val string = GlobalCompletions.toItemString(item)
			val amount = item.amount
			sender.inventory.setItemInMainHand(null)

			Tasks.async {
				BankedItem.create(nationId, string, amount)
			}

			sender.success("Successfully deposited $string x$amount in your nation bank")
		}
	}

	@Subcommand("bank list")
	@Description("Lists the items in your nation's bank")
	fun onBankList(sender: Player, @Optional page: Int?) = asyncCommand(sender) {
		val nationId = requireFrontierNationIn(sender)
		val items = BankedItem.find(BankedItem::frontierNation eq nationId).toList()

		if (items.isEmpty()) return@asyncCommand sender.userError("Your nation does not have any items deposited")

		val builder = text()

		builder.append(text("Your Nation's Items (${items.size})"), newline())
		builder.append(lineBreak(45), newline())

		val body = formatPaginatedMenu(
			items.size,
			"/frontiernation bank list",
			page ?: 1
		) { index ->
			val item = items[index]
			val itemDisplayName = fromItemString(item.itemString).displayNameComponent

			ofChildren(itemDisplayName)
		}

		builder.append(body, newline())
		builder.append(lineBreak(45), newline())

		sender.sendMessage(builder.build())
	}

	@Subcommand("set leader")
	@Description("Change the leader of your frontier nation")
	fun onSetLeader(sender: Player, player: String): Unit = asyncCommand(sender) {
		val frontierNationId = requireFrontierNationIn(sender)
		requireFrontierNationLeader(sender, frontierNationId)

		val playerId = resolveOfflinePlayer(player)
		val slPlayerId = playerId.slPlayerId
		val nationName = getFrontierNationName(frontierNationId)

		requireIsMemberOfFrontierNation(slPlayerId, frontierNationId)

		FrontierNation.setLeader(frontierNationId, slPlayerId)

		Notify.chatAndGlobal(text("${sender.name} changed their frontier nation $nationName's leader to $player"))
	}
}
