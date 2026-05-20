package net.horizonsend.ion.server.features.nations.sieges

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.schema.nations.GasDepot
import net.horizonsend.ion.common.database.schema.nations.GasDepotSiegeData
import net.horizonsend.ion.common.database.schema.nations.Nation
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
import net.horizonsend.ion.server.features.nations.region.types.RegionGasDepot
import net.horizonsend.ion.server.features.player.CombatTimer
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.Date
import java.util.concurrent.TimeUnit

object GasDepotSieges : IonServerComponent() {

	data class ActiveSiege(
		val depotId: Oid<GasDepot>,
		val start: Long,
		val points: MutableMap<Oid<Nation>, Int> = mutableMapOf()
	)

	val activeSieges = mutableListOf<ActiveSiege>()

	private val siegeDurationMillis get() = TimeUnit.MINUTES.toMillis(45)
	private const val MIN_SHIP_SIZE = 8000
	private const val COOLDOWN_HOURS = 24L
	private const val XENON_REWARD = 10

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
			val depot: RegionGasDepot = Regions[siege.depotId]
			val world = Bukkit.getWorld(depot.world) ?: continue
			val elapsed = currentTimeMillis() - siege.start

			if (elapsed >= siegeDurationMillis) {
				endSiege(siege)
				continue
			}

			val playersInZone = world.players.filter { player ->
				depot.contains(player.location) &&
					PilotedStarships.isPiloting(player) &&
					(ActiveStarships.findByPilot(player)?.initialBlockCount ?: 0) >= MIN_SHIP_SIZE
			}

			// Award points and refresh combat tags
			for (player in playersInZone) {
				val nationId = PlayerCache[player].nationOid ?: continue
				siege.points.merge(nationId, 2, Int::plus)
				CombatTimer.refreshPvpTimer(player, CombatTimer.REASON_IN_KOTH)

				val remaining = TimeUnit.MILLISECONDS.toSeconds(siegeDurationMillis - elapsed) / 60.0
				player.informationAction("${String.format("%.2f", remaining)} minutes remaining in Gas Depot siege")
			}
		}
	}

	private fun displayLeaderboards() {
		for (siege in activeSieges) {
			val depot: RegionGasDepot = Regions[siege.depotId]
			val world = Bukkit.getWorld(depot.world) ?: continue

			val sorted = siege.points.entries.sortedByDescending { it.value }

			for (player in world.players) {
				if (!depot.contains(player.location)) continue
				player.sendMessage(text("=== Gas Depot ${depot.name} Siege Scores ===", YELLOW))
				sorted.forEachIndexed { index, (nationId, points) ->
					val nationName = NationCache[nationId].name
					player.sendMessage(text("${index + 1}. $nationName: $points points", GREEN))
				}
			}
		}
	}

	override fun onDisable() {
		activeSieges.toList().forEach { endSiege(it) }
	}

	@Synchronized
	private fun locked(block: () -> Unit) = block()

	private fun asyncLocked(block: () -> Unit) = Tasks.async { locked(block) }

	fun beginSiege(player: Player) = asyncLocked {
		val nation = PlayerCache[player].nationOid
			?: return@asyncLocked player.userError("You need to be in a nation to siege a gas depot.")

		val depot = Regions.findFirstOf<RegionGasDepot>(player.location)
			?: return@asyncLocked player.userError("You must be within a gas depot's area to siege it.")

		// Check cooldown
		val lastSieged = GasDepot.findPropById(depot.id, GasDepot::lastSieged)
		if (lastSieged != null) {
			val cooldownMillis = TimeUnit.HOURS.toMillis(COOLDOWN_HOURS)
			val elapsed = currentTimeMillis() - lastSieged.time
			if (elapsed < cooldownMillis) {
				val remaining = cooldownMillis - elapsed
				return@asyncLocked player.userError(
					"This gas depot was recently sieged! Time until next siege: ${getDurationBreakdownString(remaining)}"
				)
			}
		}

		if (isUnderSiege(depot.id)) {
			return@asyncLocked player.userError("This gas depot is already under siege!")
		}

		if (!isInBigShip(player)) {
			return@asyncLocked player.userError("You cannot siege in a ship smaller than $MIN_SHIP_SIZE blocks.")
		}

		activeSieges.add(ActiveSiege(depot.id, currentTimeMillis()))

		val nationName = NationCache[nation].name
		val ownerName = depot.nation?.let { NationCache[it].name } ?: "None"

		Notify.chatAndGlobal(MiniMessage.miniMessage().deserialize(
			"<gold>${player.name} of $nationName has initiated a siege on Gas Depot ${depot.name}! (Current Owner: $ownerName) The siege will last 45 minutes!"
		))
	}

	private fun endSiege(siege: ActiveSiege) = asyncLocked {
		activeSieges.remove(siege)

		val depot: RegionGasDepot = Regions[siege.depotId]

		if (siege.points.isEmpty()) {
			Notify.chatAndGlobal(MiniMessage.miniMessage().deserialize(
				"<gold>The siege on Gas Depot ${depot.name} has ended with no participants!"
			))
			return@asyncLocked
		}

		val winnerNationId = siege.points.maxByOrNull { it.value }!!.key
		val winnerName = NationCache[winnerNationId].name

		GasDepot.setNation(siege.depotId, winnerNationId)
		GasDepot.setLastSieged(siege.depotId, Date(currentTimeMillis()))

		Notify.chatAndGlobal(MiniMessage.miniMessage().deserialize(
			"<gold>Gas Depot ${depot.name} has been captured by $winnerName!"
		))

		val xenon = CustomItemKeys.GAS_CANISTER_XENON.getValue()
		val canister = xenon.createWithFill(xenon.maximumFill)
		val rewardMap = mutableMapOf(GlobalCompletions.toItemString(canister) to XENON_REWARD)
		GasDepotSiegeData.create(siege.depotId, winnerNationId, rewardMap)
	}

	fun isUnderSiege(depotId: Oid<GasDepot>) = activeSieges.any { it.depotId == depotId }

	private fun isInBigShip(player: Player): Boolean {
		val starship = ActiveStarships.findByPilot(player) ?: return false
		return starship.initialBlockCount >= MIN_SHIP_SIZE
	}

	private fun currentTimeMillis() = System.currentTimeMillis()
}
