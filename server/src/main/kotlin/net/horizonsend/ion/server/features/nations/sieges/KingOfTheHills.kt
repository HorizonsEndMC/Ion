package net.horizonsend.ion.server.features.nations.sieges

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.FrontierNationCache
import net.horizonsend.ion.common.database.schema.economy.BankedItem
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.nations.FrontierNation
import net.horizonsend.ion.common.database.schema.nations.KothStation
import net.horizonsend.ion.common.database.schema.nations.KothSiege
import net.horizonsend.ion.common.extensions.informationAction
import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.common.utils.miscellaneous.randomInt
import net.horizonsend.ion.common.utils.text.HORIZONS_END_BRACKETED
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.formatFrontierNationName
import net.horizonsend.ion.common.utils.text.lineBreak
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.GlobalCompletions
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.KOTH_BLOCK
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.chat.Discord
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
import net.horizonsend.ion.server.miscellaneous.utils.Notify
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
		var kothPoints: MutableMap<Oid<KothStation>, MutableMap<Oid<FrontierNation>, Int?>>,
		var nation: Oid<FrontierNation>?,
		val type: Boolean,
		val timeLimit: Long
	)

	private val activeKoths = mutableListOf<Koths>()
	private val activatingKoths = mutableMapOf<RegionKothZone, Long>()

	//private val kothMaxTimeMillis get() = TimeUnit.MINUTES.toMillis(NATIONS_BALANCE.koths.majorKOTHMaxDuration)

	private fun currentHour() = ZonedDateTime.now().hour

	private val kothScores = mutableMapOf<Oid<KothStation>, MutableMap<Oid<FrontierNation>, Int?>>()

	private var nation: Oid<FrontierNation>? = null


	override fun onEnable() {
		Tasks.syncRepeat(20, 20) {
			updateKOTHs()
			updateQuarter()
			beginKoth()
		}
		Tasks.syncRepeat(0L, 20L * TimeUnit.MINUTES.toSeconds(10)) {
			displayKothLeaderboard()
		}
	}

	private fun updateKOTHs() {
		for (koth: Koths in getKOTHS()) {
			val memberCount = mutableMapOf<Oid<FrontierNation>, Int?>()
			val kothId = koth.kothId
			val kothRegion: RegionKothZone = Regions[kothId]
			val elapsed = currentTimeMillis() - koth.start
			val timeLimit = TimeUnit.MINUTES.toMillis(koth.timeLimit)
			val world: World = Bukkit.getWorld(kothRegion.world) ?: return

			for (player in world.players) {
				if (!kothRegion.contains(player.location)) continue
				val playerNation = PlayerCache[player].frontierNationOid
				if (playerNation == null || !isPiloting(player)) continue
				//If the player's nation hasnt gotten a player in the koth this loop yet:
				if (!memberCount.contains(playerNation)) {
					player.rewardAchievement(Achievement.KOTH_PARTICIPATION)
					memberCount[playerNation] = 1
					//If the player's nation hasnt gotten a player in the koth at all this siege:
					if (!kothScores[kothId]!!.contains(playerNation)) {
						kothScores[kothId]!![playerNation] = 1
					}
				}
				//If the player isnt the first of his nation to be in the koth, add 1 to his nation's member count
				else {
					val personalNationCount = memberCount[playerNation]!!
					val newCount = personalNationCount.plus(1)
					memberCount[playerNation] = newCount
				}

				//if there are people in the Koth, find the nation with the most people
				if (!memberCount.isEmpty()) {
					//if there has been no nations in this koth, just set it to the first nation in the member count
					if (nation == null) {nation = memberCount.keys.firstNotNullOf { it }}
					//Find the nation with the highest member count, if nobody participated return the nation from the line above
					val dominantNation = findDominantNation(memberCount, nation!!)
					FrontierNation.updatePoints(dominantNation, 1)
					//If the dominant nation this time isnt the same as last loop
					if (dominantNation != nation) {
						log.info("Nation ${dominantNation} has taken control of KOTH ${kothId}")
						Notify.chatAndGlobal(
							MiniMessage.miniMessage()
								.deserialize("<gold><bold>Nation ${dominantNation.id} has taken control of the KOTH ${kothId}!")
						)
						Discord.sendMessage(
							ConfigurationFiles.discordSettings().eventsChannel,
							"<gold><bold>Nation ${dominantNation.id} has taken control of the KOTH ${kothId}!"
						)
					}
					//Set the nation as this loop's dominant nation for next time
					nation = dominantNation
				}
				//Get the scores of this koth
				val thisKothsScores = kothScores[kothId]
				//if there is a dominant nation
				if (nation != null) {
					//give them a point
					thisKothsScores!![nation]!!.plus(1)
				}

				when {

					elapsed > timeLimit -> endKoth(koth)
					else -> {
						for (player in world.players) {
							if (!kothRegion.contains(player.location)) continue
							val elapsedSecondsDecimal = TimeUnit.MILLISECONDS.toSeconds(timeLimit - elapsed) / 60.0
							player.informationAction("${String.format("%.2f", elapsedSecondsDecimal)} minutes remaining")
							CombatTimer.refreshPvpTimer(player, CombatTimer.REASON_IN_KOTH)
						}
					}
				}
			}
		}
	}


	fun findDominantNation(numbers: MutableMap<Oid<FrontierNation>, Int?>, nation: Oid<FrontierNation>): Oid<FrontierNation> {
		if (numbers.isEmpty()) return nation
		val orderedNation = numbers.entries
			.sortedByDescending { it.value }
			.associate { it.key to it.value }
		val dominantNationAndCount = orderedNation.entries.first()
		val dominantNation = dominantNationAndCount.key
		return dominantNation
	}

	fun displayKothLeaderboard() {
		for (koth: Koths in getKOTHS()) {
			val kothRegion: RegionKothZone = Regions[koth.kothId]
			val message = text("Scores for KOTH ${kothRegion.name}:").color(TextColor.fromHexString("#FFD700"))
				.decorate(TextDecoration.BOLD)
			val lineBreak = lineBreak(45)
			message.append(lineBreak)
			val scores = kothScores[koth.kothId] ?: return
			val orderedScores = scores.entries
				.sortedByDescending { it.value }
				.associate { it.key to it.value }
			for ((index, score) in orderedScores.entries.withIndex()) {
				if (score.value == null) continue
				val nation = FrontierNation.findById(score.key) ?: continue
				val position = index + 1
				message.append(
					text("$position. ${nation.name} with ${score.value} points.").color(color(nation.color))
				)
				message.append(newline())
			}
			Notify.chatAndGlobal(message)
		}
	}


	@EventHandler
	fun onStarshipSink(event: StarshipSunkEvent) {
		val controller = event.previousController as? PlayerController ?: return
		val damager = event.starship.damagers
			.filter { it.key is PlayerDamager }
			.filter { SLPlayer[(it.key as PlayerDamager).player.name]?.frontierNation != SLPlayer[controller.player.name]?.frontierNation }
			.maxByOrNull { it.value.points.get() }?.key as? PlayerDamager ?: return

		for (koth: Koths in getKOTHS()) {
			val thisKoth = koth.kothId
			val kothRegion: RegionKothZone = Regions[thisKoth]
			val world: World = Bukkit.getWorld(kothRegion.world) ?: return
			if (kothRegion.contains(event.starship.centerOfMass.toLocation(world))) {
				val pointsGained = when {
					event.starship.initialBlockCount <= 4000 -> 1
					event.starship.initialBlockCount in 4001..12000 -> 2
					event.starship.initialBlockCount >= 12001 -> 4

					else -> return log.error("Pilot is flying something hitherto unknown to mankind.")
				}
				processKothKill(controller.player, damager.player, pointsGained, koth.kothId)
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
				processKothKill(event.player, killer, 1, koth.kothId)
				break
			}
		}
	}


	private fun processKothKill(player: Player, killer: Player, points: Int, kothId: Oid<KothStation>) {
		val victimNation = PlayerCache[player].frontierNationOid
		val killerNation = PlayerCache[killer].frontierNationOid
		kothScores[kothId]?.get(killerNation)?.plus(points)
		log.info("Awarded $killerNation $points points for killing ${player.name}")
		if (killerNation != null && victimNation != null) {
			IonServer.server.sendMessage(
				template(
					text("${killer.name} accrued ${points} points for killing ${player.name}."),
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
				val majorKoth = koths.find {it.type}
				if (majorKoth == null) return
				activateKoth(majorKoth)
			}
			else {
				val minorKoth = koths.find{!it.type}
				if (minorKoth == null) return
				activateKoth(minorKoth)
			}
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
		giveRewards(topThree, koths)
		activeKoths.remove(koths)

		val kothName = KothStation.findPropById(koths.kothId, KothStation::name) ?: "??NULL??"

		val message = ofChildren(
			text("The King of the Hill $kothName has ended!", HE_MEDIUM_GRAY), Component.newline(),
			if (topThree[0] != null) {text("First place: ${topThree[0]}").color(TextColor.fromHexString("#D4AF37"))} else Component.empty(), Component.newline(), //Gold
			if (topThree[1] != null) {text("Second place: ${topThree[1]}").color(TextColor.fromHexString("#C0C0C0"))} else Component.empty(), Component.newline(), //Silver
			if (topThree[2] != null) {text("Third place: ${topThree[2]}\"}").color(TextColor.fromHexString("#CD7F32"))} else Component.empty(), Component.newline() //Bronze)
		)
		Notify.chatAndGlobal(message)
		Discord.sendMessage(ConfigurationFiles.discordSettings().eventsChannel, message)
	}

	private fun determineWinner(koths: Koths): List<String?> {
		val rawScores = kothScores[koths.kothId] ?: return listOf(null, null, null)
		val kothType = koths.type
		val pointsToGive = when {
			kothType -> listOf(75, 50, 25)
			else -> listOf(35, 20, 10)
		}

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
			FrontierNation.updatePoints(leaderboard.entries.elementAt(0).key, pointsToGive[0])
			firstPlace = FrontierNationCache[leaderboard.entries.elementAt(0).key].name
		}
		if (numNationsParticipated > 1) {
			FrontierNation.updatePoints(leaderboard.entries.elementAt(1).key, pointsToGive[1])
			secondPlace = FrontierNationCache[leaderboard.entries.elementAt(1).key].name
		}
		if (numNationsParticipated > 2) {
			FrontierNation.updatePoints(leaderboard.entries.elementAt(2).key, pointsToGive[2])
			thirdPlace = FrontierNationCache[leaderboard.entries.elementAt(2).key].name
		}

		val topThree = listOf(firstPlace, secondPlace, thirdPlace)
		return topThree
	}

	private fun giveRewards(topThree: List<String?>, koth: Koths) {
		val kothType = if (koth.type) KingOfTheHillRewards.KothType.MAJOR else KingOfTheHillRewards.KothType.MINOR
		val stage = 0
		//TODO: val stage = getServerStage

		//Get the right table and its rewards

		val rewards = KothRewards.first { it.kothType == kothType && it.stage == stage }
		val ores = rewards.rewards.filter{it.rewardType == RewardType.MATERIALS}
		val cores = rewards.rewards.filter{it.rewardType == RewardType.CORES}
		val kothBlocks = rewards.rewards.filter{it.rewardType == RewardType.KOTHBLOCK}
		//val buffs = rewards.rewards.filter{it.rewardType == RewardType.BUFFS}

		val oreRewards = WeightedRandomList<ItemStack>().apply{
			for (ore in ores) {
				this.addEntry(ore.item.add(randomInt(ore.amount.first, ore.amount.last)), ore.chance)
			}
		}

		val coreRewards = WeightedRandomList<ItemStack>().apply{
			for (core in cores) {
				this.addEntry(core.item.add(randomInt(core.amount.first, core.amount.last)), core.chance)
			}
		}

		//FIRST PLACE REWARDS

		val firstPlaceName = topThree[0] ?: return
		val firstPlaceNation = FrontierNationCache.getByName(firstPlaceName) ?: return


		//TODO: Buff rewards

		for (i in 1..3) {
			val item = oreRewards.random()
			val itemString = GlobalCompletions.toItemString(item)
			BankedItem.create(firstPlaceNation, itemString)
		}

		for (i in 1..6) {
			val item = coreRewards.random()
			val itemString = GlobalCompletions.toItemString(item)
			BankedItem.create(firstPlaceNation, itemString)
		}

		val kothBlock = KOTH_BLOCK.getValue().constructItemStack(randomInt(kothBlocks[0].amount.first, kothBlocks[0].amount.last))
		val kothBlockItemString = GlobalCompletions.toItemString(kothBlock)
		for (i in 1..3) BankedItem.create(firstPlaceNation, kothBlockItemString)


		//SECOND PLACE REWARDS

		val secondPlaceName = topThree[1] ?: return
		val secondPlaceNation = FrontierNationCache.getByName(secondPlaceName) ?: return

		//TODO: Buff rewards

		for (i in 1..2) {
			val item = oreRewards.random()
			val itemString = GlobalCompletions.toItemString(item)
			BankedItem.create(secondPlaceNation, itemString)
		}

		for (i in 1..4) {
			val item = coreRewards.random()
			val itemString = GlobalCompletions.toItemString(item)
			BankedItem.create(secondPlaceNation, itemString)
		}
		for (i in 1..2) BankedItem.create(secondPlaceNation, kothBlockItemString)

		//THIRD PLACE REWARDS

		val thirdPlaceName = topThree[2] ?: return
		val thirdPlaceNation = FrontierNationCache.getByName(thirdPlaceName) ?: return

		//TODO: Buff rewards

		for (i in 1..1) {
			val item = oreRewards.random()
			val itemString = GlobalCompletions.toItemString(item)
			BankedItem.create(thirdPlaceNation, itemString)
		}

		for (i in 1..2) {
			val item = coreRewards.random()
			val itemString = GlobalCompletions.toItemString(item)
			BankedItem.create(thirdPlaceNation, itemString)
		}
		for (i in 1..1) BankedItem.create(thirdPlaceNation, kothBlockItemString)
	}

	//Starts a 15 minute activating timer before starting
	fun activateKoth(koth: RegionKothZone) = asyncLocked {
		if (!activatingKoths.isEmpty() || !activeKoths.isEmpty()) return@asyncLocked
		activatingKoths[koth] = System.nanoTime()
		val message = template(
			"KOTH {0} is starting in 15 minutes at ${koth.x}, ${koth.z}, (/jump ${koth.x} ${koth.z})!",
			color = HEColorScheme.HE_MEDIUM_GRAY,
			paramColor = HEColorScheme.HE_LIGHT_BLUE,
			useQuotesAroundObjects = false,
			koth.name
		)

		IonServer.server.sendMessage(message)
		if (ConfigurationFiles.legacySettings().master) Discord.sendEmbed(
			ConfigurationFiles.discordSettings().globalChannel, Embed(
				description = "KOTH ${koth.name} is starting in 15 minutes at ${koth.x}, ${koth.z}!"
			)
		)
	}


	//Begins an activating KOTH
	fun beginKoth() = asyncLocked {

		for (currentKoth in getCurrentKoth()) {

			val currentKothID = currentKoth.id
			if (System.nanoTime() - activatingKoths[currentKoth]!! > TimeUnit.MINUTES.toNanos(15.toLong())) continue
			val kothType = currentKoth.type
			val timeLimit = if(kothType) NATIONS_BALANCE.koths.majorKOTHMaxDuration else NATIONS_BALANCE.koths.minorKOTHMaxDuration

			activatingKoths.remove(currentKoth)
			KothSiege.create(currentKothID)

			activeKoths.add(Koths(currentKothID, currentTimeMillis(), kothScores, null, kothType, timeLimit))

			Notify.chatAndGlobal(
				MiniMessage.miniMessage().deserialize("<gold>King of the hill ${currentKoth.name} has begun!")
			)

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
			.filter { koth -> koth.siegeHour == currentHour()}
			.filter { koth -> koth.siegeHour == currentHour()+1 }
		for (koth in allKoths) {
			if (koth.name == desiredKoth) {
				if (iminentKoths.contains(koth)) return
				activateKoth(koth)
				return
			}
		}
	}


	fun getKOTHS(): Iterable<Koths> = activeKoths
}
