package net.horizonsend.ion.server.command.nations

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.NationRole
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.schema.nations.Territory
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.common.utils.miscellaneous.toCreditsString
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.common.utils.text.lineBreak
import net.horizonsend.ion.common.utils.text.lineBreakWithCenterText
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.repeatString
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.chat.Discord
import net.horizonsend.ion.server.features.misc.ServerInboxes
import net.horizonsend.ion.server.features.nations.NATIONS_BALANCE
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.features.nations.utils.isActive
import net.horizonsend.ion.server.features.nations.utils.isInactive
import net.horizonsend.ion.server.features.nations.utils.isSemiActive
import net.horizonsend.ion.server.features.player.CombatTimer
import net.horizonsend.ion.server.features.progression.achievements.Achievement
import net.horizonsend.ion.server.features.progression.achievements.rewardAchievement
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import net.horizonsend.ion.server.miscellaneous.utils.actualStyle
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.distance
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA
import net.kyori.adventure.text.format.NamedTextColor.WHITE
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
import java.util.Date
import kotlin.math.roundToInt

@CommandAlias("nation|n")
internal object NationCommand : SLCommand() {
	private val nationsMessageColor = TextColor.fromHexString("#FC3200")
	private val nationsImportantMessageColor = TextColor.fromHexString("#FC9300")
	private fun nationMessageFormat(text: String, vararg args: Any?) = template(text(text, nationsMessageColor), false, *args)
	private fun nationImportantMessageFormat(text: String, vararg args: Any?) = template(text(text, nationsImportantMessageColor), false, *args)

	override fun onEnable(manager: PaperCommandManager) {
		registerAsyncCompletion(manager, "member_settlements") { c ->
			val player = c.player ?: throw InvalidCommandArgument("Players only")
			val nation = PlayerCache[player].nationOid

			SettlementCache.all().filter { nation != null && it.nation == nation }.map { it.name }
		}

		registerAsyncCompletion(manager, "nations") { _ -> NationCache.all().map { it.name } }
		registerAsyncCompletion(manager, "outposts") { c ->
			val player = c.player ?: throw InvalidCommandArgument("Players only")
			val nation = PlayerCache[player].nationOid
			Regions.getAllOf<RegionTerritory>().filter { it.nation == nation }.map { it.name }
		}
	}

	private fun validateName(name: String, nationId: Oid<Nation>?) {
		if (!"\\w*".toRegex().matches(name)) {
			throw InvalidCommandArgument("Name must be alphanumeric")
		}

		if (name.length < 3) {
			throw InvalidCommandArgument("Name cannot be less than 3 characters")
		}

		if (name.length > 40) {
			throw InvalidCommandArgument("Name cannot be more than 40 characters")
		}

		val existingNation: Oid<Nation>? = NationCache.getByName(name)
		if (existingNation != null && (nationId == null || nationId != existingNation)) {
			throw InvalidCommandArgument("A nation named $name already exists.")
		}
	}

	private fun validateColor(red: Int, green: Int, blue: Int, nationId: Oid<Nation>?): Color {
		failIf(sequenceOf(red, green, blue).any { it !in 0..255 }) { "Red, green, and blue must be integers within 0-255" }

		val color = Color.fromRGB(red, green, blue)

		val query = if (nationId == null) EMPTY_BSON else Nation::_id ne nationId

		for (results in Nation.findProps(query, Nation::name, Nation::color)) {
			val nationName = results[Nation::name]
			val nationColor = Color.fromRGB(results[Nation::color])

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
	@Description("Create a nation. Color values must be R-G-B color values, each from 0-255")
	fun onCreate(
		sender: Player,
		name: String,
		red: Int,
		green: Int,
		blue: Int,
		@Optional cost: Int?
	) = asyncCommand(sender) {
		requireEconomyEnabled()

		val settlement = requireSettlementIn(sender)
		requireSettlementLeader(sender, settlement)
		requireNotInNation(sender)
		requireMinLevel(sender, NATIONS_BALANCE.nation.minCreateLevel)
		validateName(name, null)
		val color = validateColor(red, green, blue, nationId = null)

		val realCost = NATIONS_BALANCE.nation.createCost
		requireMoney(sender, realCost, "create a nation")

		failIf(cost != realCost) {
			"You must acknowledge the cost of creating a nation to create one. " +
				"The cost is ${realCost.toCreditsString()}. Run the command: " +
				"/nation create $name $red $green $blue $realCost"
		}

		Nation.create(name, settlement, color.asRGB())
		VAULT_ECO.withdrawPlayer(sender, realCost.toDouble())

		sender.rewardAchievement(Achievement.CREATE_NATION)

		Notify.chatAndGlobal(nationImportantMessageFormat(
			"{0}, leader of the settlement {1}, founded the nation {2}!",
			sender.name,
			getSettlementName(settlement),
			name
		))

		val embed = Embed(
			title = "${sender.name}, leader of the settlement ${getSettlementName(settlement)}, founded the nation $name!",
			color = color.asRGB(),
		)

		Discord.sendEmbed(ConfigurationFiles.discordSettings().eventsChannel, embed)
	}

	@Subcommand("disband")
	@Description("Disband your nation (this cannot be undone!)")
	fun onDisband(sender: Player, @Optional name: String?) = asyncCommand(sender) {
		val nation = requireNationIn(sender)
		requireNationLeader(sender, nation)

		val nationName = getNationName(nation)
		failIf(name != nationName) { "To disband your nation, you must confirm by specifying the name. Run the command: /n disband $nationName" }

		Nation.delete(nation)

		Notify.chatAndEvents(nationImportantMessageFormat(
			"The nation {0} has been disbanded by its leader {1}",
			nationName,
			sender.name
		))
	}

	@Subcommand("invite")
	@CommandCompletion("@settlements")
	@Description("Invite a settlement to your nation")
	fun onInvite(sender: Player, settlement: String) = asyncCommand(sender) {
		val nationId = requireNationIn(sender)
		requireNationPermission(sender, nationId, NationRole.Permission.SETTLEMENT_INVITE)

		val settlementId = resolveSettlement(settlement)
		failIf(SettlementCache[settlementId].nation == nationId) { "$settlement is already in your nation" }

		val leaderId = SettlementCache[settlementId].leader.uuid

		val nationName = getNationName(nationId)

		if (!Nation.isInvited(nationId, settlementId)) {
			Nation.addInvite(nationId, settlementId)
			sender.success("Invited settlement ${getSettlementName(settlementId)} to your nation")

			Notify.playerCrossServer(
				player = leaderId,
				message = nationImportantMessageFormat(
					"Your settlement has been invited to the nation {0} by {1}! To accept, use {2}",
					nationName,
					sender.name,
					text("/nation join $nationName", YELLOW, TextDecoration.ITALIC)
						.hoverEvent(text("Click to run /nation join $nationName"))
						.clickEvent(ClickEvent.runCommand("/nation join $nationName"))
				)
			)
		} else {
			Nation.removeInvite(nationId, settlementId)
			sender.success("Cancelled invite for settlement $settlementId to your nation")

			Notify.playerCrossServer(
				player = leaderId,
				message = nationImportantMessageFormat(
					"Your settlement's invitation to join the nation {0} has been revoked by {1}",
					nationName,
					sender.name,
				)
			)
		}
	}

	@Subcommand("invites")
	fun onInvites(sender: Player, @Optional page: Int?) = asyncCommand(sender) {
		val nationId = requireNationIn(sender)
		requireNationPermission(sender, nationId, NationRole.Permission.SETTLEMENT_INVITE)

		val invitedSettlements = NationCache[nationId].invites.toList()

		val body = formatPaginatedMenu(
			entries = invitedSettlements.count(),
			command = "/nation invites",
			currentPage = page ?: 1,
		) {
			val settlementId = invitedSettlements[it]
			val settlement = getSettlementName(settlementId)

			text(settlement, YELLOW)
		}

		sender.sendMessage(ofChildren(
			lineBreakWithCenterText(text("Invites", YELLOW)), newline(),
			body
		))
	}

	@Subcommand("join")
	@CommandCompletion("@nations")
	@Description("Join a nation which you're invited to")
	fun onJoin(sender: Player, nation: String) = asyncCommand(sender) {
		val settlementId = requireSettlementIn(sender)
		requireSettlementLeader(sender, settlementId)
		requireNotInNation(sender)
		val nationId: Oid<Nation> = resolveNation(nation)

		val settlementName = getSettlementName(settlementId)
		val nationName = getNationName(nationId)

		failIf(!Nation.isInvited(nationId, settlementId)) { "$settlementName isn't invited to $nationName" }

		Nation.removeInvite(nationId, settlementId)
		Settlement.joinNation(settlementId, nationId)

		Notify.chatAndEvents(nationImportantMessageFormat("Settlement {0} joined the nation {1}", settlementName, nationName))
	}

	@Subcommand("leave")
	@Description("Leave the nation you're in")
	fun onLeave(sender: Player, @Optional nation: String?) = asyncCommand(sender) {
		val settlementId = requireSettlementIn(sender)
		requireSettlementLeader(sender, settlementId)
		val nationId = requireNationIn(sender)
		val nationName = getNationName(nationId)

		requireNotCapital(settlementId, action = "leave the nation")

		failIf(nationName != nation) { "You need to confirm using the name of the nation. Run the command: /n leave $nationName" }

		Settlement.leaveNation(settlementId)

		Notify.chatAndEvents(nationImportantMessageFormat("Settlement {0} seceded from the nation {1}!", getSettlementName(settlementId), nationName))
	}

	@Subcommand("kick")
	@Description("Kick a settlement from your nation")
	@CommandCompletion("@member_settlements")
	fun onKick(sender: Player, settlement: String) = asyncCommand(sender) {
		val nationId = requireNationIn(sender)
		requireNationPermission(sender, nationId, NationRole.Permission.SETTLEMENT_KICK)

		val settlementId = resolveSettlement(settlement)
		val settlementName = getSettlementName(settlementId)

		failIf(SettlementCache[settlementId].nation != nationId) { "Settlement $settlementName is not in your nation" }

		requireNotCapital(settlementId, action = "be kicked")

		Settlement.leaveNation(settlementId)

		Notify.chatAndEvents(nationImportantMessageFormat("{0} kicked settlement {1} from the nation {2}!", sender.name, settlementName, getNationName(nationId)))
	}

	@Subcommand("set name")
	@Description("Rename your nation")
	fun onSetName(sender: Player, newName: String, @Optional cost: Int?) = asyncCommand(sender) {
		val nationId = requireNationIn(sender)
		requireNationLeader(sender, nationId)
		validateName(newName, nationId)

		val oldName = getNationName(nationId)
		failIf(oldName == newName) { "Your nation is already named $oldName" }

		val realCost = NATIONS_BALANCE.nation.renameCost
		requireMoney(sender, realCost, "rename")

		failIf(cost != realCost) {
			"You must acknowledge the cost of renaming to rename it. " +
				"The cost is ${realCost.toCreditsString()}. Run the command: " +
				"/nation set name $newName $realCost"
		}

		Nation.setName(nationId, newName)
		VAULT_ECO.withdrawPlayer(sender, realCost.toDouble())

		Notify.chatAndEvents(nationMessageFormat("{0} renamed their nation {1} to {2}!", sender.name, oldName, newName))
	}

	@Subcommand("set color")
	@Description("Change the color your nation")
	fun onSetColor(sender: Player, red: Int, green: Int, blue: Int) = asyncCommand(sender) {
		val nationId = requireNationIn(sender)
		requireNationLeader(sender, nationId)
		val color: Color = validateColor(red, green, blue, nationId)

		Nation.setColor(nationId, color.asRGB())

		sender.sendMessage(nationMessageFormat("Updated nation color to {0}", text("█████████████", color(red, green, blue))))
	}

	@Subcommand("set capital")
	@CommandCompletion("@member_settlements")
	fun setCapital(sender: Player, newCapital: String) = asyncCommand(sender) {
		val nationId = requireNationIn(sender)
		requireNationLeader(sender, nationId)

		val settlementId = resolveSettlement(newCapital)
		val settlementName = getSettlementName(settlementId)

		failIf(settlementId == requireSettlementIn(sender)) { "Your settlement is already the capital" }

		failIf(SettlementCache[settlementId].nation != nationId) { "Settlement $settlementName is not in your nation" }

		Nation.setCapital(nationId, settlementId)

		Notify.chatAndEvents(nationImportantMessageFormat("{0} changed the capital of their nation {1} to {2}", sender.name, getNationName(nationId), settlementName))
	}

	@Subcommand("claim")
	@Description("Claim a planetary territory (one per planet)")
	fun onClaim(sender: Player, @Optional cost: Int?) = asyncCommand(sender) {
		val nationId = requireNationIn(sender)
		requireNationPermission(sender, nationId, NationRole.Permission.CLAIM_CREATE)

		failIf(CombatTimer.isNpcCombatTagged(sender) || CombatTimer.isPvpCombatTagged(sender)) { "You are currently in combat!" }

		val territory = requireTerritoryIn(sender)
		requireTerritoryUnclaimed(territory)

		failIf(Regions.getAllOf<RegionTerritory>().any { it.world == territory.world && it.nation == nationId }) { "Nations can only have one outpost per planet" }

		val realCost = territory.cost

		failIf(cost != realCost) {
			"You must acknowledge the cost of the settlement to create it. " +
				"The cost is ${realCost.toCreditsString()}. Run the command: " +
				"/nation claim $realCost"
		}

		requireMoney(sender, realCost, "claim ${territory.name}")

		VAULT_ECO.withdrawPlayer(sender, realCost.toDouble())

		Territory.setNation(territory.id, nationId)

		sender.rewardAchievement(Achievement.CREATE_OUTPOST)

		val nationName = getNationName(nationId)
		Notify.chatAndEvents(nationImportantMessageFormat(
			"{0} claimed the territory {1} on {2} for their nation {3}!",
			sender.name,
			territory.name,
			territory.world,
			nationName
		))
	}

	@Subcommand("unclaim")
	@Description("Unclaim a planetary territory")
	@CommandCompletion("@outposts")
	fun onUnclaim(sender: Player, territory: String) = asyncCommand(sender) {
		val nationId = requireNationIn(sender)
		requireNationPermission(sender, nationId, NationRole.Permission.CLAIM_CREATE)

		val regionTerritory = Regions.getAllOf<RegionTerritory>()
			.firstOrNull { it.name.equals(territory, ignoreCase = true) }
			?: fail { "Territory $territory not found" }
		val territoryName = regionTerritory.name
		val territoryWorld = regionTerritory.world

		failIf(regionTerritory.nation != nationId) { "$territoryName is not claimed by your nation" }

		Territory.setNation(regionTerritory.id, null)

		val nationName = getNationName(nationId)
		Notify.chatAndEvents(nationImportantMessageFormat(
			"{0} unclaimed the territory {1} on {2} from their nation {3}!",
			sender.name,
			territoryName,
			territoryWorld,
			nationName
		))
	}

	@Subcommand("outpost set alias")
	fun onTerritoryAlias(sender: Player, alias: String) = asyncCommand(sender) {
		validateName(alias, null)

		val nationId = requireNationIn(sender)
		requireNationPermission(sender, nationId, NationRole.Permission.CLAIM_CREATE)

		val regionTerritory = requireTerritoryIn(sender)
		failIf(regionTerritory.nation != nationId) { "Your nation doesn't own that territory!" }

		val territoryName = regionTerritory.name
		val territoryWorld = regionTerritory.world

		Territory.updateById(regionTerritory.id, setValue(Territory::alias, alias))

		val nationName = getNationName(nationId)
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
	@Description("View the top nations on Star Legacy")
	fun onTop(sender: CommandSender, @Optional page: Int?): Unit = asyncCommand(sender) {
		val nations = Nation.allIds()

		val nationMembers: Map<Oid<Nation>, List<SLPlayerId>> =
			nations.associateWith { Nation.getMembers(it).toList() }

		val lastSeenMap: Map<SLPlayerId, Date> = SLPlayer
			.findProps(SLPlayer::nation ne null, SLPlayer::_id, SLPlayer::lastSeen)
			.associate { it[SLPlayer::_id] to it[SLPlayer::lastSeen] }

		val activeMemberCounts: Map<Oid<Nation>, Int> = nationMembers.mapValues { (_, members) ->
			members.count { lastSeenMap[it]?.let(::isActive) == true }
		}

		val semiActiveMemberCounts: Map<Oid<Nation>, Int> = nationMembers.mapValues { (_, members) ->
			members.count { lastSeenMap[it]?.let(::isSemiActive) == true }
		}

		val sortedNations: List<Oid<Nation>> = nations.toList()
			.sortedByDescending { nationMembers[it]?.size ?: 0 }
			.sortedByDescending { semiActiveMemberCounts[it] ?: 0 }
			.sortedByDescending { activeMemberCounts[it] ?: 0 }

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
			"/n top",
			page ?: 1
		) { index ->
			val nation = sortedNations[index]
			val data: NationCache.NationData = NationCache[nation]

			val members = nationMembers[nation]!!

			var active = 0
			var semiActive = 0
			var inactive = 0

			for (member in members) {
				val lastSeen = lastSeenMap[member]!!
				when {
					isActive(lastSeen) -> active++
					isSemiActive(lastSeen) -> semiActive++
					isInactive(lastSeen) -> inactive++
				}
			}

			val name = data.name
			val leaderName = SLPlayer.getName(data.leader)!!

			text()
				.hoverEvent(text("Click for more info"))
				.clickEvent(ClickEvent.runCommand("/n info $name"))

				.append(text("    $name ", nameColor))
				.append(text(leaderName, leaderColor))
				.append(text(" ${members.count()} ", membersColor))
				.append(SettlementCommand.formatActive(active, semiActive, inactive))
				.append(text(" ${SettlementCache.all().count { it.nation == nation }}", settlementsColor))
				.append(text(" ${Regions.getAllOf<RegionTerritory>().count { it.nation == nation }}", outpostsColor))
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
	@CommandCompletion("@nations")
	fun onInfo(sender: CommandSender, @Optional nation: String?): Unit = asyncCommand(sender) {
		val nationId: Oid<Nation> = when (sender) {
			is Player -> {
				when (nation) {
					null -> PlayerCache[sender].nationOid ?: fail { "You need to specify a nation. /n info <nation>" }
					else -> resolveNation(nation)
				}
			}

			else -> resolveNation(nation ?: fail { "Non-players must specify a nation" })
		}

		val senderNationId: Oid<Nation>? = when (sender) {
			is Player -> PlayerCache[sender].nationOid
			else -> null
		}

		val message = text().color(TextColor.fromHexString("#b8e0d4"))

		val lineWidth = 45
		val lineBreak = lineBreak(lineWidth)

		val data = Nation.findById(nationId) ?: fail { "Failed to load data" }
		val cached = NationCache[nationId]

		message.append(lineBreak)

		val leftPad = (((lineWidth * (3.0 / 2.0)) - cached.name.length) / 2) + 3 // = is 3/2 the size of a space
		message.append(text(repeatString(" ", leftPad.roundToInt()) + cached.name).color(color(cached.color)).decorate(TextDecoration.BOLD))
		message.append(newline())

		senderNationId?.let {
			val relation = RelationCache[nationId, senderNationId]
			val otherRelation = RelationCache.getWish(nationId, senderNationId)
			val wish = RelationCache.getWish(senderNationId, nationId)

			val relationHover = text("Your Wish: ")
				.append(wish.component)
				.append(newline())
				.append(text("Their Wish: "))
				.append(otherRelation.component)
				.asHoverEvent()

			message.append(
				text("Relation: ").hoverEvent(relationHover)
					.append(relation.component)
			)
			message.append(newline())
		}

		val outposts: List<RegionTerritory> = Regions.getAllOf<RegionTerritory>().filter { it.nation == nationId }
		val outpostsText = text()
			.append(text("Outposts ("))
			.append(text(outposts.size).color(WHITE))
			.append(text("): "))

		for (outpost in outposts) {
			val outpostBuilder = text()
			val hoverText = text().color(TextColor.fromHexString("#b8e0d4"))
				.append(text(outpost.name).color(NamedTextColor.AQUA))
				.append(newline())
				.append(text("Planet: ").append(text(outpost.world).color(WHITE)))
				.append(newline())
				.append(text("Centered at ")
					.append(text(outpost.centerX).color(WHITE))
					.append(text(", "))
					.append(text(outpost.centerZ).color(WHITE))
				)
				.build()
				.asHoverEvent()

			val isLast: Boolean = outposts.indexOf(outpost) == (outposts.size - 1)

			outpostBuilder.append(
				text(outpost.name)
					.hoverEvent(hoverText)
					.color(WHITE)
			)

			if (!isLast) outpostBuilder.append(text(", "))

			outpostsText.append(outpostBuilder.build())
		}

		message.append(outpostsText)
		message.append(newline())

		val settlements: List<Oid<Settlement>> = Nation.getSettlements(nationId)
			.sortedByDescending { SLPlayer.count(SLPlayer::settlement eq it) }
			.toList()

		val settlementsText = text()
			.append(text("Settlements ("))
			.append(text(settlements.size).color(WHITE))
			.append(text("): "))

		for (settlement in settlements) {
			val cachedSettlement = SettlementCache[settlement]
			val cachedTerritory = Regions.get<RegionTerritory>(cachedSettlement.territory)
			val memberCount = SLPlayer.count(SLPlayer::settlement eq settlement).toInt()

			val hoverTextBuilder = text().color(TextColor.fromHexString("#b8e0d4"))
				.append(text(cachedSettlement.name).color(NamedTextColor.AQUA))
				.append(newline())
				.append(text("Led By: ").append(text(getPlayerName(cachedSettlement.leader)).color(NamedTextColor.AQUA)))
				.append(newline())
				.append(text(memberCount).color(WHITE)).append(text(" members"))
				.append(newline())

			if (cachedSettlement.cityState != null) hoverTextBuilder
				.append(text("Trade city status: ${cachedSettlement.cityState}"))
				.append(newline())

			hoverTextBuilder
				.append(text("Planet: ").append(text(cachedTerritory.world).color(WHITE)))
				.append(newline())
				.append(text("Centered at ")
					.append(text(cachedTerritory.centerX).color(WHITE))
					.append(text(", "))
					.append(text(cachedTerritory.centerX).color(WHITE))
				)

			val settlementBuilder = text()
				.append(
					text(cachedSettlement.name)
						.hoverEvent(hoverTextBuilder.build().asHoverEvent())
						.clickEvent(ClickEvent.runCommand("/s info ${cachedSettlement.name}"))
						.color(WHITE)
				)

			val isLast: Boolean = settlements.indexOf(settlement) == (settlements.size - 1)

			if (cachedSettlement.cityState != null) settlementBuilder.decorate(TextDecoration.BOLD)
			if (!isLast) settlementBuilder.append(text(", "))

			settlementsText.append(settlementBuilder)
		}

		message.append(settlementsText)
		message.append(newline())
		message.append(text("Balance: ").append(text(data.balance).color(WHITE)))
		message.append(newline())

		val leaderRole = NationRole.getHighestRole(cached.leader)
		val leaderRoleComp = leaderRole?.let { leader ->
			text(leader.name).color(color(
				leader.color.actualStyle.wrappedColor.color.red,
				leader.color.actualStyle.wrappedColor.color.green,
				leader.color.actualStyle.wrappedColor.color.blue
			))
		} ?: text()
		val leaderText = text("Leader: ")
			.append(leaderRoleComp)
			.append(text(" "))
			.append(text(getPlayerName(cached.leader)).color(WHITE))

		message.append(leaderText)
		message.append(newline())

		val activeStyle = NamedTextColor.GREEN
		val semiActiveStyle = NamedTextColor.GRAY
		val inactiveStyle = NamedTextColor.RED
		val members: List<Triple<SLPlayerId, String, Date>> = SLPlayer
			.findProps(SLPlayer::nation eq nationId, SLPlayer::lastKnownName, SLPlayer::lastSeen)
			.map { Triple(it[SLPlayer::_id], it[SLPlayer::lastKnownName], it[SLPlayer::lastSeen]) }
			.sortedByDescending { it.third }

		val names = mutableListOf<Component>()
		var active = 0
		var semiActive = 0
		var inactive = 0
		for ((playerId, name, lastSeen) in members) {
			val style: NamedTextColor = when {
				isActive(lastSeen) -> {
					active++
					activeStyle
				}

				isSemiActive(lastSeen) -> {
					semiActive++
					semiActiveStyle
				}

				isInactive(lastSeen) -> {
					inactive++
					inactiveStyle
				}

				else -> error("Impossible!")
			}
			names.add(text(getNationTag(playerId, name)).color(style))
		}

		val playerCountBuilder = text().color(TextColor.fromHexString("#b8e0d4"))
			.append(text("Members ("))
			.append(text(members.size).color(WHITE))
			.append(text("): ("))
			.append(text("$active Active").color(NamedTextColor.GREEN))
			.append(text(" $semiActive Semi-Active").color(NamedTextColor.GRAY))
			.append(text(" $inactive Inactive").color(NamedTextColor.RED))
			.append(text(")"))

		message.append(playerCountBuilder.build())
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
			namesList.append(text(" [Hover for full member list]").color(DARK_AQUA)).hoverEvent(fullNamesList.asComponent().asHoverEvent())
		}

		message.append(namesList)
		message.append(newline())
		message.append(lineBreak)

		sender.sendMessage(message.build())
	}

	@Subcommand("role")
	fun onRole(sender: CommandSender): Unit = fail { "Use /nrole, not /n role (remove the space)" }

	@Subcommand("broadcast")
	fun onBroadcast(sender: Player, message: String) = asyncCommand(sender) {
		val nationId = requireNationIn(sender)
		requireNationPermission(sender, nationId, NationRole.Permission.BRODCAST)
		ServerInboxes.sendToNationMembers(nationId, message.miniMessage())
	}
}
