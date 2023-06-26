package net.starlegacy.command.nations.admin

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import net.horizonsend.ion.server.database.schema.nations.Nation
import net.starlegacy.command.SLCommand
import net.horizonsend.ion.server.database.schema.misc.SLPlayer
import net.horizonsend.ion.server.database.schema.nations.CapturableStation
import net.horizonsend.ion.server.database.schema.nations.CapturableStationSiege
import net.horizonsend.ion.server.database.schema.nations.Settlement
import net.horizonsend.ion.server.database.schema.nations.SpaceStation
import net.horizonsend.ion.server.database.schema.nations.Territory
import net.horizonsend.ion.server.database.schema.nations.moonsieges.ForwardOperatingBase
import net.horizonsend.ion.server.database.schema.nations.moonsieges.SiegeTerritory
import net.horizonsend.ion.server.database.slPlayerId
import net.horizonsend.ion.server.features.landsieges.MoonSieges
import net.starlegacy.feature.nations.NATIONS_BALANCE
import net.starlegacy.feature.nations.NationsBalancing
import net.starlegacy.feature.nations.NationsMap
import net.starlegacy.feature.nations.NationsMasterTasks
import net.starlegacy.feature.nations.TerritoryImporter
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.feature.nations.utils.isActive
import net.starlegacy.feature.nations.utils.isInactive
import net.starlegacy.util.msg
import net.starlegacy.util.toCreditsString
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.gt
import org.litote.kmongo.ne
import org.litote.kmongo.set
import org.litote.kmongo.setTo
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOne

@CommandAlias("nadmin|nationsadmin")
@CommandPermission("nations.admin")
internal object NationAdminCommand : SLCommand() {
	@Subcommand("rebalance")
	@Suppress("unused")
	fun onRebalance(sender: CommandSender) {
		NationsBalancing.reload()
		sender msg "&aRebalanced"
	}

	@Subcommand("refresh map")
	@Suppress("unused")
	fun onRefreshMap(sender: CommandSender) {
		NationsMap.reloadDynmap()
		sender msg "Refreshed map"
	}

	@Subcommand("runtask money")
	@Suppress("unused")
	fun onRunTaskIncome(sender: CommandSender) {
		NationsMasterTasks.executeMoneyTasks()
		sender msg "Executed income task"
	}

	@Subcommand("runtask purge")
	@Suppress("unused")
	fun onRunTaskPurge(sender: CommandSender) = asyncCommand(sender) {
		NationsMasterTasks.checkPurges()
		sender msg "Executed purge task"
	}

	@Subcommand("player set settlement")
	@Suppress("unused")
	fun onPlayerSetSettlement(sender: CommandSender, player: String, settlement: String) = asyncCommand(sender) {
		val playerId = resolveOfflinePlayer(player).slPlayerId
		val settlementId = resolveSettlement(settlement)

		failIf(SLPlayer.isSettlementLeader(playerId)) { "$player is the leader of a settlement, leader can't leave" }

		if (SLPlayer.matches(playerId, SLPlayer::settlement ne null)) {
			SLPlayer.leaveSettlement(playerId)
		}

		SLPlayer.joinSettlement(playerId, settlementId)

		sender msg "&aPut $player in $settlement"
	}

	private fun percentAndTotal(dividend: Double, divisor: Double) =
		"${(dividend / divisor * 100).roundToInt()}% ($dividend)"

	@Subcommand("player stats")
	@Suppress("unused")
	fun onPlayerStats(sender: CommandSender) = asyncCommand(sender) {
		sender msg "Pulling from db..."
		val allPlayers = SLPlayer.all()
		val total = allPlayers.size.toDouble()
		sender msg "Analyzing $total players..."
		var playersInSettlements = 0.0
		var playersInNations = 0.0
		var activePlayers = 0.0
		var semiActivePlayers = 0.0
		var inactivePlayers = 0.0

		allPlayers.forEach {
			if (it.settlement != null) playersInSettlements++
			if (it.nation != null) playersInNations++

			when {
				isActive(it.lastSeen) -> activePlayers++
				isInactive(it.lastSeen) -> inactivePlayers++
				else -> semiActivePlayers++
			}
		}

		sender msg "&6Players in settlements: &b" + percentAndTotal(playersInSettlements, total)
		sender msg "&6Players in nations: &5" + percentAndTotal(playersInNations, total)
		sender msg "&6Active Players: &2" + percentAndTotal(activePlayers, total)
		sender msg "&6Semi-Active Players: &7" + percentAndTotal(semiActivePlayers, total)
		sender msg "&6Inactive Players: &c" + percentAndTotal(inactivePlayers, total)
	}

	@Subcommand("settlement set leader")
	@Suppress("unused")
	fun onSettlementSetLeader(sender: CommandSender, settlement: String, player: String) = asyncCommand(sender) {
		val settlementId = resolveSettlement(settlement)
		val playerId = resolveOfflinePlayer(player).slPlayerId
		requireIsMemberOf(playerId, settlementId)
		Settlement.setLeader(settlementId, playerId)
		sender msg "Changed leader of ${getSettlementName(settlementId)} to ${getPlayerName(playerId)}"
	}

	@Subcommand("settlement purge")
	@Suppress("unused")
	fun onSettlementPurge(sender: CommandSender, settlement: String, sendMessage: Boolean) = asyncCommand(sender) {
		val settlementId = resolveSettlement(settlement)
		NationsMasterTasks.purgeSettlement(settlementId, sendMessage)
		sender msg "Purged ${getSettlementName(settlementId)}"
	}

	@Subcommand("settlement set balance")
	@Suppress("unused")
	fun onSettlementSetBalance(sender: CommandSender, settlement: String, balance: Int) = asyncCommand(sender) {
		val settlementId = resolveSettlement(settlement)
		Settlement.updateById(settlementId, setValue(Settlement::balance, balance))
		sender msg "Set balance of $settlement to ${balance.toCreditsString()}"
	}

	@Subcommand("nation set balance")
	@Suppress("unused")
	fun onNationSetBalance(sender: CommandSender, nation: String, balance: Int) = asyncCommand(sender) {
		val nationId = resolveNation(nation)
		Nation.updateById(nationId, setValue(Nation::balance, balance))
		sender msg "Set balance of $nation to ${balance.toCreditsString()}"
	}

	@CommandPermission("nations.admin.movestation")
	@Subcommand("nation station set location")
	@Suppress("unused")
	fun onStationSetLocaiton(sender: CommandSender, station: String, world: World, x: Int, z: Int) =
		asyncCommand(sender) {
			val spaceStation = SpaceStation.findOne(SpaceStation::name eq station)
				?: fail { "Station $station not found" }
			SpaceStation.updateById(
				spaceStation._id,
				set(SpaceStation::world setTo world.name, SpaceStation::x setTo x, SpaceStation::z setTo z)
			)
			NationsMap.updateSpaceStation(Regions[spaceStation._id])
			sender msg "Set position of $station to $x, $z"
		}

	@Subcommand("station set quarter")
	@Suppress("unused")
	fun onStationSetQuarter(sender: CommandSender, station: String, quarter: Int) = asyncCommand(sender) {
		failIf(quarter !in 1..4) { "Quarter must be within [1, 4]" }
		val station = CapturableStation.findOne(CapturableStation::name eq station)
			?: fail { "Station $station not found" }
		station.siegeTimeFrame = quarter
		CapturableStation.col.updateOne(station)
		sender msg "Set quarter of $station to $quarter"
	}

	@Subcommand("station clearsieges")
	@Suppress("unused")
	fun onStationClearSieges(sender: CommandSender, nation: String) = asyncCommand(sender) {
		val nationId = resolveNation(nation)
		val daysPerSiege = NATIONS_BALANCE.capturableStation.daysPerSiege
		val duration = TimeUnit.DAYS.toMillis(daysPerSiege.toLong())
		val date = Date(System.currentTimeMillis() - duration)
		val deleted = CapturableStationSiege.col
			.deleteMany(and(CapturableStationSiege::time gt date, CapturableStationSiege::nation eq nationId))
			.deletedCount
		sender msg "Deleted $deleted siege(s)"
	}

	@Subcommand("territoryimport")
	@Suppress("unused")
	fun onTerritoryImport(sender: CommandSender) {
		TerritoryImporter.importOldTerritories(sender, Territory.Companion)
	}

	@Subcommand("fobimport")
	@Suppress("unused")
	fun onForwardOperatingBaseImport(sender: CommandSender) {
		TerritoryImporter.importOldTerritories(sender, ForwardOperatingBase.Companion)
	}

	@Subcommand("siegeterritoryimport")
	@Suppress("unused")
	fun onSiegeTerritoryImport(sender: CommandSender) {
		TerritoryImporter.importOldTerritories(sender, SiegeTerritory.Companion)
	}

	@Subcommand("clearbeacons")
	fun clearBeacons() {
		MoonSieges.clearBeacons()
		Regions
	}
}
