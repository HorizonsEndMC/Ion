package net.starlegacy.feature.nations

import java.lang.System.currentTimeMillis
import java.time.ZonedDateTime
import java.util.Date
import java.util.concurrent.TimeUnit
import net.horizonsend.ion.server.legacy.events.StationCaptureEvent
import net.horizonsend.ion.server.legacy.events.StationSiegeBeginEvent
import net.horizonsend.ion.server.legacy.feedback.FeedbackType.USER_ERROR
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackMessage
import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.md_5.bungee.api.ChatColor.GOLD
import net.starlegacy.SLComponent
import net.starlegacy.cache.nations.NationCache
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.database.Oid
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.schema.misc.SLPlayerId
import net.starlegacy.database.schema.nations.CapturableStation
import net.starlegacy.database.schema.nations.CapturableStationSiege
import net.starlegacy.database.schema.nations.Nation
import net.starlegacy.database.schema.nations.NationRelation
import net.starlegacy.database.slPlayerId
import net.starlegacy.database.uuid
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.feature.nations.region.types.RegionCapturableStation
import net.starlegacy.feature.progression.SLXP
import net.starlegacy.feature.starship.StarshipType
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.feature.starship.event.StarshipPilotedEvent
import net.starlegacy.feature.starship.event.StarshipUnpilotedEvent
import net.starlegacy.util.Notify
import net.starlegacy.util.Tasks
import net.starlegacy.util.VAULT_ECO
import net.starlegacy.util.colorize
import net.starlegacy.util.getDurationBreakdown
import net.starlegacy.util.msg
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.gt

object StationSieges : SLComponent() {
	data class Siege(val siegerId: SLPlayerId, val stationId: Oid<CapturableStation>, val start: Long)

	private val sieges = mutableListOf<Siege>()

	private val siegeMinTimeMillis get() = TimeUnit.MINUTES.toMillis(NATIONS_BALANCE.capturableStation.siegeMinDuration)
	private val siegeMaxTimeMillis get() = TimeUnit.MINUTES.toMillis(NATIONS_BALANCE.capturableStation.siegeMaxDuration)

	private fun currentHour() = ZonedDateTime.now().hour

	override fun onEnable() {
		Tasks.syncRepeat(20, 20) {
			updateSieges()
			updateQuarter()
		}
	}

	private fun updateSieges() {
		for (siege: Siege in getSieges()) {
			val player: Player? = Bukkit.getPlayer(siege.siegerId.uuid)
			val stationId = siege.stationId
			val station: RegionCapturableStation = Regions[stationId]
			val elapsed = currentTimeMillis() - siege.start
			when {
				player == null || !Regions.find(player.location).contains(station) -> endSiege(siege)
				elapsed > siegeMaxTimeMillis -> endSiege(siege)
				elapsed >= siegeMinTimeMillis -> capture(player, stationId)
				else -> {
					val elapsedSecondsDecimal = TimeUnit.MILLISECONDS.toSeconds(siegeMinTimeMillis - elapsed) / 60.0
					player.sendActionBar("$elapsedSecondsDecimal minutes remaining")
				}
			}
		}
	}

	private var lastQuarter = -1
	private var lastStations = listOf<String>()

	private fun updateQuarter() {
		val newQuarter = currentHour()
		if (lastQuarter == newQuarter) {
			return
		}
		log.info("Siege quarter change: $lastQuarter -> $newQuarter")
		lastQuarter = newQuarter
		lastStations.forEach { lastStationName ->
			Ion.server.broadcastMessage("&7Space Station &b$lastStationName&7's siege quarter has ended.".colorize())
		}
		val stations = Regions.getAllOf<RegionCapturableStation>()
			.filter { station -> station.siegeTimeFrame == lastQuarter }
		for (station in stations) {
			Ion.server.broadcastMessage("&7Space Station &b${station.name}&7's siege quarter has began! It can be besieged for the rest of the hour with /siege".colorize())
		}
		lastStations = stations.map { it.name }
	}

	fun getStationsNow() = Regions.getAllOf<RegionCapturableStation>()
		.filter { station -> station.siegeTimeFrame == currentHour() }

	override fun onDisable() {
		sieges.forEach(this::endSiege)
	}

	@Synchronized
	private fun locked(block: () -> Unit) = block()

	private fun asyncLocked(block: () -> Unit) = Tasks.async {
		locked(block)
	}

	fun tryEndSiege(player: Player) = asyncLocked {
		val slPlayerId = player.slPlayerId
		sieges.find { it.siegerId == slPlayerId }?.let(this::endSiege)
	}

	private fun endSiege(siege: Siege) = asyncLocked {
		sieges.remove(siege)

		val playerName = SLPlayer.getName(siege.siegerId) ?: "UNKNOWN"
		val stationName = CapturableStation.findPropById(siege.stationId, CapturableStation::name) ?: "??NULL??"

		Notify.online("${GOLD}Siege of Space Station $stationName by $playerName has failed!")
		Notify.discord("Siege of Space Station **$stationName** by **$playerName** has failed!")
	}

	fun beginSiege(player: Player) = asyncLocked {
		val nation = PlayerCache[player].nation
			?: return@asyncLocked player msg "&cYou need to be in a nation to siege a station."

		val station = Regions.findFirstOf<RegionCapturableStation>(player.location)
			?: return@asyncLocked player msg "&cYou must be within a station's area to siege it"

		for (relation in NationRelation.find(NationRelation::nation eq station.nation)) {
			if (relation.other == nation && relation.actual == NationRelation.Level.ALLY)
				return@asyncLocked player.sendFeedbackMessage(USER_ERROR, "This station is owned by an ally of your nation")
		}

		val stationId = station.id
		when {
			station.nation?.id == nation.id -> {
				return@asyncLocked player msg "&cYour nation already owns this station."
			}

			isUnderSiege(stationId) -> {
				return@asyncLocked player msg "&cThis station is already under siege."
			}

			station.siegeTimeFrame != currentHour() -> {
				return@asyncLocked player msg "&cThis station can only be besieged in quarter ${station.siegeTimeFrame} " +
					"of the day (EST timezone), but the current quarter is ${currentHour()}"
			}
		}

		if (isUnderSiege(stationId)) {
			player msg "&cThis station is already under siege!"
			return@asyncLocked
		}

		val playerId = player.uniqueId.slPlayerId

		if (sieges.any { it.siegerId == playerId }) {
			player msg "&cYou are already besieging a station"
			return@asyncLocked
		}

		if (!isInBigShip(player)) {
			player.sendFeedbackMessage(USER_ERROR, "You cannot siege in a ship smaller then 2000 blocks.")
			return@asyncLocked
		}

		// only allow nations to siege multiple times within the time period if it's simultaneous
		if (sieges.none { Bukkit.getPlayer(it.siegerId.uuid)?.let(PlayerCache::get)?.nation == nation }) {
			val daysPerSiege = NATIONS_BALANCE.capturableStation.daysPerSiege
			val duration = (TimeUnit.DAYS.toMillis(1) * daysPerSiege).toLong()
			val date = Date(currentTimeMillis() - duration)

			val lastSiege: CapturableStationSiege? = CapturableStationSiege
				.find(and(CapturableStationSiege::nation eq nation, CapturableStationSiege::time gt date))
				.maxBy { it.time }

			if (lastSiege != null) {
				val remainingTime = lastSiege.time.time + duration - currentTimeMillis()
				player msg "&cYour nation has already besieged stations in the past $daysPerSiege day(s)!" +
					" Time until next siege: ${getDurationBreakdown(remainingTime)}"
				player msg "&7Note: Please do not try to bypass this restriction using exploits such as splitting into multiple nations. " +
					"This would be considered exploiting and against the rules."
				return@asyncLocked
			}
		}

		if (!VAULT_ECO.has(player, NATIONS_BALANCE.capturableStation.siegeCost.toDouble())) {
			player msg "&cYou need C${NATIONS_BALANCE.capturableStation.siegeCost} to begin a siege."
			return@asyncLocked
		} else {
			VAULT_ECO.withdrawPlayer(player, NATIONS_BALANCE.capturableStation.siegeCost.toDouble())
		}

		val oldNation = station.nation

		if (oldNation == null) {
			capture(player, stationId)
			return@asyncLocked
		}

		CapturableStationSiege.create(stationId, nation)

		sieges.add(Siege(playerId, stationId, currentTimeMillis()))

		val nationName = NationCache[nation].name
		val oldNationName = NationCache[oldNation].name

		Notify.online("$GOLD${player.name} of $nationName began a siege on Space Station ${station.name}! (Current Nation: $oldNationName)")
		Notify.discord("**${player.name}** of $nationName has initiated a siege on $oldNationName's Space Station ${station.name}")

		StationSiegeBeginEvent(player).callEvent()

	}

	fun isUnderSiege(stationId: Oid<CapturableStation>) = sieges.any { it.stationId == stationId }

	fun getSieges(): Iterable<Siege> = sieges

	fun capture(player: Player, stationId: Oid<CapturableStation>) {
		val station: RegionCapturableStation = Regions[stationId]
		val world: World = Bukkit.getWorld(station.world) ?: return
		val oldNation = station.nation

		val playerNation = PlayerCache[player].nation
		if (playerNation == null) {
			player msg "&cYou need to be in a nation to siege a station."
			return
		}

		if (oldNation != null) {
			var count = 0
			for (otherPlayer in world.players) {
				if (!station.contains(otherPlayer.location)) {
					continue
				}
				if (!isInBigShip(otherPlayer)) {
					continue
				}
				val otherNation = PlayerCache[otherPlayer].nation
					?: continue
				// includes NATION relation, so this includes same nation
				var isOldNationAlly = false
				for (relation in NationRelation.find(NationRelation::nation eq oldNation)) {
					if (relation.other == otherNation && relation.actual == NationRelation.Level.ALLY)
						isOldNationAlly = true
				}
				var isNotNewNationAlly = false
				for (relation in NationRelation.find(NationRelation::nation eq playerNation)) {
					if (relation.other == otherNation && relation.actual == NationRelation.Level.ALLY)
						isNotNewNationAlly = true
				}
				if (isOldNationAlly && isNotNewNationAlly) {
					count++
				}
			}
			if (count > 0) {
				player msg "$count members of the defending station's nation " +
					"or its allied nations are piloting ships in the region! " +
					"Remove them to complete the siege."
				return
			}
		}

		asyncLocked {
			val slPlayerId = player.slPlayerId
			sieges.removeIf { it.siegerId == slPlayerId }
			if (station.nation == playerNation) {
				player msg "&cThis station is already captured by your nation, capture failed."
				return@asyncLocked
			}
			sieges.removeIf { it.siegerId == slPlayerId }
			CapturableStation.setNation(stationId, playerNation)
			val nationName = NationCache[playerNation].name
			val oldNationName = oldNation?.let { NationCache[it].name } ?: "None"
			val nowCaptured = CapturableStation.count(CapturableStation::nation eq playerNation)
			val playerName = player.name
			Notify online "${GOLD}Space Station ${station.name} has been captured by $playerName of $nationName from $oldNationName." +
				" $nationName now has $nowCaptured stations!"
			Notify discord "Space Station **${station.name}** has been captured by **$playerName of $nationName** from **$oldNationName**"
			SLXP.addAsync(player, NATIONS_BALANCE.capturableStation.siegerXP)
			Tasks.sync {
				for (otherPlayer in world.players) {
					if (otherPlayer.slPlayerId == slPlayerId) {
						continue
					}
					val playerNationId: Oid<Nation> = PlayerCache[otherPlayer].nation ?: continue
					for (relation in NationRelation.find(NationRelation::nation eq playerNation)) {
						if (relation.other == playerNationId && relation.actual == NationRelation.Level.ALLY)
							continue
					}
					if (!station.contains(otherPlayer.location)) {
						continue
					}
					SLXP.addAsync(otherPlayer, NATIONS_BALANCE.capturableStation.siegerAllyXP)
				}
			}
		}
		StationCaptureEvent(player).callEvent()
	}

	private fun isInBigShip(player: Player): Boolean {
		val starship = ActiveStarships.findByPilot(player) ?: return false
		return starship.blockCount >= StarshipType.CORVETTE.minSize
	}

	@EventHandler
	fun onPlayerQuit(event: PlayerQuitEvent) {
		tryEndSiege(event.player)
	}

	@EventHandler
	fun onStarshipUnpilot(event: StarshipPilotedEvent) {
		tryEndSiege(event.player)
	}

	@EventHandler
	fun onStarshipUnpilot(event: StarshipUnpilotedEvent) {
		tryEndSiege(event.player)
	}
}