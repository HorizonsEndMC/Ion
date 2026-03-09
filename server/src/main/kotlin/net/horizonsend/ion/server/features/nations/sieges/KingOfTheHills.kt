package net.horizonsend.ion.server.features.nations.sieges

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.FrontierNationCache
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.schema.economy.BankedItem
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.nations.FrontierNation
import net.horizonsend.ion.common.database.schema.nations.KothStation
import net.horizonsend.ion.common.database.schema.nations.KothSiege
import net.horizonsend.ion.common.database.schema.nations.KothType
import net.horizonsend.ion.common.extensions.informationAction
import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.common.utils.miscellaneous.randomInt
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_ORANGE
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
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.KOTH_BLOCK
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
import net.horizonsend.ion.server.miscellaneous.utils.ServerStage.getServerStage
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
					"${killer.name} accrued $points points for $verb ${player.name}.",
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
		val stage = getServerStage()
		if (stage < 2) return
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
		val stage = getServerStage()

		if (stage !in 2..4) return@asyncLocked log.error("Server age is wrong somehow!")

		val pointsToGive = when (kothType) {
			KothType.MAJOR -> listOf(75.0, 50.0, 25.0)
			KothType.MINOR -> listOf(35.0, 20.0, 10.0)
			KothType.MOON -> listOf(35.0, 20.0, 10.0)
		}
		val pointsMultiplier = when (stage) {
			3 -> 1.5
			4 -> 2.0
			else -> 1.0
		}

		val rewards = KothRewards.first { it.kothType == kothType && it.stage == stage }
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
		val pvpRewards = WeightedRandomList<ItemStack>().apply {
			for (pvp in pvps) addEntry(pvp.item, pvp.chance)
		}

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
			val quantity = 	pvps.find { it.item == item}?.amount ?: 1..1
			return safeRandomInt(quantity.first, quantity.last)
		}

		fun givePvpRewards(nation: Oid<FrontierNation>, count: Int) {
			if (pvpRewards.isEmpty()) return
			repeat(count) {
				val item = pvpRewards.random()
				BankedItem.create(nation, GlobalCompletions.toItemString(item), getPvpQuantity(item))
			}
		}

		val kothBlock = KOTH_BLOCK.getValue().constructItemStack()
		val kothBlockItemString = GlobalCompletions.toItemString(kothBlock)
		val kothBlockQuantity = kothBlocks[0].amount

		// FIRST PLACE
		val firstPlaceName = topThree[0] ?: return@asyncLocked
		val firstPlaceNation = FrontierNationCache.getByName(firstPlaceName) ?: return@asyncLocked

		FrontierNation.updatePoints(firstPlaceNation, (pointsToGive[0] * pointsMultiplier).toInt())

		repeat(3) {
			val item = oreRewards.random()
			BankedItem.create(firstPlaceNation, GlobalCompletions.toItemString(item), getOreQuantity(item))
		}
		repeat(6) {
			val item = coreRewards.random()
			BankedItem.create(firstPlaceNation, GlobalCompletions.toItemString(item), getCoreQuantity(item))
		}
		repeat(3) {
			BankedItem.create(firstPlaceNation, kothBlockItemString, randomInt(kothBlockQuantity.first, kothBlockQuantity.last)) //!!!
		}
		repeat(2) {
			val item = buffRewards.random()
			BankedItem.create(firstPlaceNation, GlobalCompletions.toItemString(item), getBuffQuantity(item))
		}
		if (pvps.isNotEmpty()) givePvpRewards(firstPlaceNation, 10)


		// SECOND PLACE
		val secondPlaceName = topThree[1] ?: return@asyncLocked
		val secondPlaceNation = FrontierNationCache.getByName(secondPlaceName) ?: return@asyncLocked

		FrontierNation.updatePoints(secondPlaceNation, (pointsToGive[1] * pointsMultiplier).toInt())

		repeat(2) {
			val item = oreRewards.random()
			BankedItem.create(secondPlaceNation, GlobalCompletions.toItemString(item), getOreQuantity(item))
		}
		repeat(4) {
			val item = coreRewards.random()
			BankedItem.create(secondPlaceNation, GlobalCompletions.toItemString(item), getCoreQuantity(item))
		}
		repeat(2) {
			BankedItem.create(secondPlaceNation, kothBlockItemString, randomInt(kothBlockQuantity.first, kothBlockQuantity.last))
		}
		repeat(2) {
			val item = buffRewards.random()
			BankedItem.create(secondPlaceNation, GlobalCompletions.toItemString(item), getBuffQuantity(item))
		}
		if (pvps.isNotEmpty()) givePvpRewards(secondPlaceNation, 6)


		// THIRD PLACE
		val thirdPlaceName = topThree[2] ?: return@asyncLocked
		val thirdPlaceNation = FrontierNationCache.getByName(thirdPlaceName) ?: return@asyncLocked

		FrontierNation.updatePoints(thirdPlaceNation, (pointsToGive[2] * pointsMultiplier).toInt())

		repeat(1) {
			val item = oreRewards.random()
			BankedItem.create(thirdPlaceNation, GlobalCompletions.toItemString(item), getOreQuantity(item))
		}
		repeat(2) {
			val item = coreRewards.random()
			BankedItem.create(thirdPlaceNation, GlobalCompletions.toItemString(item), getCoreQuantity(item))
		}
		repeat(1) {
			BankedItem.create(thirdPlaceNation, kothBlockItemString, randomInt(kothBlockQuantity.first, kothBlockQuantity.last))
		}
		repeat(1) {
			val item = buffRewards.random()
			BankedItem.create(thirdPlaceNation, GlobalCompletions.toItemString(item), getBuffQuantity(item))
		}
		if (pvps.isNotEmpty()) givePvpRewards(thirdPlaceNation, 4)
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
