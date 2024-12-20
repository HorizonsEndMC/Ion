package net.horizonsend.ion.server.command.nations.admin

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.CapturableStation
import net.horizonsend.ion.common.database.schema.nations.CapturableStationSiege
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.schema.nations.Territory
import net.horizonsend.ion.common.database.schema.nations.spacestation.NationSpaceStation
import net.horizonsend.ion.common.database.schema.nations.spacestation.PlayerSpaceStation
import net.horizonsend.ion.common.database.schema.nations.spacestation.SettlementSpaceStation
import net.horizonsend.ion.common.database.schema.nations.spacestation.SpaceStationCompanion
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.extensions.hint
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.common.utils.miscellaneous.toCreditsString
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.chat.Discord
import net.horizonsend.ion.server.features.nations.NATIONS_BALANCE
import net.horizonsend.ion.server.features.nations.NationsBalancing
import net.horizonsend.ion.server.features.nations.NationsMap
import net.horizonsend.ion.server.features.nations.NationsMasterTasks
import net.horizonsend.ion.server.features.nations.TerritoryImporter
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionSpaceStation
import net.horizonsend.ion.server.features.nations.sieges.SolarSiege
import net.horizonsend.ion.server.features.nations.sieges.SolarSieges
import net.horizonsend.ion.server.features.nations.utils.isActive
import net.horizonsend.ion.server.features.nations.utils.isInactive
import net.horizonsend.ion.server.features.space.spacestations.CachedSpaceStation
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.kyori.adventure.text.Component
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.litote.kmongo.and
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.eq
import org.litote.kmongo.gt
import org.litote.kmongo.ne
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOne
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@CommandAlias("nadmin|nationsadmin")
@CommandPermission("nations.admin")
internal object NationAdminCommand : net.horizonsend.ion.server.command.SLCommand() {
	@Subcommand("rebalance")
    fun onRebalance(sender: CommandSender) {
		NationsBalancing.reload()
		sender.success("Reloaded config")
	}

	@Subcommand("refresh map")
    fun onRefreshMap(sender: CommandSender) {
		NationsMap.reloadDynmap()
		sender.success("Refreshed map")
	}

	@Subcommand("runtask money")
    fun onRunTaskIncome(sender: CommandSender) {
		NationsMasterTasks.executeMoneyTasks()
		sender.success("Executed income task")
	}

	@Subcommand("runtask purge")
    fun onRunTaskPurge(sender: CommandSender) = asyncCommand(sender) {
		NationsMasterTasks.checkPurges()
		sender.success("Executed purge task")
	}

	@Subcommand("player set settlement")
    fun onPlayerSetSettlement(sender: CommandSender, player: String, settlement: String) = asyncCommand(sender) {
		val playerId = resolveOfflinePlayer(player).slPlayerId
		val settlementId = resolveSettlement(settlement)

		failIf(SLPlayer.isSettlementLeader(playerId)) { "$player is the leader of a settlement, leader can't leave" }

		if (SLPlayer.matches(playerId, SLPlayer::settlement ne null)) {
			SLPlayer.leaveSettlement(playerId)
		}

		SLPlayer.joinSettlement(playerId, settlementId)

		sender.success("Put $player in $settlement")
	}

	private fun percentAndTotal(dividend: Double, divisor: Double) =
		"${(dividend / divisor * 100).roundToInt()}% ($dividend)"

	@Subcommand("player stats")
    fun onPlayerStats(sender: CommandSender) = asyncCommand(sender) {
		sender.hint("Pulling from db...")
		val allPlayers = SLPlayer.all()

		val total = allPlayers.size.toDouble()
		sender.hint("Analyzing $total players...")

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

		sender.information("Players in settlements: " + percentAndTotal(playersInSettlements, total))
		sender.information("Players in nations: " + percentAndTotal(playersInNations, total))
		sender.information("Active Players: " + percentAndTotal(activePlayers, total))
		sender.information("Semi-Active Players: " + percentAndTotal(semiActivePlayers, total))
		sender.information("Inactive Players: " + percentAndTotal(inactivePlayers, total))
	}

	@Subcommand("settlement set leader")
    fun onSettlementSetLeader(sender: CommandSender, settlement: String, player: String) = asyncCommand(sender) {
		val settlementId = resolveSettlement(settlement)
		val playerId = resolveOfflinePlayer(player).slPlayerId
		requireIsMemberOf(playerId, settlementId)
		Settlement.setLeader(settlementId, playerId)
		sender.success("Changed leader of ${getSettlementName(settlementId)} to ${getPlayerName(playerId)}")
	}

	@Subcommand("settlement purge")
    fun onSettlementPurge(sender: CommandSender, settlement: String, sendMessage: Boolean) = asyncCommand(sender) {
		val settlementId = resolveSettlement(settlement)
		NationsMasterTasks.purgeSettlement(settlementId, sendMessage)
		sender.success("Purged ${getSettlementName(settlementId)}")
	}

	@Subcommand("settlement set balance")
    fun onSettlementSetBalance(sender: CommandSender, settlement: String, balance: Int) = asyncCommand(sender) {
		val settlementId = resolveSettlement(settlement)
		Settlement.updateById(settlementId, setValue(Settlement::balance, balance))
		sender.success("Set balance of $settlement to ${balance.toCreditsString()}")
	}

	@Subcommand("nation set balance")
    fun onNationSetBalance(sender: CommandSender, nation: String, balance: Int) = asyncCommand(sender) {
		val nationId = resolveNation(nation)
		Nation.updateById(nationId, setValue(Nation::balance, balance))
		sender.success("Set balance of $nation to ${balance.toCreditsString()}")
	}

	@Subcommand("nation set capital")
    fun onNationSetCapital(sender: CommandSender, nation: String, capital: String) = asyncCommand(sender) {
		val nationId = resolveNation(nation)
		val newCapital = resolveSettlement(capital)

		failIf(SettlementCache[newCapital].nation != nationId) { "Settlement $capital must be in nation $nation" }

		Nation.setCapital(nationId, newCapital)
	}

	@CommandPermission("nations.admin.movestation")
	@Subcommand("spacestation set location")
    fun onStationSetLocaiton(sender: CommandSender, station: CachedSpaceStation<*, *, *>, world: World, x: Int, z: Int) = asyncCommand(sender) {
		station.setLocation(x, z, world.name)

		sender.success("Set position of ${station.name} to $x, $z")
	}

	@CommandPermission("nations.admin.movestation")
	@Subcommand("spacestation set owner")
    fun onStationSetOwner(sender: CommandSender, station: CachedSpaceStation<*, *, *>, newOwner: String) = asyncCommand(sender) {
		when (station.companion) {
			is PlayerSpaceStation.Companion -> {
				val player = resolveOfflinePlayer(newOwner).slPlayerId
				transferPersonal(station, player)
			}

			is SettlementSpaceStation.Companion -> {
				val settlement = resolveSettlement(newOwner)
				transferSettlement(station, settlement)
			}

			is NationSpaceStation.Companion -> {
				val nation = resolveNation(newOwner)
				transferNation(station, nation)
			}

			else -> throw NotImplementedError()
		}

		sender.success("Transferred ${station.ownershipType} station ${station.name} to ${station.ownershipType} $newOwner")
	}

	private fun transferPersonal(station: CachedSpaceStation<*, *, *>, newOwner: SLPlayerId) {
		station.companion.col.deleteOneById(station.databaseId)

		val id = PlayerSpaceStation.create(
			newOwner,
			station.name,
			station.world,
			station.x,
			station.z,
			station.radius,
			SpaceStationCompanion.TrustLevel.MANUAL
		)

		PlayerSpaceStation.updateById(id, setValue(PlayerSpaceStation::trustedPlayers, station.trustedPlayers))
		PlayerSpaceStation.updateById(id, setValue(PlayerSpaceStation::trustedSettlements, station.trustedSettlements))
		PlayerSpaceStation.updateById(id, setValue(PlayerSpaceStation::trustedNations, station.trustedNations))
		PlayerSpaceStation.updateById(id, setValue(PlayerSpaceStation::trustLevel, station.trustLevel))
	}

	private fun transferSettlement(station: CachedSpaceStation<*, *, *>, newOwner: Oid<Settlement>) {
		station.companion.col.deleteOneById(station.databaseId)

		val id = SettlementSpaceStation.create(
			newOwner,
			station.name,
			station.world,
			station.x,
			station.z,
			station.radius,
			SpaceStationCompanion.TrustLevel.MANUAL
		)

		SettlementSpaceStation.updateById(id, setValue(SettlementSpaceStation::trustedPlayers, station.trustedPlayers))
		SettlementSpaceStation.updateById(id, setValue(SettlementSpaceStation::trustedSettlements, station.trustedSettlements))
		SettlementSpaceStation.updateById(id, setValue(SettlementSpaceStation::trustedNations, station.trustedNations))
		SettlementSpaceStation.updateById(id, setValue(SettlementSpaceStation::trustLevel, station.trustLevel))
	}

	private fun transferNation(station: CachedSpaceStation<*, *, *>, newOwner: Oid<Nation>) {
		station.companion.col.deleteOneById(station.databaseId)

		val id = NationSpaceStation.create(
			newOwner,
			station.name,
			station.world,
			station.x,
			station.z,
			station.radius,
			SpaceStationCompanion.TrustLevel.MANUAL
		)

		NationSpaceStation.updateById(id, setValue(NationSpaceStation::trustedPlayers, station.trustedPlayers))
		NationSpaceStation.updateById(id, setValue(NationSpaceStation::trustedSettlements, station.trustedSettlements))
		NationSpaceStation.updateById(id, setValue(NationSpaceStation::trustedNations, station.trustedNations))
		NationSpaceStation.updateById(id, setValue(NationSpaceStation::trustLevel, station.trustLevel))
	}

	@CommandPermission("nations.admin.movestation")
	@Subcommand("spacestation set radius")
    fun onStationSetRadius(sender: CommandSender, station: CachedSpaceStation<*, *, *>, radius: Int) =
		asyncCommand(sender) {

			station.changeRadius(radius)

			sender.success("Set radius of ${station.name} to $radius")
		}


	@Subcommand("spacestation reload")
    fun onStationReload(sender: CommandSender) {
		Regions.getAllOf<RegionSpaceStation<*, *>>().forEach(NationsMap::updateSpaceStation)
		sender.success("Reloaded space stations")
	}

	@Subcommand("station set quarter")
    fun onStationSetQuarter(sender: CommandSender, stationName: String, quarter: Int) = asyncCommand(sender) {
		failIf(quarter !in 1..4) { "Quarter must be within [1, 4]" }
		val station = CapturableStation.findOne(CapturableStation::name eq stationName) ?: fail { "Station $stationName not found" }
		station.siegeTimeFrame = quarter
		CapturableStation.col.updateOne(station)

		sender.success("Set quarter of $station to $quarter")
	}

	@Subcommand("station clearsieges")
    fun onStationClearSieges(sender: CommandSender, nation: String) = asyncCommand(sender) {
		val nationId = resolveNation(nation)
		val daysPerSiege = NATIONS_BALANCE.capturableStation.daysPerSiege
		val duration = TimeUnit.DAYS.toMillis(daysPerSiege.toLong())
		val date = Date(System.currentTimeMillis() - duration)
		val deleted = CapturableStationSiege.col
			.deleteMany(and(CapturableStationSiege::time gt date, CapturableStationSiege::nation eq nationId))
			.deletedCount

		sender.success("Deleted $deleted siege(s)")
	}

	@Subcommand("territory import")
    fun onTerritoryImport(sender: CommandSender) {
		TerritoryImporter.importOldTerritories(sender)
	}

	@Subcommand("territory setOwner")
	@CommandCompletion("@nations")
	fun onTerritoryOwn(sender: Player, newOwner: String, @Optional confirm: String?) {
		val currentTerritory = requireTerritoryIn(sender)
		val nation = resolveNation(newOwner)

		failIf(currentTerritory.settlement != null) {
			"This territory is claimed by a settlement!"
		}

		if (confirm != "confirm") {
			val ownerName = currentTerritory.nation?.let { getNationName(it) }
			sender.userError("You are about to change the owner of this territory from $ownerName to ${getNationName(nation)}. You must confirm.")
		}

		Territory.setNation(currentTerritory.id, null)
		Territory.setNation(currentTerritory.id, nation)
	}

	@Subcommand("solarSiege start")
	fun onSiegeStart(sender: CommandSender, siege: SolarSiege) {
		Notify.chatAndGlobal(template(Component.text("{0} has begun.", HEColorScheme.HE_MEDIUM_GRAY), siege.formatName()))
		Discord.sendEmbed(
			ConfigurationFiles.discordSettings().eventsChannel, Embed(
				title = "Siege Start",
				description = "${siege.formatName().plainText()} has begun. It will end <t:${TimeUnit.MILLISECONDS.toSeconds(siege.getSiegeEnd())}:R>."
			)
		)

		SolarSieges.setActive(siege)
		siege.scheduleEnd()
	}

	@Subcommand("solarSiege end")
	fun onSiegeEnd(sender: CommandSender, siege: SolarSiege) {
		siege.endSiege(true)
	}

	@Subcommand("solarSiege win")
	fun onSiegeWin(sender: CommandSender, siege: SolarSiege) {
		siege.succeed()
		siege.removeActive()
	}

	@Subcommand("solarSiege lose")
	fun onSiegeLose(sender: CommandSender, siege: SolarSiege) {
		siege.fail(true)
		siege.removeActive()
	}
}
