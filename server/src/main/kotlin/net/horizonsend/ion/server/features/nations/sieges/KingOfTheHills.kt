package net.horizonsend.ion.server.features.nations.sieges

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.FrontierNationCache
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.schema.economy.BankedItem
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.nations.FrontierNation
import net.horizonsend.ion.common.database.schema.nations.FrontierNation.Companion.getMembers
import net.horizonsend.ion.common.database.schema.nations.KothStation
import net.horizonsend.ion.common.database.schema.nations.KothSiege
import net.horizonsend.ion.common.database.schema.nations.KothType
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.extensions.informationAction
import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.common.utils.miscellaneous.randomInt
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_ORANGE
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_BLUE
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_ORANGE
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.formatFrontierNationName
import net.horizonsend.ion.common.utils.text.lineBreak
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.GlobalCompletions
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ATAVUM
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ASSEMBLY_CORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.SCORDITE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.VANADIUM
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ZIRCON
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.chat.Discord
import net.horizonsend.ion.server.features.misc.CachedKothStation
import net.horizonsend.ion.server.features.nations.NATIONS_BALANCE
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionKothZone
import net.horizonsend.ion.server.features.nations.sieges.KingOfTheHillRewards.KothRewards
import net.horizonsend.ion.server.features.nations.sieges.KingOfTheHillRewards.RewardType
import net.horizonsend.ion.server.features.player.CombatTimer
import net.horizonsend.ion.server.features.progression.achievements.Achievement
import net.horizonsend.ion.server.features.progression.achievements.rewardAchievement
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.starship.PilotedStarships.isPiloting
import net.horizonsend.ion.server.features.starship.event.StarshipSunkEvent
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.Notify
//import net.horizonsend.ion.server.miscellaneous.utils.ServerStage.getServerStage
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.WeightedRandomList
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextColor.color
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import java.lang.System.currentTimeMillis
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object KingOfTheHills : IonServerComponent() {
	data class Koths(
		val kothId: Oid<KothStation>,
		val start: Long,
		var kothPoints: MutableMap<Oid<FrontierNation>, Int>,
		var nation: Oid<FrontierNation>?,
		val type: KothType,
		val timeLimit: Long
	)

	data class RewardItem(val name: String, val quantity: Int)

	private val activeKoths = mutableListOf<Koths>()
	private val activatingKoths = mutableMapOf<RegionKothZone, Long>()
	private fun currentHour() = ZonedDateTime.now().hour
	private val moonKillRewards = WeightedRandomList<ItemStack>().apply {
		addEntry(SCORDITE.getValue().constructItemStack(), 45)
		addEntry(VANADIUM.getValue().constructItemStack(), 35)
		addEntry(ZIRCON.getValue().constructItemStack(), 15)
		addEntry(ATAVUM.getValue().constructItemStack(), 5)
	}


	override fun onEnable() {
		Tasks.syncRepeat(20, 20) {
			updateKOTHs()
			updateQuarter()
			beginKoth()
		}
		Tasks.syncRepeat(0L, 20L * 60L * 2L) { // Every 2 minutes
			displayKothLeaderboard()
		}
	}

	private fun updateKOTHs() {
		for (koth: Koths in getKOTHS()) {
			val kothId = koth.kothId
			val kothRegion: RegionKothZone = Regions[kothId]
			val elapsed = currentTimeMillis() - koth.start
			val timeLimit = TimeUnit.MINUTES.toMillis(koth.timeLimit)
			val world: World = Bukkit.getWorld(kothRegion.world) ?: return

			for (player in world.players) {
				if (!kothRegion.contains(player.location)) continue
				val playerNation = PlayerCache[player].frontierNationOid ?: continue
				if (koth.type != KothType.MOON && !isPiloting(player)) continue
				if (!koth.kothPoints.containsKey(playerNation)) {
					koth.kothPoints[playerNation] = 0
				} else {
					koth.kothPoints.merge(playerNation, 2, Int::plus)
				}
			}
			when {
				elapsed > timeLimit -> endKoth(koth)
				else -> {
					for (player in world.players) {
						if (!kothRegion.contains(player.location)) continue
						val elapsedSecondsDecimal = TimeUnit.MILLISECONDS.toSeconds(timeLimit - elapsed) / 60.0
						player.informationAction(
							"${
								String.format(
									"%.2f",
									elapsedSecondsDecimal
								)
							} minutes remaining"
						)
						CombatTimer.refreshPvpTimer(player, CombatTimer.REASON_IN_KOTH)
					}
				}
			}
		}
	}

	fun displayKothLeaderboard() {
		for (koth: Koths in getKOTHS()) {
			val kothRegion: RegionKothZone = Regions[koth.kothId]
			val world: World = Bukkit.getWorld(kothRegion.world) ?: return
			val scores = koth.kothPoints
			val orderedScores = scores.entries
				.sortedByDescending { it.value }
				.associate { it.key to it.value }
			val messageBuilder = text()
				.content("Scores for KOTH ${kothRegion.name}:")
				.color(TextColor.fromHexString("#FFD700"))
				.decorate(TextDecoration.BOLD)
			messageBuilder.append(newline())
			messageBuilder.append(lineBreak(45))
			messageBuilder.append(newline())

			for ((index, entry) in orderedScores.entries.withIndex()) {
				val nation = FrontierNation.findById(entry.key) ?: continue
				val position = index + 1
				messageBuilder.append(
					text("$position. ${nation.name} with ${entry.value} points.").color(color(nation.color))
				)
				messageBuilder.append(newline())
			}
			messageBuilder.append(lineBreak(45))
			for (player in (world.players)) {
				if (!kothRegion.contains(player.location)) continue
				player.sendMessage(messageBuilder.build())
			}
		}
	}

	@EventHandler
	fun onStarshipSink(event: StarshipSunkEvent) {
		val controller = event.previousController as? PlayerController ?: return
		val damagers = event.starship.damagers
			.filter { it.key is PlayerDamager }
			.filter { SLPlayer[(it.key as PlayerDamager).player.name]?.frontierNation != SLPlayer[controller.player.name]?.frontierNation }

		for (koth: Koths in getKOTHS()) {
			val thisKoth = koth.kothId
			val kothRegion: RegionKothZone = Regions[thisKoth]
			val world: World = Bukkit.getWorld(kothRegion.world) ?: return
			if (kothRegion.contains(event.starship.centerOfMass.toLocation(world))) {
				val pointsGained = when {
					event.starship.initialBlockCount <= 4000 -> 500
					event.starship.initialBlockCount in 4001..12000 -> 1000
					event.starship.initialBlockCount >= 12001 -> 2000

					else -> return log.error("Pilot is flying something hitherto unknown to mankind.")
				}
				val damagePointsSum = damagers.values.sumOf { it.points.get() }
				for (damager in damagers) {
					val percent = damager.value.points.get().toDouble() / damagePointsSum.toDouble()
					val player = damager.key as PlayerDamager
					processKothKill(controller.player, player.player, (pointsGained * percent).toInt(), koth.kothId, "sinking")
				}
				break
			}
		}
	}

	@EventHandler
	fun onPlayerDeath(event: PlayerDeathEvent) {
		val killer = event.player.killer ?: return
		for (koth: Koths in getKOTHS()) {
			val thisKoth = koth.kothId
			val kothRegion: RegionKothZone = Regions[thisKoth]
			val world: World = Bukkit.getWorld(kothRegion.world) ?: return
			if (kothRegion.contains(event.player.location)) {
				processKothKill(event.player, killer, 50, koth.kothId, "killing")
				break
			}
		}
	}


	private fun processKothKill(player: Player, killer: Player, points: Int, kothId: Oid<KothStation>, verb: String) {
		val victimNation = PlayerCache[player].frontierNationOid
		val killerNation = PlayerCache[killer].frontierNationOid
		val koth = activeKoths.find { it.kothId == kothId } ?: return

		killerNation?.let { killerNation ->
			koth.kothPoints[killerNation] = (koth.kothPoints[killerNation] ?: 0) + points

			if (koth.type == KothType.MOON) {
				val item = moonKillRewards.random()
				BankedItem.create(killerNation,GlobalCompletions.toItemString(item) , 1)
			}
		}
		log.info("Awarded $killerNation $points points for killing ${player.name}")
		if (killerNation != null && victimNation != null) {
			IonServer.server.sendMessage(
				template(
					"[KOTH] ${killer.name} accrued $points points for $verb ${player.name}.",
					color = HE_DARK_ORANGE,
					paramColor = HEColorScheme.HE_LIGHT_ORANGE,
					useQuotesAroundObjects = false,
					formatFrontierNationName(killerNation),
					points,
					player.name
				)
			)
		}
	}


	private var lastQuarter = -1
	private var lastStations = listOf<String>()

	private fun updateQuarter() {
		val newQuarter = currentHour()
		if (lastQuarter == newQuarter) {
			return
		}
		lastQuarter = newQuarter
		//val stage = getServerStage()
		//if (stage < 2) return
		val koths = Regions.getAllOf<RegionKothZone>()
			.filter { station -> station.siegeHour == lastQuarter }

		for (koth in koths) {
			activateKoth(koth)
		}
		lastStations = koths.map { it.name }

		//Start a koth if sufficient players are on and none are starting
		if (activatingKoths.isEmpty() && activeKoths.isEmpty() && Bukkit.getOnlinePlayers().size > 19) {
			val playerCount = Bukkit.getOnlinePlayers().size
			val koths = Regions.getAllOf<RegionKothZone>()
			if (playerCount > 34) {
				val majorKoth = koths.filter { it.type == KothType.MAJOR }.randomOrNull()
				if (majorKoth == null) return
				activateKoth(majorKoth)
			}
			else {
				val minorKoth = koths.filter { it.type == KothType.MINOR }.randomOrNull()
				if (minorKoth == null) return
				activateKoth(minorKoth)
			}

			val moonKoth = koths.filter { it.type == KothType.MOON }.randomOrNull()
			if (moonKoth == null) return
			activateKoth(moonKoth)
		}
	}


	fun getCurrentKoth() = Regions.getAllOf<RegionKothZone>()
		.filter { koth -> activatingKoths.keys.contains(koth) }

	override fun onDisable() {
		activeKoths.forEach(this::endKoth)
	}

	@Synchronized
	private fun locked(block: () -> Unit) = block()

	private fun asyncLocked(block: () -> Unit) = Tasks.async {
		locked(block)
	}

	private fun endKoth(koths: Koths) = asyncLocked {
		val topThree = determineWinner(koths)
		for (player in Bukkit.getOnlinePlayers()) {
			player.world.playSound(player, "horizonsend:server.koth.end", 12f, 0.5f)
		}

		val kothName = KothStation.findPropById(koths.kothId, KothStation::name) ?: "??NULL??"

		val message = ofChildren(
			text("The King of the Hill $kothName has ended!", HE_MEDIUM_GRAY),
			newline(),
			if (topThree[0] != null) {text("First place: ${topThree[0]}").color(TextColor.fromHexString("#D4AF37"))} else Component.empty(),
			newline(), //Gold
			if (topThree[1] != null) {text("Second place: ${topThree[1]}").color(TextColor.fromHexString("#C0C0C0"))} else Component.empty(),
			newline(), //Silver
			if (topThree[2] != null) {text("Third place: ${topThree[2]}").color(TextColor.fromHexString("#CD7F32"))} else Component.empty(),
			newline() //Bronze)
		)
		Notify.chatAndGlobal(message)
		Discord.sendMessage(ConfigurationFiles.discordSettings().eventsChannel, message)
		giveRewards(topThree, koths)
		activeKoths.removeIf { it.kothId == koths.kothId }
	}

	private fun determineWinner(koths: Koths): List<String?> {
		val rawScores = koths.kothPoints

		if (rawScores.isEmpty()) {
			Notify.chatAndGlobal(MiniMessage.miniMessage().deserialize("<gold>The King of the Hill has ended, nobody participated!"))
			Discord.sendMessage(ConfigurationFiles.discordSettings().eventsChannel, "King of the Hill event has ended, nobody participated!")
			return listOf(null, null, null)
		}
		val leaderboard = rawScores.entries
			.sortedByDescending { it.value }
			.associate { it.key to it.value }
		val numNationsParticipated = leaderboard.size
		//replace with minOf?
		var firstPlace: String? = null
		var secondPlace: String? = null
		var thirdPlace: String? = null
		if (numNationsParticipated > 0) {
			firstPlace = FrontierNationCache[leaderboard.entries.elementAt(0).key].name
		}
		if (numNationsParticipated > 1) {
			secondPlace = FrontierNationCache[leaderboard.entries.elementAt(1).key].name
		}
		if (numNationsParticipated > 2) {
			thirdPlace = FrontierNationCache[leaderboard.entries.elementAt(2).key].name
		}

		val topThree = listOf(firstPlace, secondPlace, thirdPlace)
		return topThree
	}

	//Handles randomness where the max and min are the same because i am lazy
	fun safeRandomInt(min: Int, max: Int): Int {
		if (min >= max) return min
		return randomInt(min, max)
	}

	private fun giveRewards(topThree: List<String?>, koth: Koths) = asyncLocked {
		val kothType = koth.type
		//val stage = getServerStage()

		//if (stage !in 2..4) return@asyncLocked log.error("Server age is wrong somehow!")

		val pointsToGive = when (kothType) {
			KothType.MAJOR -> listOf(75.0, 50.0, 25.0)
			KothType.MINOR -> listOf(35.0, 20.0, 10.0)
			KothType.MOON -> listOf(35.0, 20.0, 10.0)
		}
		val pointsMultiplier = 1.0

		val rewards = KothRewards.first { it.kothType == kothType && it.stage == 4 }
		val ores = rewards.rewards.filter { it.rewardType == RewardType.MATERIALS }
		val cores = rewards.rewards.filter { it.rewardType == RewardType.CORES }
		val kothBlocks = rewards.rewards.filter { it.rewardType == RewardType.KOTHBLOCK }
		val buffs = rewards.rewards.filter { it.rewardType == RewardType.BUFFS }
		val pvps = rewards.rewards.filter { it.rewardType == RewardType.PVP }

		val oreRewards = WeightedRandomList<ItemStack>().apply {
			for (ore in ores) addEntry(ore.item, ore.chance)
		}
		val coreRewards = WeightedRandomList<ItemStack>().apply {
			for (core in cores) addEntry(core.item, core.chance)
		}
		val buffRewards = WeightedRandomList<ItemStack>().apply {
			for (buff in buffs) addEntry(buff.item, buff.chance)
		}
		val pvpRewards = if (pvps.isNotEmpty()) {
			WeightedRandomList<ItemStack>().apply {
				for (pvp in pvps) addEntry(pvp.item, pvp.chance)
			}
		} else null

		fun getOreQuantity(item: ItemStack): Int {
			val quantity = ores.find { it.item == item }?.amount ?: 1..1
			return safeRandomInt(quantity.first, quantity.last)
		}
		fun getCoreQuantity(item: ItemStack): Int {
			val quantity = cores.find { it.item == item }?.amount ?: 1..1
			return safeRandomInt(quantity.first, quantity.last)
		}
		fun getBuffQuantity(item: ItemStack): Int {
			val quantity = buffs.find { it.item == item }?.amount ?: 1..1
			return safeRandomInt(quantity.first, quantity.last)
		}
		fun getPvpQuantity(item: ItemStack): Int {
			val quantity = pvps.find { it.item == item}?.amount ?: 1..1
			return safeRandomInt(quantity.first, quantity.last)
		}


		val kothBlock = ASSEMBLY_CORE.getValue().constructItemStack()
		val kothBlockItemString = GlobalCompletions.toItemString(kothBlock)
		val kothBlockQuantity = kothBlocks.firstOrNull()?.amount ?: (1..1)

		fun createRewardBreakdown(
			points: Int,
			oreItems: List<RewardItem>,
			coreItems: List<RewardItem>,
			kothBlockCount: Int,
			buffItems: List<RewardItem>,
			pvpItems: List<RewardItem>
		): Component {
			val breakdown = mutableListOf<Component>()

			breakdown.add(
				template(
					message = text("Points: {0}\n", HE_LIGHT_ORANGE),
					paramColor = HE_LIGHT_GRAY,
					useQuotesAroundObjects = false,
					points
				)
			)

			if (oreItems.isNotEmpty()) {
				breakdown.add(text("Ores:\n", HE_LIGHT_BLUE))
				oreItems.forEach { item ->
					breakdown.add(
						template(
							message = text("  - {0} x{1}\n", HE_MEDIUM_GRAY),
							paramColor = HE_LIGHT_GRAY,
							useQuotesAroundObjects = false,
							item.name,
							item.quantity
						)
					)
				}
			}

			if (coreItems.isNotEmpty()) {
				breakdown.add(text("Cores:\n", HE_LIGHT_BLUE))
				coreItems.forEach { item ->
					breakdown.add(
						template(
							message = text("  - {0} x{1}\n", HE_MEDIUM_GRAY),
							paramColor = HE_LIGHT_GRAY,
							useQuotesAroundObjects = false,
							item.name,
							item.quantity
						)
					)
				}
			}

			if (kothBlockCount > 0) {
				breakdown.add(
					template(
						message = text("KOTH Blocks: {0}\n", HE_LIGHT_BLUE),
						paramColor = HE_LIGHT_GRAY,
						useQuotesAroundObjects = false,
						kothBlockCount
					)
				)
			}

			if (buffItems.isNotEmpty()) {
				breakdown.add(text("Buffs:\n", HE_LIGHT_BLUE))
				buffItems.forEach { item ->
					breakdown.add(
						template(
							message = text("  - {0} x{1}\n", HE_MEDIUM_GRAY),
							paramColor = HE_LIGHT_GRAY,
							useQuotesAroundObjects = false,
							item.name,
							item.quantity
						)
					)
				}
			}

			if (pvpItems.isNotEmpty()) {
				breakdown.add(text("PVP Items:\n", HE_LIGHT_BLUE))
				pvpItems.forEach { item ->
					breakdown.add(
						template(
							message = text("  - {0} x{1}", HE_MEDIUM_GRAY),
							paramColor = HE_LIGHT_GRAY,
							useQuotesAroundObjects = false,
							item.name,
							item.quantity
						)
					)
				}
			}

			return ofChildren(*breakdown.toTypedArray())
		}

		// FIRST PLACE
		val firstPlaceName = topThree[0] ?: return@asyncLocked
		val firstPlaceNation = FrontierNationCache.getByName(firstPlaceName) ?: return@asyncLocked

		val firstPlacePoints = (pointsToGive[0] * pointsMultiplier).toInt()
		FrontierNation.updatePoints(firstPlaceNation, firstPlacePoints)

		val firstOres = mutableListOf<RewardItem>()
		if (ores.isNotEmpty()) {
			repeat(3) {
				val item = oreRewards.random()
				val quantity = getOreQuantity(item)
				firstOres.add(RewardItem(item.type.name, quantity))
				BankedItem.create(firstPlaceNation, GlobalCompletions.toItemString(item), quantity)
			}
		}

		val firstCores = mutableListOf<RewardItem>()
		if (cores.isNotEmpty()) {
			repeat(6) {
				val item = coreRewards.random()
				val quantity = getCoreQuantity(item)
				firstCores.add(RewardItem(item.type.name, quantity))
				BankedItem.create(firstPlaceNation, GlobalCompletions.toItemString(item), quantity)
			}
		}
		var firstKothBlockTotal = 0
		if (kothBlocks.isNotEmpty()) {
			repeat(3) {
				val quantity = randomInt(kothBlockQuantity.first, kothBlockQuantity.last)
				firstKothBlockTotal += quantity
				BankedItem.create(firstPlaceNation, kothBlockItemString, quantity)
			}
		}

		val firstBuffs = mutableListOf<RewardItem>()
		if (buffs.isNotEmpty()) {
			repeat(2) {
				val item = buffRewards.random()
				val quantity = getBuffQuantity(item)
				firstBuffs.add(RewardItem(item.type.name, quantity))
				BankedItem.create(firstPlaceNation, GlobalCompletions.toItemString(item), quantity)
			}
		}

		val firstPvps = mutableListOf<RewardItem>()
		if (pvpRewards != null) {
			repeat(10) {
				val item = pvpRewards.random()
				val quantity = getPvpQuantity(item)
				firstPvps.add(RewardItem(item.type.name, quantity))
				BankedItem.create(firstPlaceNation, GlobalCompletions.toItemString(item), quantity)
			}
		}

		val firstBreakdown = createRewardBreakdown(firstPlacePoints, firstOres, firstCores, firstKothBlockTotal, firstBuffs, firstPvps)
		val firstDetailsText = text("[Details]", HE_LIGHT_BLUE).hoverEvent(firstBreakdown)

		val firstMessage = template(
			message = text("Your nation placed {0} and received rewards! {1}", HE_LIGHT_GRAY),
			paramColor = TextColor.fromHexString("#D4AF37"),
			useQuotesAroundObjects = false,
			"1st",
			firstDetailsText
		)
		for (player in Bukkit.getOnlinePlayers()) {
			if (PlayerCache[player].frontierNationOid == firstPlaceNation) {
				player.sendMessage { firstMessage }
			}
		}

		// SECOND PLACE
		val secondPlaceName = topThree[1] ?: return@asyncLocked
		val secondPlaceNation = FrontierNationCache.getByName(secondPlaceName) ?: return@asyncLocked

		val secondPlacePoints = (pointsToGive[1] * pointsMultiplier).toInt()
		FrontierNation.updatePoints(secondPlaceNation, secondPlacePoints)

		val secondOres = mutableListOf<RewardItem>()
		if (ores.isNotEmpty()) {
			repeat(2) {
				val item = oreRewards.random()
				val quantity = getOreQuantity(item)
				secondOres.add(RewardItem(item.type.name, quantity))
				BankedItem.create(secondPlaceNation, GlobalCompletions.toItemString(item), quantity)
			}
		}

		val secondCores = mutableListOf<RewardItem>()
		if (cores.isNotEmpty()) {
			repeat(4) {
				val item = coreRewards.random()
				val quantity = getCoreQuantity(item)
				secondCores.add(RewardItem(item.type.name, quantity))
				BankedItem.create(secondPlaceNation, GlobalCompletions.toItemString(item), quantity)
			}
		}

		var secondKothBlockTotal = 0
		if (kothBlocks.isNotEmpty()) {
			repeat(2) {
				val quantity = randomInt(kothBlockQuantity.first, kothBlockQuantity.last)
				secondKothBlockTotal += quantity
				BankedItem.create(secondPlaceNation, kothBlockItemString, quantity)
			}
		}

		val secondBuffs = mutableListOf<RewardItem>()
		if (buffs.isNotEmpty()) {
			repeat(2) {
				val item = buffRewards.random()
				val quantity = getBuffQuantity(item)
				secondBuffs.add(RewardItem(item.type.name, quantity))
				BankedItem.create(secondPlaceNation, GlobalCompletions.toItemString(item), quantity)
			}
		}

		val secondPvps = mutableListOf<RewardItem>()
		if (pvpRewards != null) {
			repeat(6) {
				val item = pvpRewards.random()
				val quantity = getPvpQuantity(item)
				secondPvps.add(RewardItem(item.type.name, quantity))
				BankedItem.create(secondPlaceNation, GlobalCompletions.toItemString(item), quantity)
			}
		}

		val secondBreakdown = createRewardBreakdown(secondPlacePoints, secondOres, secondCores, secondKothBlockTotal, secondBuffs, secondPvps)
		val secondDetailsText = text("[Details]", HE_LIGHT_BLUE).hoverEvent(secondBreakdown)

		val secondMessage = template(
			message = text("Your nation placed {0} and received rewards! {1}", HE_LIGHT_GRAY),
			paramColor = TextColor.fromHexString("#C0C0C0"),
			useQuotesAroundObjects = false,
			"2nd",
			secondDetailsText
		)
		for (player in Bukkit.getOnlinePlayers()) {
			if (PlayerCache[player].frontierNationOid == secondPlaceNation) {
				player.sendMessage { secondMessage }
			}
		}

		// THIRD PLACE
		val thirdPlaceName = topThree[2] ?: return@asyncLocked
		val thirdPlaceNation = FrontierNationCache.getByName(thirdPlaceName) ?: return@asyncLocked

		val thirdPlacePoints = (pointsToGive[2] * pointsMultiplier).toInt()
		FrontierNation.updatePoints(thirdPlaceNation, thirdPlacePoints)

		val thirdOres = mutableListOf<RewardItem>()
		if (ores.isNotEmpty()) {
			repeat(1) {
				val item = oreRewards.random()
				val quantity = getOreQuantity(item)
				thirdOres.add(RewardItem(item.type.name, quantity))
				BankedItem.create(thirdPlaceNation, GlobalCompletions.toItemString(item), quantity)
			}
		}

		val thirdCores = mutableListOf<RewardItem>()
		if (cores.isNotEmpty()) {
			repeat(2) {
				val item = coreRewards.random()
				val quantity = getCoreQuantity(item)
				thirdCores.add(RewardItem(item.type.name, quantity))
				BankedItem.create(thirdPlaceNation, GlobalCompletions.toItemString(item), quantity)
			}
		}

		var thirdKothBlockTotal = 0
		if (kothBlocks.isNotEmpty()) {
			repeat(1) {
				val quantity = randomInt(kothBlockQuantity.first, kothBlockQuantity.last)
				thirdKothBlockTotal += quantity
				BankedItem.create(thirdPlaceNation, kothBlockItemString, quantity)
			}
		}

		val thirdBuffs = mutableListOf<RewardItem>()
		if (buffs.isNotEmpty()) {
			repeat(1) {
				val item = buffRewards.random()
				val quantity = getBuffQuantity(item)
				thirdBuffs.add(RewardItem(item.type.name, quantity))
				BankedItem.create(thirdPlaceNation, GlobalCompletions.toItemString(item), quantity)
			}
		}

		val thirdPvps = mutableListOf<RewardItem>()
		if (pvpRewards != null) {
			repeat(4) {
				val item = pvpRewards.random()
				val quantity = getPvpQuantity(item)
				thirdPvps.add(RewardItem(item.type.name, quantity))
				BankedItem.create(thirdPlaceNation, GlobalCompletions.toItemString(item), quantity)
			}
		}

		val thirdBreakdown = createRewardBreakdown(thirdPlacePoints, thirdOres, thirdCores, thirdKothBlockTotal, thirdBuffs, thirdPvps)
		val thirdDetailsText = text("[Details]", HE_LIGHT_BLUE).hoverEvent(thirdBreakdown)

		val thirdMessage = template(
			message = text("Your nation placed {0} and received rewards! {1}", HE_LIGHT_GRAY),
			paramColor = TextColor.fromHexString("#CD7F32"),
			useQuotesAroundObjects = false,
			"3rd",
			thirdDetailsText
		)
		for (player in Bukkit.getOnlinePlayers()) {
			if (PlayerCache[player].frontierNationOid == thirdPlaceNation) {
				player.sendMessage { thirdMessage }
			}
		}
	}

	//Starts a 15 minute activating timer before starting
	fun activateKoth(koth: RegionKothZone) = asyncLocked {
		if (!activatingKoths.isEmpty() || !activeKoths.isEmpty()) return@asyncLocked
		activatingKoths[koth] = System.nanoTime()
		val message = template(
			"KOTH {0} is starting in 15 minutes at ${koth.x}, ${koth.z}, (/jump ${koth.x} ${koth.z})!",
			color = HE_MEDIUM_GRAY,
			paramColor = HEColorScheme.HE_LIGHT_BLUE,
			useQuotesAroundObjects = false,
			koth.name
		)

		val message2 = template(
			"Moon KOTH {0} is starting in 15 minutes at ${koth.x}, ${koth.z} on {1}, (/jump {1})!",
			color = HE_MEDIUM_GRAY,
			paramColor = HEColorScheme.HE_LIGHT_BLUE,
			useQuotesAroundObjects = false,
			koth.name, koth.world
		)
		if (koth.type == KothType.MOON) {
			IonServer.server.sendMessage(message2)
			if (ConfigurationFiles.legacySettings().master) Discord.sendEmbed(
				ConfigurationFiles.discordSettings().globalChannel, Embed(
					description = "Moon KOTH ${koth.name} is starting in 15 minutes at ${koth.x}, ${koth.z} on ${koth.world}!"
				)
			)
		}
		else {
			IonServer.server.sendMessage(message)
			if (ConfigurationFiles.legacySettings().master) Discord.sendEmbed(
				ConfigurationFiles.discordSettings().globalChannel, Embed(
					description = "KOTH ${koth.name} is starting in 15 minutes at ${koth.x}, ${koth.z}!"
				)
			)
		}
	}


	//Begins an activating KOTH
	fun beginKoth() = asyncLocked {

		for (currentKoth in getCurrentKoth()) {

			val currentKothID = currentKoth.id
			if (System.nanoTime() - activatingKoths[currentKoth]!! < TimeUnit.MINUTES.toNanos(15.toLong())) continue
			val kothType = currentKoth.type
			val timeLimit = when (kothType) {
				KothType.MAJOR -> NATIONS_BALANCE.koths.majorKOTHMaxDuration
				KothType.MINOR -> NATIONS_BALANCE.koths.minorKOTHMaxDuration
				KothType.MOON -> NATIONS_BALANCE.koths.majorKOTHMaxDuration
			}

			activatingKoths.remove(currentKoth)
			KothSiege.create(currentKothID)

			activeKoths.add(Koths(currentKothID, currentTimeMillis(), mutableMapOf(), null, kothType, timeLimit))

			val message = template(
				"KOTH {0} has begun at ${currentKoth.x}, ${currentKoth.z}!",
				color = HE_DARK_ORANGE,
				paramColor = HEColorScheme.HE_LIGHT_ORANGE,
				useQuotesAroundObjects = false,
				currentKoth.name
			)

			IonServer.server.sendMessage(message)

			Discord.sendMessage(
				ConfigurationFiles.discordSettings().eventsChannel,
				"<gold>King of the hill ${currentKoth.name} has begun!"
			)
		}
	}

	fun isActiveKoth(kothID: Oid<KothStation>) = activeKoths.any { it.kothId == kothID }

	fun forceActivateKoth(desiredKoth: String) {
		val allKoths = Regions.getAllOf<RegionKothZone>()
		val iminentKoths = Regions.getAllOf<RegionKothZone>()
			.filter { koth -> koth.siegeHour == currentHour() || koth.siegeHour == currentHour() + 1 }
		for (koth in allKoths) {
			if (koth.name == desiredKoth) {
				if (iminentKoths.contains(koth)) return
				activateKoth(koth)
				return
			}
		}
	}

	fun getKOTHS(): Iterable<Koths> = activeKoths

	fun moonSiegeActiveOrStaging(siege: CachedKothStation): Boolean {
		return siege.siegeHour in (ZonedDateTime.now().hour - 1)..ZonedDateTime.now().hour
	}
}
