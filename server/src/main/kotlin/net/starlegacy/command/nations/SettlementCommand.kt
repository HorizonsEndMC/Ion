package net.starlegacy.command.nations

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import java.util.Date
import java.util.UUID
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import net.horizonsend.ion.server.database.schema.nations.Nation
import net.horizonsend.ion.server.features.achievements.Achievement
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.features.achievements.rewardAchievement
import net.horizonsend.ion.server.miscellaneous.repeatString
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.md_5.bungee.api.chat.TextComponent
import net.starlegacy.cache.nations.NationCache
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.cache.nations.RelationCache
import net.starlegacy.cache.nations.SettlementCache
import net.starlegacy.command.SLCommand
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.schema.misc.SLPlayer
import net.horizonsend.ion.server.database.schema.misc.SLPlayerId
import net.horizonsend.ion.server.database.schema.nations.Settlement
import net.horizonsend.ion.server.database.schema.nations.SettlementRole
import net.horizonsend.ion.server.database.schema.nations.Territory
import net.horizonsend.ion.server.database.slPlayerId
import net.starlegacy.feature.economy.city.TradeCities
import net.starlegacy.feature.nations.NATIONS_BALANCE
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.feature.nations.region.types.RegionTerritory
import net.starlegacy.feature.nations.utils.cmd
import net.starlegacy.feature.nations.utils.hover
import net.starlegacy.feature.nations.utils.isActive
import net.starlegacy.feature.nations.utils.isInactive
import net.starlegacy.feature.nations.utils.isSemiActive
import net.starlegacy.util.Notify
import net.starlegacy.util.SLTextStyle
import net.starlegacy.util.VAULT_ECO
import net.starlegacy.util.darkAqua
import net.starlegacy.util.darkGreen
import net.starlegacy.util.depositMoney
import net.starlegacy.util.fromLegacy
import net.starlegacy.util.gray
import net.starlegacy.util.msg
import net.starlegacy.util.style
import net.starlegacy.util.toCreditsString
import net.starlegacy.util.white
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.litote.kmongo.eq
import org.litote.kmongo.ne
import org.litote.kmongo.updateOneById

@Suppress("unused")
@CommandAlias("settlement|s")
internal object SettlementCommand : SLCommand() {
	private fun validateName(name: String, settlementId: Oid<Settlement>?) {
		if (!"\\w*".toRegex().matches(name)) {
			throw InvalidCommandArgument("Name must be alphanumeric")
		}

		if (name.length < 3) {
			throw InvalidCommandArgument("Name cannot be less than 3 characters")
		}

		if (name.length > 40) {
			throw InvalidCommandArgument("Name cannot be more than 40 characters")
		}

		val existingSettlement: Oid<Settlement>? = SettlementCache.getByName(name)
		if (existingSettlement != null && (settlementId == null || settlementId != existingSettlement)) {
			throw InvalidCommandArgument("A settlement named $name already exists.")
		}
	}

	@Subcommand("create")
	@Description("Create your own settlement in the territory you're in (More expensive for bigger territories)")
	fun onCreate(sender: Player, name: String, @Optional cost: Int?): Unit = asyncCommand(sender) {
		requireNotInSettlement(sender)

// 		requireMinLevel(sender, NATIONS_BALANCE.settlement.minCreateLevel)

		validateName(name, null)

		val territory = requireTerritoryIn(sender)
		requireTerritoryUnclaimed(territory)

		val realCost = territory.cost
		requireMoney(sender, realCost, "create a settlement in ${territory.name}")

		failIf(cost != realCost) {
			"You must acknowledge the cost of the settlement to create it. " +
				"The cost is ${realCost.toCreditsString()}. Run the command: " +
				"/settlement create $name $realCost"
		}

		Settlement.create(territory.id, name, sender.slPlayerId)
		VAULT_ECO.withdrawPlayer(sender, realCost.toDouble())

		sender.rewardAchievement(Achievement.CREATE_SETTLEMENT)

		Notify all MiniMessage.miniMessage().deserialize("<green>${sender.name} has founded the settlement $name in ${territory.name} on ${territory.world}!")

		// No manual territory cache update is needed as settlement creation should automatically trigger that
	}

	@Subcommand("disband|delete")
	@Description("Disband your settlement, permanently deleting it")
	fun onDisband(sender: Player, @Optional name: String?): Unit = asyncCommand(sender) {
		val settlement = requireSettlementIn(sender)
		requireSettlementLeader(sender, settlement)
		requireNotCapital(settlement)

		val settlementName = getSettlementName(settlement)

		failIf(settlementName != name) { "You must verify the name of your settlement to disband it. To disband your settlement, use /s disband $settlementName" }

		Settlement.delete(settlement)

		Notify all MiniMessage.miniMessage().deserialize("<yellow>${sender.name} has disbanded their settlement $settlementName!")

		// No manual territory cache update is needed as settlement removal from territory should automatically trigger that
		// Additionally, all members of the settlement should be updated as their player cache will be updated,
		// which triggers a territory access cache update for them as well.
	}

	@Subcommand("invite")
	@CommandCompletion("@players")
	@Description("Invite a player to your settlement so they can join")
	fun onInvite(sender: Player, player: String): Unit = asyncCommand(sender) {
		val playerId: UUID = resolveOfflinePlayer(player)
		val slPlayerId = playerId.slPlayerId

		val settlementId: Oid<Settlement> = requireSettlementIn(sender)

		requireSettlementPermission(sender, settlementId, SettlementRole.Permission.INVITE)

		failIf(SLPlayer.matches(slPlayerId, SLPlayer::settlement eq settlementId)) { "$player is already in your settlement!" }

		val settlementName = getSettlementName(settlementId)

		if (Settlement.isInvitedTo(settlementId, slPlayerId)) {
			Settlement.removeInvite(settlementId, slPlayerId)
			sender msg "&bRemoved $player's invite to your settlement."
			Notify.player(playerId, MiniMessage.miniMessage().deserialize("<yellow>You were un-invited from $settlementName by ${sender.name}"))
		} else {
			Settlement.addInvite(settlementId, slPlayerId)
			sender msg "&bInvited $player to your settlement."
			Notify.player(
				playerId,
				MiniMessage.miniMessage().deserialize("<aqua>You were invited to $settlementName by ${sender.name}. " +
						"To join, use <italic>/s join $settlementName")
			)
		}
	}

	@Subcommand("invites")
	fun onInvites(sender: Player) = asyncCommand(sender) {
		val settlementId = requireSettlementIn(sender)
		requireSettlementPermission(sender, settlementId, SettlementRole.Permission.INVITE)

		val invitedPlayers = Settlement.findPropById(settlementId, Settlement::invites)
		sender msg "&7Invited Settlements:&b ${invitedPlayers?.joinToString { getPlayerName(it) }}"
	}

	@Subcommand("join")
	@CommandCompletion("@settlements")
	@Description("Join the settlement, requires an invite")
	fun onJoin(sender: Player, settlement: String): Unit = asyncCommand(sender) {
		requireNotInSettlement(sender)

		val settlementId: Oid<Settlement> = resolveSettlement(settlement)
		val settlementName = getSettlementName(settlementId)

		failIf(!Settlement.isInvitedTo(settlementId, sender.slPlayerId)) { "You're not invited the settlement $settlementName!" }

		SLPlayer.joinSettlement(sender.slPlayerId, settlementId)

		Notify.online(MiniMessage.miniMessage().deserialize("<green>${sender.name} joined the settlement $settlementName!"))

		// No manual territory cache updating is needed, as the player is added to the settlement/nation, thus
		// automatically triggering the player cache update, which triggers the territory cache update
	}

	@Subcommand("leave|quit")
	@Description("Leave the settlement you're in")
	fun onLeave(sender: Player): Unit = asyncCommand(sender) {
		val settlementId = requireSettlementIn(sender)
		val settlementName = getSettlementName(settlementId)

		requireNotSettlementLeader(sender, settlementId)

		SLPlayer.leaveSettlement(sender.slPlayerId)

		Notify.online(MiniMessage.miniMessage().deserialize("<yellow>${sender.name} left the settlement $settlementName!"))

		// No manual territory cache updating is needed, as the player is removed from the settlement/nation, thus
		// automatically triggering the player cache update, which triggers the territory cache update
	}

	@Subcommand("kick")
	@CommandCompletion("@players")
	@Description("Kick a player from your settlement, forcing them to leave")
	fun onKick(sender: Player, player: String): Unit = asyncCommand(sender) {
		val settlementId = requireSettlementIn(sender)
		requireSettlementPermission(sender, settlementId, SettlementRole.Permission.KICK)
		val settlementName = getSettlementName(settlementId)

		val playerId: UUID = resolveOfflinePlayer(player)
		val slPlayerId = playerId.slPlayerId

		requireIsMemberOf(slPlayerId, settlementId)

		fun getWeight(slPlayerId: SLPlayerId) = when {
			SLPlayer.isSettlementLeader(slPlayerId) -> 1001
			else -> SettlementRole.getHighestRole(slPlayerId)?.weight ?: -1
		}

		failIf(getWeight(slPlayerId) >= getWeight(sender.slPlayerId)) { "$player has a weight greater than or equal to yours, so you can't kick them" }

		SLPlayer.leaveSettlement(slPlayerId)

		Notify.online(MiniMessage.miniMessage().deserialize("<yellow>${sender.name} kicked $player from settlement $settlementName!"))
	}

	@Subcommand("set name")
	@Description("Rename your settlement")
	fun onSetName(sender: Player, newName: String, @Optional cost: Int?): Unit = asyncCommand(sender) {
		val settlementId = requireSettlementIn(sender)
		requireSettlementLeader(sender, settlementId)

		validateName(newName, settlementId)

		val realCost = NATIONS_BALANCE.settlement.renameCost
		requireMoney(sender, realCost, "rename")

		failIf(cost != realCost) {
			"You must acknowledge the cost of renaming to rename it. " +
				"The cost is ${realCost.toCreditsString()}. Run the command: " +
				"/settlement set name $newName $realCost"
		}

		val oldName = getSettlementName(settlementId)

		Settlement.setName(settlementId, newName)
		VAULT_ECO.withdrawPlayer(sender, realCost.toDouble())

		Notify.online(MiniMessage.miniMessage().deserialize("<aqua>${sender.name} renamed their settlement $oldName to $newName!"))
	}

	@Subcommand("set leader")
	@CommandCompletion("@players")
	@Description("Change the leader of your settlement")
	fun onSetLeader(sender: Player, player: String): Unit = asyncCommand(sender) {
		val settlementId = requireSettlementIn(sender)
		requireSettlementLeader(sender, settlementId)

		val playerId: UUID = resolveOfflinePlayer(player)
		val slPlayerId = playerId.slPlayerId

		requireIsMemberOf(slPlayerId, settlementId)

		Settlement.setLeader(settlementId, slPlayerId)

		Notify.settlement(settlementId, "${sender.name} changed your settlement's leader to $player")

		// leader update automatically triggers entire settlement access cache update in CacheHelper
	}

	@Subcommand("set minbuildaccess")
	@CommandCompletion("NONE|ALLY|NATION_MEMBER|SETTLEMENT_MEMBER|STRICT")
	@Description("Change your settlement's minimum build access level")
	fun onSetMinBuildAccess(sender: Player, accessLevel: Settlement.ForeignRelation): Unit = asyncCommand(sender) {
		val settlementId = requireSettlementIn(sender)
		requireSettlementLeader(sender, settlementId)

		Settlement.setMinBuildAccess(settlementId, accessLevel)

		Notify.settlement(settlementId, "${sender.name} changed your settlement's min build access to $accessLevel")
		val description = when (accessLevel) {
			Settlement.ForeignRelation.NONE -> "Anyone, even nationless and settlementless people (should probably NEVER select this)"
			Settlement.ForeignRelation.ALLY -> "Anyone who is a nation ally, nation member, or settlement member"
			Settlement.ForeignRelation.NATION_MEMBER -> "Anyone who is a nation member"
			Settlement.ForeignRelation.SETTLEMENT_MEMBER -> "Anyone who is a settlement member"
			Settlement.ForeignRelation.STRICT -> "No default permission, only people with explicit access from e.g. a role"
		}
		sender msg "&aChanged min build access to $accessLevel. Description: $description"
	}

	@Subcommand("set tax")
	@CommandCompletion("0|1|5|10|15")
	@Description("Set your settlement's trade tax. For cities only")
	fun onSetTax(sender: Player, newTax: Int) = asyncCommand(sender) {
		val settlement: Oid<Settlement> = requireSettlementIn(sender)
		requireSettlementLeader(sender, settlement)
		failIf(newTax < 0) { "Tax must be at least 0%" }
		val max = NATIONS_BALANCE.settlement.maxTaxPercent
		failIf(newTax > max) { "Tax can't be above $max%" }
		failIf(!TradeCities.isCity(getSettlementTerritory(settlement))) { "Your settlement is not a trade city" }

		Settlement.col.updateOneById(
			settlement.id,
			org.litote.kmongo.setValue(Settlement::tradeTax, newTax.toDouble() / 100.0)
		)
		sender.success("Set tax to $newTax")
	}

	@Subcommand("top|list")
	@Description("View the top settlements on Star Legacy")
	fun onTop(sender: CommandSender, @Optional page: Int?): Unit = asyncCommand(sender) {
		val lines = mutableListOf<TextComponent>()
		lines += lineBreak().fromLegacy()

		val settlements = SettlementCache.allIds()

		val settlementMembers: Map<Oid<Settlement>, List<SLPlayerId>> =
			settlements.associateWith { Settlement.getMembers(it).toList() }

		val lastSeenMap: Map<SLPlayerId, Date> = SLPlayer
			.findProps(SLPlayer::settlement ne null, SLPlayer::_id, SLPlayer::lastSeen)
			.associate { it[SLPlayer::_id] to it[SLPlayer::lastSeen] }

		val activeMemberCounts: Map<Oid<Settlement>, Int> = settlementMembers.mapValues { (_, members) ->
			members.count { lastSeenMap[it]?.let(::isActive) == true }
		}

		val semiActiveMemberCounts: Map<Oid<Settlement>, Int> = settlementMembers.mapValues { (_, members) ->
			members.count { lastSeenMap[it]?.let(::isSemiActive) == true }
		}

		val sortedSettlements: List<Oid<Settlement>> = settlements.toList()
			.sortedByDescending { settlementMembers[it]?.size ?: 0 }
			.sortedByDescending { semiActiveMemberCounts[it] ?: 0 }
			.sortedByDescending { activeMemberCounts[it] ?: 0 }

		val pages = max(1, sortedSettlements.size / super.linesPerPage)
		val index = (max(min(page ?: 1, pages), 1)) - 1
		val settlementsOnPage = sortedSettlements.subList(
			fromIndex = index * linesPerPage,
			toIndex = min(index * linesPerPage + linesPerPage, sortedSettlements.size)
		)

		val nameColor = SLTextStyle.GOLD
		val leaderColor = SLTextStyle.AQUA
		val membersColor = SLTextStyle.BLUE
		val activeColor = SLTextStyle.GREEN
		val semiActiveColor = SLTextStyle.GRAY
		val inactiveColor = SLTextStyle.RED
		val nationColor = SLTextStyle.YELLOW
		val split = "&8|"

		lines += (
			"${nameColor}Name " +
				"$split ${leaderColor}Leader " +
				"$split ${membersColor}Members &2(${activeColor}Active ${semiActiveColor}Semi-Active ${inactiveColor}Inactive&2) " +
				"$split ${nationColor}Nation"
			).fromLegacy()

		for (settlement in settlementsOnPage) {
			val data: SettlementCache.SettlementData = SettlementCache[settlement]

			val members = settlementMembers[settlement]!!

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

			val line = TextComponent()

			val name = data.name
			val leaderName = SLPlayer.getName(data.leader)!!

			line.addExtra("    $name ".style(nameColor).cmd("/s info $name").hover("Click for more info"))
			line.addExtra(leaderName.style(leaderColor))
			line.addExtra(" ${members.count()}".style(membersColor))
			line.addExtra(" [".style(SLTextStyle.DARK_GRAY))
			line.addExtra("$active ".style(activeColor))
			line.addExtra("$semiActive ".style(semiActiveColor))
			line.addExtra("$inactive ".style(inactiveColor))
			line.addExtra("]".style(SLTextStyle.DARK_GRAY))

			data.nation?.let { nation: Oid<Nation> ->
				line.addExtra(" ${getNationName(nation)}".style(nationColor))
			}

			lines += line
		}

		val pageLine = TextComponent()

		if (index > 0) {
			pageLine.addExtra(darkGreen(" ["))
			pageLine.addExtra(white("<--").cmd("/settlement top $index").hover("Click to see previous page"))
			pageLine.addExtra(darkGreen("]"))
		}

		pageLine.addExtra(darkAqua(" Page "))
		pageLine.addExtra(gray("${index + 1}/$pages "))

		if (index < pages - 1) {
			pageLine.addExtra(darkGreen(" ["))
			pageLine.addExtra(white("-->").cmd("/settlement top ${index + 2}").hover("Click to see next page"))
			pageLine.addExtra(darkGreen("]"))
		}

		lines += pageLine

		lines += lineBreak().fromLegacy()

		lines.forEach(sender::msg)
	}

	@Subcommand("info")
	@CommandCompletion("@settlements")
	fun onInfo(sender: CommandSender, @Optional settlement: String?): Unit = asyncCommand(sender) {
		val settlementId: Oid<Settlement> = when (sender) {
			is Player -> {
				when (settlement) {
					null -> PlayerCache[sender].settlementOid
						?: SettlementCommand.fail { "You need to specify a settlement. /s info <settlement>" }

					else -> resolveSettlement(settlement)
				}
			}

			else -> resolveSettlement(settlement ?: fail { "Non-players must specify a settlement" })
		}

		val senderNationId: Oid<Nation>? = when (sender) {
			is Player -> PlayerCache[sender].nationOid
			else -> null
		}

		val data = Settlement.findById(settlementId) ?: fail { "Failed to load data" }

		val message = text().color(TextColor.fromHexString("#b8e0d4"))

		val lineWidth = 45
		val lineBreak = text(repeatString("=", lineWidth)).decorate(TextDecoration.STRIKETHROUGH).color(NamedTextColor.DARK_GRAY)

		val cached = SettlementCache[settlementId]

		message.append(lineBreak)

		val leftPad = (((lineWidth * (3.0 / 2.0)) - cached.name.length) / 2) + 3 // = is 3/2 the size of a space
		message.append(
			text(repeatString(" ", leftPad.roundToInt()) + cached.name)
				.color(NamedTextColor.AQUA)
				.decorate(TextDecoration.BOLD)
		)
		message.append(newline())

		data.nation?.let { nationId ->
			val settlementNationCached = NationCache[nationId]

			val nationsText = text().color(TextColor.fromHexString("#b8e0d4"))
				.append(text("Nation: ").color(TextColor.fromHexString("#b8e0d4")))
				.append(text(settlementNationCached.name).color(TextColor.color(settlementNationCached.color)))
				.hoverEvent(text("Click here for more info about ${settlementNationCached.name}"))
				.clickEvent(ClickEvent.runCommand("/n info ${settlementNationCached.name}"))

			senderNationId?.let {
				val relation = RelationCache[nationId, senderNationId]
				val otherRelation = RelationCache.getWish(nationId, senderNationId)
				val wish = RelationCache.getWish(senderNationId, nationId)

				val relationHover = text("Your Wish: ")
					.append(MiniMessage.miniMessage().deserialize(wish.coloredName))
					.append(newline())
					.append(text("Their Wish: "))
					.append(MiniMessage.miniMessage().deserialize(otherRelation.coloredName))
					.asHoverEvent()

				val relationText = text()
					.color(NamedTextColor.DARK_GRAY)
					.hoverEvent(relationHover)
					.append(text(" ("))
					.append(text("Relation: ").color(TextColor.fromHexString("#b8e0d4")))
					.append(MiniMessage.miniMessage().deserialize(relation.coloredName))
					.append(text(")"))

				nationsText.append(relationText)
			}

			message.append(nationsText)
			message.append(newline())
		}

		val cachedTerritory = Regions.get<RegionTerritory>(data.territory)

		val territoryHover = text().color(TextColor.fromHexString("#b8e0d4"))
			.append(text(cachedTerritory.name).color(NamedTextColor.AQUA))
			.append(newline())
			.append(text("Planet: ").append(text(cachedTerritory.world).color(NamedTextColor.WHITE)))
			.append(newline())
			.append(text("Centered at ")
				.append(text(cachedTerritory.centerX).color(NamedTextColor.WHITE))
				.append(text(", "))
				.append(text(cachedTerritory.centerZ).color(NamedTextColor.WHITE))
			)
			.build()
			.asHoverEvent()

		message.append(
			text().hoverEvent(territoryHover)
				.append(text("Territory: "))
				.append(text(cachedTerritory.name).color(NamedTextColor.WHITE))
		)
		message.append(newline())

		message.append(text("Balance: ").append(text(data.balance).color(NamedTextColor.WHITE)))
		message.append(newline())
		if (data.cityState != null && data.tradeTax != null) {
			message.append(text("Trade Tax: ").append(text("${(data.tradeTax!! * 100).toInt()}%").color(NamedTextColor.WHITE)))
			message.append(newline())
		}

		val leaderRole = SettlementRole.getHighestRole(cached.leader)
		val leaderRoleComp = leaderRole?.let { leader ->
			text(leader.name).color(
				TextColor.color(
					leader.color.wrappedColor.color.red,
					leader.color.wrappedColor.color.green,
					leader.color.wrappedColor.color.blue
				)
			)
		} ?: text()
		val leaderText = text("Leader: ")
			.append(leaderRoleComp)
			.append(text(" "))
			.append(text(getPlayerName(cached.leader)).color(NamedTextColor.WHITE))

		message.append(leaderText)
		message.append(newline())

		val activeStyle = NamedTextColor.GREEN
		val semiActiveStyle = NamedTextColor.GRAY
		val inactiveStyle = NamedTextColor.RED
		val members: List<Triple<SLPlayerId, String, Date>> = SLPlayer
			.findProps(SLPlayer::settlement eq settlementId, SLPlayer::lastKnownName, SLPlayer::lastSeen)
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
			names.add(text(getSettlementTag(playerId, name)).color(style))
		}

		val playerCountBuilder = text().color(TextColor.fromHexString("#b8e0d4"))
			.append(text("Members ("))
			.append(text(members.size).color(NamedTextColor.WHITE))
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
			namesList.append(text(" [Hover for full member list]").color(NamedTextColor.DARK_AQUA)).hoverEvent(fullNamesList.asComponent().asHoverEvent())
		}

		message.append(namesList)
		message.append(newline())
		message.append(lineBreak)

		sender.sendMessage(message.build())
	}

	@Subcommand("zone|region")
	fun onZone(sender: CommandSender): Unit = fail { "Use /szone, not /s zone (remove the space)" }

	@Subcommand("plot")
	fun onPlot(sender: CommandSender): Unit = fail { "Use /splot, not /s plot (remove the space)" }

	@Subcommand("role")
	fun onRole(sender: CommandSender): Unit = fail { "Use /srole, not /s role (remove the space)" }

	@Subcommand("refund")
	fun onRefund(sender: Player) {
		val settlementId = requireSettlementIn(sender)
		requireSettlementLeader(sender, settlementId)

		val settlement = Settlement.findById(settlementId) ?: return

		val territory = RegionTerritory(Territory.findById(settlement.territory) ?: return)

		if (settlement.needsRefund) {
			Settlement.setNeedsRefund(settlementId)
			sender.depositMoney(territory.oldCost - territory.cost)
		}
	}
}
