package net.horizonsend.ion.server.features.nations.sieges

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.schema.nations.KothStation
import net.horizonsend.ion.common.database.schema.nations.KothSiege
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.extensions.informationAction
import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.formatNationName
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.chat.Discord
import net.horizonsend.ion.server.features.nations.NATIONS_BALANCE
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionKothZone
import net.horizonsend.ion.server.features.player.CombatTimer
import net.horizonsend.ion.server.features.progression.achievements.Achievement
import net.horizonsend.ion.server.features.progression.achievements.rewardAchievement
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.starship.PilotedStarships.isPiloting
import net.horizonsend.ion.server.features.starship.event.StarshipSunkEvent
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import java.lang.System.currentTimeMillis
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object KingOfTheHills : IonServerComponent() {
	data class Koths(
		val kothId: Oid<KothStation>,
		val start: Long,
		var kothPoints: MutableMap<Oid<KothStation>, MutableMap<Oid<Nation>, Int?>>,
		var nation: Oid<Nation>?
	)

	private val activeKoths = mutableListOf<Koths>()

	private val kothMaxTimeMillis get() = TimeUnit.MINUTES.toMillis(NATIONS_BALANCE.koths.majorKOTHMaxDuration)

	private fun currentHour() = ZonedDateTime.now().hour

	private val kothScores = mutableMapOf<Oid<KothStation>, MutableMap<Oid<Nation>, Int?>>()

	private var nation: Oid<Nation>? = null


	override fun onEnable() {
		Tasks.syncRepeat(20, 20) {
			updateKOTHs()
			updateQuarter()
		}
	}

	private fun updateKOTHs() {
		for (koth: Koths in getKOTHS()) {
			val memberCount = mutableMapOf<Oid<Nation>, Int?>()
			val kothId = koth.kothId
			val kothRegion: RegionKothZone = Regions[kothId]
			val elapsed = currentTimeMillis() - koth.start
			val world: World = Bukkit.getWorld(kothRegion.world) ?: return

			for (player in world.players) {
				if (!kothRegion.contains(player.location)) continue
				val playerNation = PlayerCache[player].nationOid
				if (playerNation == null || !isPiloting(player)) continue
				if (!memberCount.contains(playerNation)) {
					player.rewardAchievement(Achievement.KOTH_PARTICIPATION)
					memberCount[playerNation] = 1
					kothScores[kothId]?.contains(playerNation)?.let {
						if (!it) {
							kothScores[kothId]?.get(playerNation)?.plus(1)
							log.info("New nation ${playerNation} has entered the KOTH ${kothId}")
							Notify.chatAndGlobal(
								MiniMessage.miniMessage()
									.deserialize("<gold><bold>Nation ${playerNation.id} has entered the KOTH ${kothId}!")
							)
							Discord.sendMessage(
								ConfigurationFiles.discordSettings().eventsChannel,
								"<gold>Nation ${playerNation.id} has entered the KOTH!"
							)
						}
					}

				} else {
					val personalNationCount = memberCount[playerNation]
					val newCount = personalNationCount?.plus(1)
					memberCount[playerNation] = newCount
				}
			}

			val dominantNation = findDominantNation(memberCount)
			if (dominantNation != nation && dominantNation != null) {
				kothScores[kothId]?.get(dominantNation)?.plus(1)
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

			nation = dominantNation
			when {

				elapsed > kothMaxTimeMillis -> endKoth(koth)
				else -> {
					for (player in world.players) {
						if (!kothRegion.contains(player.location)) continue
						val elapsedSecondsDecimal = TimeUnit.MILLISECONDS.toSeconds(kothMaxTimeMillis - elapsed) / 60.0
						player.informationAction("${String.format("%.2f", elapsedSecondsDecimal)} minutes remaining")
						CombatTimer.refreshPvpTimer(player, CombatTimer.REASON_IN_KOTH)
					}
				}
			}
		}
	}


	fun findDominantNation(numbers: MutableMap<Oid<Nation>, Int?>): Oid<Nation>? {
		if (numbers.isEmpty()) return null
		val orderedNation = numbers.entries
			.sortedByDescending { it.value }
			.associate { it.key to it.value }
		val dominantNationAndCount = orderedNation.entries.first()
		val dominantNation = dominantNationAndCount.key
		return dominantNation
	}


	@EventHandler
	fun onStarshipSink(event: StarshipSunkEvent) {
		val controller = event.previousController as? PlayerController ?: return
		val damager = event.starship.damagers
			.filter { it.key is PlayerDamager }
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
		val victimNation = PlayerCache[player].nationOid
		val killerNation = PlayerCache[killer].nationOid

		kothScores[kothId]?.get(killerNation)?.plus(points)
		log.info("Awarded $killerNation $points points for killing ${player.name}")
		if (killerNation != null && victimNation != null) {
			IonServer.server.sendMessage(
				template(
					text("{0} accrued {1} points for killing {2}."),
					formatNationName(killerNation),
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
		log.info("Koth quarter change: $lastQuarter -> $newQuarter")
		lastQuarter = newQuarter
		lastStations.forEach { lastStationName ->
			val message = template(
				"KOTH {0}'s hour has ended.",
				color = HEColorScheme.HE_MEDIUM_GRAY,
				paramColor = HEColorScheme.HE_LIGHT_BLUE,
				useQuotesAroundObjects = false,
				lastStationName
			)

			IonServer.server.sendMessage(message)
			if (ConfigurationFiles.legacySettings().master) Discord.sendEmbed(
				ConfigurationFiles.discordSettings().globalChannel, (Embed(
					description = "KOTH $lastStationName's hour has ended."
				))
			)
		}

		val stations = Regions.getAllOf<RegionKothZone>()
			.filter { station -> station.siegeHour == lastQuarter }

		for (station in stations) {

			beginKoth()
			val message = template(
				"KOTH {0}'s hour has began! Enter the ring to participate!",
				color = HEColorScheme.HE_MEDIUM_GRAY,
				paramColor = HEColorScheme.HE_LIGHT_BLUE,
				useQuotesAroundObjects = false,
				station.name
			)

			IonServer.server.sendMessage(message)
			if (ConfigurationFiles.legacySettings().master) Discord.sendEmbed(
				ConfigurationFiles.discordSettings().globalChannel, Embed(
					description = "KOTH ${station.name}'s hour has began! Enter the ring to participate!"
				)
			)
		}
		lastStations = stations.map { it.name }
	}

	fun getCurrentKoth() = Regions.getAllOf<RegionKothZone>()
		.filter { koth -> koth.siegeHour == currentHour() }


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
		activeKoths.remove(koths)

		val kothName = KothStation.findPropById(koths.kothId, KothStation::name) ?: "??NULL??"

		Notify.chatAndGlobal(MiniMessage.miniMessage().deserialize("<gold>The King of the Hill $kothName has ended!\n" +
			if (topThree[0] != "None") {
				"<gold><bold>First place: ${topThree[0]}\n"} else {""}+
				if (topThree[1] != "None") {
			"<grey><bold>Second place: ${topThree[1]}\n"} else {""} +
				if (topThree[2] != "None") {
			"<red><bold>Third place: ${topThree[2]}"} else {""}
		))
		Discord.sendMessage(ConfigurationFiles.discordSettings().eventsChannel, "The King of the Hill $kothName has ended!\n" +
			if (topThree[0] != "None") {
				"First place: ${topThree[0]}\n"} else {""}+
			if (topThree[1] != "None") {
				"Second place: ${topThree[1]}\n"} else {""} +
			if (topThree[2] != "None") {
				"Third place: ${topThree[2]}"} else {""}
		)
	}

	private fun determineWinner(koths: Koths): MutableList<String> {
		val rawScores = kothScores[koths.kothId] ?: return mutableListOf("why")

		if (rawScores.isEmpty()) {
			Notify.chatAndGlobal(MiniMessage.miniMessage().deserialize("<gold>The King of the Hill has ended, nobody participated!"))
			Discord.sendMessage(ConfigurationFiles.discordSettings().eventsChannel, "King of the Hill event has ended, nobody participated!")
		}
		val leaderboard = rawScores.entries
			.sortedByDescending { it.value }
			.associate { it.key to it.value }
		val numNationsParticipated = leaderboard.size
		val firstPlace = if (numNationsParticipated > 0) NationCache[leaderboard.entries.elementAt(0).key].name else "None"
		val secondPlace = if (numNationsParticipated > 1) NationCache[leaderboard.entries.elementAt(1).key].name else "None"
		val thirdPlace = if (numNationsParticipated > 2) NationCache[leaderboard.entries.elementAt(2).key].name else "None"
		val topThree = mutableListOf(firstPlace, secondPlace, thirdPlace)
		return topThree
	}

	fun beginKoth() = asyncLocked {

		for (currentKoth in getCurrentKoth()) {

			val currentKothID = currentKoth.id


			KothSiege.create(currentKothID)

			activeKoths.add(Koths(currentKothID, currentTimeMillis(), kothScores, null))

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
				if(!activeKoths.isEmpty()) return
				if (iminentKoths.contains(koth)) return
				KothSiege.create(koth.id)
				activeKoths.add(Koths(koth.id, currentTimeMillis(), kothScores, null))
				Notify.chatAndGlobal(
					MiniMessage.miniMessage().deserialize("<gold>King of the hill ${koth.name} has begun!")
				)
				Discord.sendMessage(
					ConfigurationFiles.discordSettings().eventsChannel,
					"<gold>King of the hill ${koth.name} has begun!"
				)
				return
			}
		}
	}


	fun getKOTHS(): Iterable<Koths> = activeKoths

}
