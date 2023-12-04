package net.horizonsend.ion.server.features.nations

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.CapturableStation
import net.horizonsend.ion.common.database.schema.nations.CapturableStationSiege
import net.horizonsend.ion.common.database.schema.nations.NationRelation
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.informationAction
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.miscellaneous.getDurationBreakdown
import net.horizonsend.ion.common.utils.text.HEColorScheme
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.achievements.Achievement
import net.horizonsend.ion.server.features.achievements.rewardAchievement
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionCapturableStation
import net.horizonsend.ion.server.features.progression.SLXP
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.event.StarshipPilotedEvent
import net.horizonsend.ion.server.features.starship.event.StarshipUnpilotedEvent
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.gt
import java.lang.System.currentTimeMillis
import java.time.ZonedDateTime
import java.util.Date
import java.util.concurrent.TimeUnit

object StationSieges : IonServerComponent() {
	data class Siege(var siegerId: SLPlayerId, val stationId: Oid<CapturableStation>, val start: Long)

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
					player.informationAction("$elapsedSecondsDecimal minutes remaining")
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
			val message = template(
				"Siege Station {0}'s siege hour has ended.",
				color = HEColorScheme.HE_MEDIUM_GRAY,
				paramColor = HEColorScheme.HE_LIGHT_BLUE,
				lastStationName
			)

			IonServer.server.sendMessage(message)
			if (IonServer.legacySettings.master) Notify.sendDiscord(Notify.getChannel("global"), message.plainText())
		}

		val stations = Regions.getAllOf<RegionCapturableStation>()
			.filter { station -> station.siegeTimeFrame == lastQuarter }

		for (station in stations) {
			val message = template(
				"Siege Station {0}'s siege hour has began! It can be besieged for the rest of the hour with /siege!.",
				color = HEColorScheme.HE_MEDIUM_GRAY,
				paramColor = HEColorScheme.HE_LIGHT_BLUE,
				station.name
			).hoverEvent(text("${station.world} : (${station.x}, ${station.z})"))

			IonServer.server.sendMessage(message)
			if (IonServer.legacySettings.master) Notify.sendDiscord(Notify.getChannel("global"), message.plainText())
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

		Notify.online(MiniMessage.miniMessage().deserialize("<gold>Siege of Space Station $stationName by $playerName has failed!"))
		Notify.eventsChannel("Siege of Space Station **$stationName** by **$playerName** has failed!")
	}

	fun beginSiege(player: Player) = asyncLocked {
		val nation = PlayerCache[player].nationOid
			?: return@asyncLocked player.userError("You need to be in a nation to siege a station.")

		val station = Regions.findFirstOf<RegionCapturableStation>(player.location)
			?: return@asyncLocked player.userError("You must be within a station's area to siege it.")

		if (station.nation?.let { RelationCache[nation, it].ordinal >= 5 } == true) {
			return@asyncLocked player.userError("This station is owned by an ally of your nation.")
		}

		val stationId = station.id
		when {
			station.nation?.id == nation.id -> {
				return@asyncLocked player.userError("Your nation already owns this station.")
			}

			isUnderSiege(stationId) -> {
				return@asyncLocked player.userError("This station is already under siege!")
			}

			station.siegeTimeFrame != currentHour() -> {
				return@asyncLocked player.userError(
					"This station can only be besieged in " +
						"quarter ${station.siegeTimeFrame}" + "of the day (EST timezone), but the current quarter is ${currentHour()}"
				)
			}
		}

		if (isUnderSiege(stationId)) {
			player.userError("This station is already under siege!")
			return@asyncLocked
		}

		val playerId = player.uniqueId.slPlayerId

		if (sieges.any { it.siegerId == playerId }) {
			player.userError("You are already besieging a station")
			return@asyncLocked
		}

		if (!isInBigShip(player)) {
			player.userError("You cannot siege in a ship smaller then 2000 blocks.")
			return@asyncLocked
		}

		// only allow nations to siege multiple times within the time period if it's simultaneous
		if (sieges.none { Bukkit.getPlayer(it.siegerId.uuid)?.let(PlayerCache::get)?.nationOid == nation }) {
			val daysPerSiege = NATIONS_BALANCE.capturableStation.daysPerSiege
			val duration = (TimeUnit.DAYS.toMillis(1) * daysPerSiege).toLong()
			val date = Date(currentTimeMillis() - duration)

			val lastSiege: CapturableStationSiege? = CapturableStationSiege
				.find(and(CapturableStationSiege::nation eq nation, CapturableStationSiege::time gt date))
				.maxBy { it.time }

			if (lastSiege != null) {
				val remainingTime = lastSiege.time.time + duration - currentTimeMillis()
				player.information(
					"Your nation has already besieged stations in the past $daysPerSiege day(s)!" +
						" Time until next siege: ${getDurationBreakdown(remainingTime)}"
				)
				player.information(
					"Note: Please do not try to bypass this restriction using " +
						"exploits such as splitting into multiple nations. This would be considered exploiting and against the rules."
				)
				return@asyncLocked
			}
		}

		if (!VAULT_ECO.has(player, NATIONS_BALANCE.capturableStation.siegeCost.toDouble())) {
			player.userError("You need C${NATIONS_BALANCE.capturableStation.siegeCost} to begin a siege.")
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

		Notify.online(MiniMessage.miniMessage().deserialize("<gold>${player.name} of $nationName began a siege on Space Station ${station.name}! (Current Nation: $oldNationName)"))
		Notify.eventsChannel("**${player.name}** of $nationName has initiated a siege on $oldNationName's Space Station ${station.name}")

		player.rewardAchievement(Achievement.SIEGE_STATION)
	}

	fun isUnderSiege(stationId: Oid<CapturableStation>) = sieges.any { it.stationId == stationId }

	fun getSieges(): Iterable<Siege> = sieges

	fun capture(player: Player, stationId: Oid<CapturableStation>) {
		val station: RegionCapturableStation = Regions[stationId]
		val world: World = Bukkit.getWorld(station.world) ?: return
		val oldNation = station.nation

		val playerNation = PlayerCache[player].nationOid
		if (playerNation == null) {
			player.userError("You need to be in a nation to siege a station.")
			return
		}

		if (oldNation != null) {
			var count = 0
			for (otherPlayer in world.players) {
				if (!station.contains(otherPlayer.location)) continue

				if (!isInBigShip(otherPlayer)) continue

				val otherNation = PlayerCache[otherPlayer].nationOid ?: continue

				// includes NATION relation, so this includes same nation

				// Get the relation between the current station holder and the other players inside
				val relationOld = RelationCache[oldNation, otherNation]

				if (relationOld.ordinal <= 4) continue

				// Get the relation between the sieging nation and the other players inside
				val relationNew = RelationCache[playerNation, otherNation]

				// Ignore this player if they're also allies to the sieging nation
				if (relationNew.ordinal >= 5) continue

				count++
			}
			if (count > 0) {
				player.alert(
					"$count members of the defending station's nation " +
						"or its allied nations are piloting ships in the region! " +
						"Remove them to complete the siege."
				)
				return
			}
		}

		asyncLocked {
			val slPlayerId = player.slPlayerId

			sieges.removeIf { it.siegerId == slPlayerId }

			if (station.nation == playerNation) {
				player.userError("This station is already captured by your nation, capture failed.")
				return@asyncLocked
			}

			sieges.removeIf { it.siegerId == slPlayerId }

			CapturableStation.setNation(stationId, playerNation)

			val nationName = NationCache[playerNation].name
			val oldNationName = oldNation?.let { NationCache[it].name } ?: "None"
			val nowCaptured = CapturableStation.count(CapturableStation::nation eq playerNation)
			val playerName = player.name

			Notify online MiniMessage.miniMessage().deserialize("<gold>Space Station ${station.name} has been captured by $playerName of $nationName from $oldNationName." +
				" $nationName now has $nowCaptured stations!")
			Notify eventsChannel "Space Station **${station.name}** has been captured by **$playerName of $nationName** from **$oldNationName**"

			SLXP.addAsync(player, NATIONS_BALANCE.capturableStation.siegerXP)

			Tasks.sync {
				for (otherPlayer in world.players) {
					if (otherPlayer.slPlayerId == slPlayerId) continue
					if (oldNation == null) continue
					if (!station.contains(otherPlayer.location)) continue

					val otherPlayerNation = PlayerCache[otherPlayer].nationOid ?: continue

					if (RelationCache[playerNation, otherPlayerNation].ordinal >= NationRelation.Level.ALLY.ordinal) {

						SLXP.addAsync(otherPlayer, NATIONS_BALANCE.capturableStation.siegerAllyXP)
						player.rewardAchievement(Achievement.CAPTURE_STATION)
					}
				}
			}
		}

		player.rewardAchievement(Achievement.CAPTURE_STATION)
	}

	private fun isInBigShip(player: Player): Boolean {
		val starship = ActiveStarships.findByPilot(player) ?: return false
		return starship.initialBlockCount >= StarshipType.CORVETTE.minSize
	}

	fun getAllies(sieger: Player, stationId: Oid<CapturableStation>): List<Player> {
		val players = mutableListOf<Player>()

		val station: RegionCapturableStation = Regions[stationId]
		val world: World = sieger.world
		val oldNation = station.nation

		val playerNation = PlayerCache[sieger].nationOid

		if (playerNation == null) {
			sieger.userError("You need to be in a nation to siege a station.")
			return players
		}

		for (otherPlayer in world.players) {
			if (otherPlayer.slPlayerId == sieger.slPlayerId) continue
			if (oldNation == null) continue
			if (!station.contains(otherPlayer.location)) continue

			val otherPlayerNation = PlayerCache[otherPlayer].nationOid ?: continue

			if (RelationCache[playerNation, otherPlayerNation].ordinal >= NationRelation.Level.ALLY.ordinal) {

				players.add(otherPlayer)
			}
		}

		return players
	}

	@EventHandler
	fun onPlayerQuit(event: PlayerQuitEvent) {
		val player = event.player

		val siege = sieges.find { it.siegerId == player.slPlayerId } ?: return
		val allies = getAllies(player, siege.stationId)

		if (allies.isEmpty()) return tryEndSiege(event.player)

		val newSieger = allies.firstOrNull() ?: return tryEndSiege(event.player)

		siege.siegerId = newSieger.slPlayerId
		Notify.online(text("Sieger ${player.name} disconnected, so the siege was transferred to ${newSieger.name}"))
	}

	@EventHandler
	fun onStarshipUnpilot(event: StarshipPilotedEvent) {
		tryEndSiege(event.player)
	}

	@EventHandler
	fun onStarshipUnpilot(event: StarshipUnpilotedEvent) {
		val player = (event.starship.controller as? PlayerController)?.player ?: return
		tryEndSiege(player)
	}
}
