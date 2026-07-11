package net.horizonsend.ion.server.features.nations.sieges

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.NationRelation
import net.horizonsend.ion.common.database.schema.nations.RegionalObjective
import net.horizonsend.ion.common.database.schema.nations.RegionalObjectiveType
import net.horizonsend.ion.common.database.schema.nations.RegionalObjectiveSiegeData
import net.horizonsend.ion.common.extensions.informationAction
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.miscellaneous.getDurationBreakdownString
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_ORANGE
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.command.GlobalCompletions
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionDominionTerritory
import net.horizonsend.ion.server.features.nations.region.types.RegionRegionalObjective
import net.horizonsend.ion.server.features.player.CombatTimer
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.starship.event.StarshipSunkEvent
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import java.util.Collections
import java.util.Date
import java.util.concurrent.TimeUnit

object RegionalObjectiveSieges : IonServerComponent() {

	data class ActiveSiege(
		val objectiveId: Oid<RegionalObjective>,
		val type: RegionalObjectiveType,
		val start: Long,
		val points: MutableMap<Oid<Nation>, Int> = mutableMapOf()
	)

	val activeSieges = Collections.synchronizedList(mutableListOf<ActiveSiege>())

	private val siegeDurationMillis get() = TimeUnit.MINUTES.toMillis(45)
	const val MIN_SHIP_SIZE = 8000
	const val COOLDOWN_HOURS = 8L
	const val XENON_REWARD = 4
	const val MIN_DOMINION_TERRITORIES = 1

	// Points awarded per tick for presence
	private const val PRESENCE_POINTS = 2

	// Points awarded for kills/sinks
	private const val PLAYER_KILL_POINTS = 50
	private const val SMALL_SHIP_SINK_POINTS = 500
	private const val MEDIUM_SHIP_SINK_POINTS = 1000
	private const val LARGE_SHIP_SINK_POINTS = 2000

	@Synchronized
	private fun locked(block: () -> Unit) = block()

	private fun asyncLocked(block: () -> Unit) = Tasks.async {
		locked(block)
	}

	override fun onEnable() {
		Tasks.syncRepeat(20L, 20L) {
			updateSieges()
		}
		Tasks.syncRepeat(0L, 20L * 60L * 2L) {
			displayLeaderboards()
		}
	}

	private fun updateSieges() {
		for (siege in activeSieges.toList()) {
			val region = Regions.getAllOf<RegionRegionalObjective>()
				.firstOrNull { it.id == siege.objectiveId } ?: continue
			val world = Bukkit.getWorld(region.world) ?: continue
			val elapsed = System.currentTimeMillis() - siege.start

			if (elapsed >= siegeDurationMillis) {
				endSiege(siege)
				continue
			}

			val playersInZone = world.players.filter { player -> region.contains(player.location) && PilotedStarships.isPiloting(player) }

			for (player in playersInZone) {
				CombatTimer.refreshPvpTimer(player, CombatTimer.REASON_SIEGE_STATION)
				val remaining = TimeUnit.MILLISECONDS.toSeconds(siegeDurationMillis - elapsed) / 60.0
				player.informationAction("${String.format("%.2f", remaining)} minutes remaining in ${region.name} siege")

				if ((ActiveStarships.findByPilot(player)?.initialBlockCount ?: 0) >= MIN_SHIP_SIZE) {
					val nationId = PlayerCache[player].nationOid ?: continue
					siege.points.merge(nationId, PRESENCE_POINTS, Int::plus)
				}
			}
		}
	}

	private fun displayLeaderboards() {
		for (siege in activeSieges) {
			val region = Regions.getAllOf<RegionRegionalObjective>()
				.firstOrNull { it.id == siege.objectiveId } ?: continue
			val world = Bukkit.getWorld(region.world) ?: continue
			val sorted = siege.points.entries.sortedByDescending { it.value }

			for (player in world.players) {
				if (!region.contains(player.location)) continue
				player.sendMessage(text("=== ${region.name} Siege Scores ===", YELLOW))
				sorted.forEachIndexed { index, (nationId, points) ->
					val nationName = NationCache[nationId].name
					player.sendMessage(text("${index + 1}. $nationName: $points points", GREEN))
				}
			}
		}
	}

	@EventHandler
	fun onPlayerDeath(event: PlayerDeathEvent) {
		val killer = event.player.killer ?: return
		val killerNation = PlayerCache[killer].nationOid ?: return
		val victimNation = PlayerCache[event.player].nationOid

		// Don't award points for killing allies
		if (killerNation == victimNation) return
		if (victimNation != null && RelationCache[killerNation, victimNation].ordinal >= NationRelation.Level.ALLY.ordinal) return

		for (siege in activeSieges) {
			val region = Regions.getAllOf<RegionRegionalObjective>()
				.firstOrNull { it.id == siege.objectiveId } ?: continue
			if (!region.contains(event.player.location)) continue

			siege.points.merge(killerNation, PLAYER_KILL_POINTS, Int::plus)
			break
		}
	}

	@EventHandler
	fun onStarshipSunk(event: StarshipSunkEvent) {
		val controller = event.previousController as? PlayerController ?: return
		val victimNation = PlayerCache[controller.player].nationOid
		val damagers = event.starship.damagers
			.filter { it.key is PlayerDamager }
			.filter {
				val damagerNation = PlayerCache[(it.key as PlayerDamager).player].nationOid ?: return@filter false
				val victimNation = victimNation ?: return@filter true
				RelationCache[damagerNation, victimNation].ordinal < NationRelation.Level.ALLY.ordinal
			}
		if (damagers.isEmpty()) return

		for (siege in activeSieges) {
			val region = Regions.getAllOf<RegionRegionalObjective>()
				.firstOrNull { it.id == siege.objectiveId } ?: continue
			val world = Bukkit.getWorld(region.world) ?: continue

			if (!region.contains(event.starship.centerOfMass.toLocation(world))) continue

			val pointsGained = when {
				event.starship.initialBlockCount <= 4000 -> SMALL_SHIP_SINK_POINTS
				event.starship.initialBlockCount in 4001..12000 -> MEDIUM_SHIP_SINK_POINTS
				else -> LARGE_SHIP_SINK_POINTS
			}

			val damagePointsSum = damagers.values.sumOf { it.points.get() }
			for ((damager, damageData) in damagers) {
				val player = (damager as? PlayerDamager)?.player ?: continue
				val nationId = PlayerCache[player].nationOid ?: continue
				val percent = damageData.points.get().toDouble() / damagePointsSum.toDouble()
				siege.points.merge(nationId, (pointsGained * percent).toInt(), Int::plus)
			}
			break
		}
	}

	fun beginSiege(player: Player) = asyncLocked {
		val nation = PlayerCache[player].nationOid
			?: return@asyncLocked player.userError("You need to be in a nation to siege a regional objective.")

		val region = Regions.findFirstOf<RegionRegionalObjective>(player.location)
			?: return@asyncLocked player.userError("You must be within a regional objective's area to siege it.")

		// Check cooldown
		val lastSieged = RegionalObjective.findPropById(region.id, RegionalObjective::lastSieged)
		if (lastSieged != null) {
			val cooldownMillis = TimeUnit.HOURS.toMillis(COOLDOWN_HOURS)
			val elapsed = System.currentTimeMillis() - lastSieged.time
			if (elapsed < cooldownMillis) {
				val remaining = cooldownMillis - elapsed
				return@asyncLocked player.userError(
					"This objective was recently sieged! Time until next siege: ${getDurationBreakdownString(remaining)}"
				)
			}
		}

		if (isUnderSiege(region.id)) {
			return@asyncLocked player.userError("This objective is already under siege!")
		}

		if (!isInBigShip(player)) {
			return@asyncLocked player.userError("You cannot siege in a ship smaller than $MIN_SHIP_SIZE blocks.")
		}

		val dominionCount = Regions.getAllOf<RegionDominionTerritory>().count { it.nation == nation }
		if (dominionCount < MIN_DOMINION_TERRITORIES) {
			return@asyncLocked player.userError("Your nation needs to own at least $MIN_DOMINION_TERRITORIES dominion territories to siege a regional objective.")
		}

		activeSieges.add(ActiveSiege(region.id, region.type, System.currentTimeMillis()))

		val nationName = NationCache[nation].name
		val ownerName = region.nation?.let { NationCache[it].name } ?: "None"

		Notify.chatAndGlobal(MiniMessage.miniMessage().deserialize(
			"<gold>${player.name} of $nationName has initiated a siege on ${region.type.name.replace('_', ' ')} ${region.name}! (Current Owner: $ownerName) The siege will last 45 minutes!"
		))
	}

	private fun endSiege(siege: ActiveSiege) = asyncLocked {
		activeSieges.remove(siege)

		val region = Regions.getAllOf<RegionRegionalObjective>()
			.firstOrNull { it.id == siege.objectiveId }
		val regionName = region?.name ?: "Unknown"

		if (siege.points.isEmpty()) {
			Notify.chatAndGlobal(MiniMessage.miniMessage().deserialize(
				"<gold>The siege on ${siege.type.name.replace('_', ' ')} $regionName has ended with no participants!"
			))
			return@asyncLocked
		}

		val winnerNationId = siege.points.maxByOrNull { it.value }!!.key
		val winnerName = NationCache[winnerNationId].name

		RegionalObjective.setNation(siege.objectiveId, winnerNationId)
		RegionalObjective.setLastSieged(siege.objectiveId, Date(System.currentTimeMillis()))

		Notify.chatAndGlobal(MiniMessage.miniMessage().deserialize(
			"<gold>${siege.type.name.replace('_', ' ')} $regionName has been captured by $winnerName!"
		))

		// Type-specific rewards
		when (siege.type) {
			RegionalObjectiveType.GAS_DEPOT -> giveGasDepotRewards(siege.objectiveId, winnerNationId)
			RegionalObjectiveType.TAX_BEACON -> { /* No item rewards */ }
		}
	}

	private fun giveGasDepotRewards(objectiveId: Oid<RegionalObjective>, winnerNationId: Oid<Nation>) {
		val xenon = CustomItemKeys.GAS_CANISTER_XENON.getValue()
		val canister = xenon.createWithFill(xenon.maximumFill)
		val rewardMap = mutableMapOf(GlobalCompletions.toItemString(canister) to XENON_REWARD)
		RegionalObjectiveSiegeData.create(objectiveId, winnerNationId, rewardMap)
	}

	fun isUnderSiege(objectiveId: Oid<RegionalObjective>) = activeSieges.any { it.objectiveId == objectiveId }

	fun isInBigShip(player: Player): Boolean {
		val starship = ActiveStarships.findByPilot(player) ?: return false
		return starship.initialBlockCount >= MIN_SHIP_SIZE
	}

	fun getSiegeForObjective(objectiveId: Oid<RegionalObjective>) =
		activeSieges.firstOrNull { it.objectiveId == objectiveId }
}
