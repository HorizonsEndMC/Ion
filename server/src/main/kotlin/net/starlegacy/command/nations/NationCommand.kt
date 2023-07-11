package net.starlegacy.command.nations

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.features.achievements.Achievement
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.features.achievements.rewardAchievement
import net.horizonsend.ion.server.miscellaneous.repeatString
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextColor.color
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.md_5.bungee.api.chat.TextComponent
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.starlegacy.command.SLCommand
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.NationRelation
import net.horizonsend.ion.common.database.schema.nations.NationRole
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.schema.nations.Territory
import net.horizonsend.ion.common.database.uuid
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
import net.starlegacy.util.darkGray
import net.starlegacy.util.darkGreen
import net.starlegacy.util.distance
import net.starlegacy.util.fromLegacy
import net.starlegacy.util.gray
import net.starlegacy.util.msg
import net.starlegacy.util.style
import net.starlegacy.util.toCreditsString
import net.starlegacy.util.white
import org.bukkit.Color
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.litote.kmongo.EMPTY_BSON
import org.litote.kmongo.eq
import org.litote.kmongo.ne
import java.util.Date
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@CommandAlias("nation|n")
internal object NationCommand : SLCommand() {
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

		Notify all MiniMessage.miniMessage().deserialize("<yellow>${sender.name}, leader of the settlement ${getSettlementName(settlement)}, founded the nation $name!")
	}

	@Suppress("unused")
	@Subcommand("disband")
	@Description("Disband your nation (this cannot be undone!)")
	fun onDisband(sender: Player, @Optional name: String?) = asyncCommand(sender) {
		val nation = requireNationIn(sender)
		requireNationLeader(sender, nation)

		val nationName = getNationName(nation)
		failIf(name != nationName) { "To disband your nation, you must confirm by specifying the name. Run the command: /n disband $nationName" }

		Nation.delete(nation)

		Notify all MiniMessage.miniMessage().deserialize("<yellow>The nation $nationName has been disbanded by its leader ${sender.name}!")
	}

	@Suppress("unused")
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
			Notify.player(
				player = leaderId,
				message = MiniMessage.miniMessage().deserialize("<aqua>Your settlement is invited to the nation $nationName by ${sender.name}! " +
						"To accept, use <yellow><italic>/nation join $nationName")
			)
		} else {
			Nation.removeInvite(nationId, settlementId)
			sender.success("Cancelled invite for settlement $settlementId to your nation")
			Notify.player(
				player = leaderId,
				message = MiniMessage.miniMessage().deserialize("<yellow>Your settlement's invite to the nation $nationName has been revoked by ${sender.name}")
			)
		}
	}

	@Suppress("unused")
	@Subcommand("invites")
	fun onInvites(sender: Player) = asyncCommand(sender) {
		val nationId = requireNationIn(sender)
		requireNationPermission(sender, nationId, NationRole.Permission.SETTLEMENT_INVITE)

		val invitedSettlements = Nation.findPropById(nationId, Nation::invites)
		sender msg "&7Invited Settlements:&b ${invitedSettlements?.joinToString { SettlementCache[it].name }}"
	}

	@Suppress("unused")
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

		Notify all MiniMessage.miniMessage().deserialize("<light_purple>Settlement <aqua>$settlementName<light_purple> joined the nation <red>$nationName<light_purple>!")
	}

	@Suppress("unused")
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

		Notify all MiniMessage.miniMessage().deserialize("<yellow>Settlement <dark_green>${getSettlementName(settlementId)}<yellow> seceded from the nation <red>$nationName<yellow>!")
	}

	@Suppress("unused")
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

		Notify all MiniMessage.miniMessage().deserialize("<gold>${sender.name}<yellow> kicked settlement $settlementName from the nation ${getNationName(nationId)}")
	}

	@Suppress("unused")
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

		Notify.online(MiniMessage.miniMessage().deserialize("<gold>${sender.name}<light_purple> renamed their nation <red>$oldName<light_purple> to <>$newName<light_purple>!"))
	}

	@Suppress("unused")
	@Subcommand("set color")
	@Description("Change the color your nation")
	fun onSetColor(sender: Player, red: Int, green: Int, blue: Int) = asyncCommand(sender) {
		val nationId = requireNationIn(sender)
		requireNationLeader(sender, nationId)
		val color: Color = validateColor(red, green, blue, nationId)

		Nation.setColor(nationId, color.asRGB())

		sender.information("Updated nation color.")
	}

	@Suppress("unused")
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

		Notify all MiniMessage.miniMessage().deserialize("<gold>${sender.name}<light_purple> changed the capital of their nation ${getNationName(nationId)} to $settlementName!")
	}

	@Suppress("unused")
	@Subcommand("claim")
	@Description("Claim a planetary territory (one per planet)")
	fun onClaim(sender: Player, @Optional cost: Int?) = asyncCommand(sender) {
		val nationId = requireNationIn(sender)
		requireNationPermission(sender, nationId, NationRole.Permission.CLAIM_CREATE)

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
		Notify.online(MiniMessage.miniMessage().deserialize("<gold>${sender.name}<light_purple> claimed the territory <dark_green>${territory.name}<light_purple> for their nation <red>$nationName<light_purple>!"))
	}

	@Suppress("unused")
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

		failIf(regionTerritory.nation != nationId) { "$territoryName is not claimed by your nation" }

		Territory.setNation(regionTerritory.id, null)

		val nationName = getNationName(nationId)
		Notify.online(MiniMessage.miniMessage().deserialize("<gold>${sender.name}<light_purple> unclaimed the territory <dark_green>$territoryName<light_purple> from their nation <red>$nationName<light_purple>!"))
	}

	@Suppress("unused")
	@Subcommand("top|list")
	@Description("View the top nations on Star Legacy")
	fun onTop(sender: CommandSender, @Optional page: Int?): Unit = asyncCommand(sender) {
		val lines = mutableListOf<TextComponent>()
		lines += lineBreak().fromLegacy()

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

		val pages = max(1, sortedNations.size / super.linesPerPage)
		val index = (max(min(page ?: 1, pages), 1)) - 1
		val nationsOnPage = sortedNations.subList(
			fromIndex = index * linesPerPage,
			toIndex = min(index * linesPerPage + linesPerPage, sortedNations.size)
		)

		val nameColor = SLTextStyle.GOLD
		val leaderColor = SLTextStyle.AQUA
		val membersColor = SLTextStyle.BLUE
		val activeColor = SLTextStyle.GREEN
		val semiActiveColor = SLTextStyle.GRAY
		val inactiveColor = SLTextStyle.RED
		val settlementsColor = SLTextStyle.DARK_AQUA
		val outpostsColor = SLTextStyle.YELLOW
		val split = "&8|"

		lines += (
			"${nameColor}Name " +
				"$split ${leaderColor}Leader " +
				"$split ${membersColor}Members " +
				"$split ${settlementsColor}Settlements " +
				"$split ${outpostsColor}Outposts"
			).fromLegacy()

		for (nation in nationsOnPage) {
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

			val line = TextComponent()

			val name = data.name
			val leaderName = SLPlayer.getName(data.leader)!!

			line.addExtra("    $name ".style(nameColor).cmd("/n info $name").hover("Click for more info"))
			line.addExtra(leaderName.style(leaderColor))
			line.addExtra(" ${members.count()}".style(membersColor))
			line.addExtra(darkGray(" ["))
			line.addExtra("$active ".style(activeColor))
			line.addExtra("$semiActive ".style(semiActiveColor))
			line.addExtra("$inactive ".style(inactiveColor))
			line.addExtra(darkGray("]"))
			line.addExtra(" ${SettlementCache.all().count { it.nation == nation }}".style(settlementsColor))
			line.addExtra(" ${Regions.getAllOf<RegionTerritory>().count { it.nation == nation }}".style(outpostsColor))

			lines += line
		}

		val pageLine = TextComponent()

		if (index > 0) {
			pageLine.addExtra(darkGreen(" ["))
			pageLine.addExtra(white("<--").cmd("/nation top $index").hover("Click to see previous page"))
			pageLine.addExtra(darkGreen("]"))
		}

		pageLine.addExtra(darkAqua(" Page "))
		pageLine.addExtra(gray("${index + 1}/$pages "))

		if (index < pages - 1) {
			pageLine.addExtra(darkGreen(" ["))
			pageLine.addExtra(white("-->").cmd("/nation top ${index + 2}").hover("Click to see next page"))
			pageLine.addExtra(darkGreen("]"))
		}

		lines += pageLine

		lines += lineBreak().fromLegacy()

		lines.forEach(sender::msg)
	}

	@Suppress("unused")
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
		val lineBreak = text(repeatString("=", lineWidth)).decorate(TextDecoration.STRIKETHROUGH).color(NamedTextColor.DARK_GRAY)

		val data = Nation.findById(nationId) ?: fail { "Failed to load data" }
		val cached = NationCache[nationId]

		message.append(lineBreak)

		val leftPad = (((lineWidth * (3.0 / 2.0)) - cached.name.length) / 2) + 3 // = is 3/2 the size of a space
		message.append(text(repeatString(" ", leftPad.roundToInt()) + cached.name).color(color(cached.color)).decorate(TextDecoration.BOLD))
		message.append(newline())

		senderNationId?.let {
			val relation = NationRelation.getRelationActual(nationId, senderNationId)
			val otherRelation = NationRelation.getRelationWish(nationId, senderNationId)
			val wish = NationRelation.getRelationWish(senderNationId, nationId)

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
			.append(text(outposts.size).color(NamedTextColor.WHITE))
			.append(text("): "))

		for (outpost in outposts) {
			val outpostBuilder = text()
			val hoverText = text().color(TextColor.fromHexString("#b8e0d4"))
				.append(text(outpost.name).color(NamedTextColor.AQUA))
				.append(newline())
				.append(text("Planet: ").append(text(outpost.world).color(NamedTextColor.WHITE)))
				.append(newline())
				.append(text("Centered at ")
					.append(text(outpost.centerX).color(NamedTextColor.WHITE))
					.append(text(", "))
					.append(text(outpost.centerZ).color(NamedTextColor.WHITE))
				)
				.build()
				.asHoverEvent()

			val isLast: Boolean = outposts.indexOf(outpost) == (outposts.size - 1)

			outpostBuilder.append(
				text(outpost.name)
					.hoverEvent(hoverText)
					.color(NamedTextColor.WHITE)
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
			.append(text(settlements.size).color(NamedTextColor.WHITE))
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
				.append(text(memberCount).color(NamedTextColor.WHITE)).append(text(" members"))
				.append(newline())

			if (cachedSettlement.cityState != null) hoverTextBuilder
				.append(text("Trade city status: ${cachedSettlement.cityState}"))
				.append(newline())

			hoverTextBuilder
				.append(text("Planet: ").append(text(cachedTerritory.world).color(NamedTextColor.WHITE)))
				.append(newline())
				.append(text("Centered at ")
					.append(text(cachedTerritory.centerX).color(NamedTextColor.WHITE))
					.append(text(", "))
					.append(text(cachedTerritory.centerX).color(NamedTextColor.WHITE))
				)

			val settlementBuilder = text()
				.append(
					text(cachedSettlement.name)
						.hoverEvent(hoverTextBuilder.build().asHoverEvent())
						.clickEvent(ClickEvent.runCommand("/s info ${cachedSettlement.name}"))
						.color(NamedTextColor.WHITE)
				)

			val isLast: Boolean = settlements.indexOf(settlement) == (settlements.size - 1)

			if (cachedSettlement.cityState != null) settlementBuilder.decorate(TextDecoration.BOLD)
			if (!isLast) settlementBuilder.append(text(", "))

			settlementsText.append(settlementBuilder)
		}

		message.append(settlementsText)
		message.append(newline())
		message.append(text("Balance: ").append(text(data.balance).color(NamedTextColor.WHITE)))
		message.append(newline())

		val leaderRole = NationRole.getHighestRole(cached.leader)
		val leaderRoleComp = leaderRole?.let { leader ->
			text(leader.name).color(
				color(
					SLTextStyle.valueOf(leader.color).wrappedColor.color.red,
					SLTextStyle.valueOf(leader.color).wrappedColor.color.green,
					SLTextStyle.valueOf(leader.color).wrappedColor.color.blue
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

	@Suppress("unused")
	@Subcommand("role")
	fun onRole(sender: CommandSender): Unit = fail { "Use /nrole, not /n role (remove the space)" }
}
