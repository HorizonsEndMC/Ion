package net.horizonsend.ion.server.command.nations

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.schema.nations.NationRole
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionCapturableStation
import net.horizonsend.ion.server.features.nations.region.types.RegionSolarSiegeZone
import net.horizonsend.ion.server.features.nations.sieges.SolarSiege
import net.horizonsend.ion.server.features.nations.sieges.SolarSieges
import net.horizonsend.ion.server.features.nations.sieges.StationSieges
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.entity.Player

@CommandAlias("siege")
object SiegeCommand : SLCommand() {
	@Default
	fun execute(sender: Player) {
		tellPlayerCurrentlySiegableStations(sender)
		ensurePilotingStarship(sender)
		beginSiege(sender)
	}

	private fun tellPlayerCurrentlySiegableStations(sender: Player) {
		val currentStationNames = StationSieges.getStationsNow().joinToString {
			val nationName = it.nation?.let(NationCache::get)?.name
			val stationName = it.name
			val world = it.world
			val x = it.x
			val z = it.z
			"<dark_gray>[<aqua>$nationName<gray>'s <aqua>$stationName <gray>in <yellow>$world <gray>(<yellow>$x<gray>, <yellow>$z<gray>)<dark_gray>]"
		}

		sender.sendRichMessage("<gray>Current Stations: $currentStationNames")
	}

	private fun ensurePilotingStarship(sender: Player) {
		getStarshipPiloting(sender)
	}

	private fun beginSiege(sender: Player) {
		if (!NationRole.hasPermission(sender.slPlayerId, NationRole.Permission.START_NATION_SIEGE)) {
			sender.userError("Your nation prevents you from starting station sieges!")
			return
		}

		if (Regions.findFirstOf<RegionCapturableStation>(sender.location) != null) StationSieges.beginSiege(sender)
		if (Regions.findFirstOf<RegionSolarSiegeZone>(sender.location) != null) SolarSieges.initSiege(sender)
	}

	override fun onEnable(manager: PaperCommandManager) {
		manager.commandCompletions.registerAsyncCompletion("solarSieges") {
			println(SolarSieges.getAllPreparingSieges())
			println(SolarSieges.getAllActiveSieges())
			return@registerAsyncCompletion SolarSieges.getAllSieges().map { it.region.name.replace(' ', '_') }
		}
		manager.commandContexts.registerContext(SolarSiege::class.java) { c ->
			val name = c.popFirstArg()
			println(SolarSieges.getAllPreparingSieges())
			println(SolarSieges.getAllActiveSieges())
			SolarSieges.getAllSieges().firstOrNull { it.region.name.replace(' ', '_').equals(name, ignoreCase = true) } ?: throw InvalidCommandArgument("$name not found!")
		}
		manager.commandCompletions.setDefaultCompletion("solarSieges", SolarSiege::class.java)
	}

	@Subcommand("abandon")
	@CommandCompletion("@solarSieges")
	fun onAbandon(sender: Player, siege: SolarSiege) {
		if (siege.isAttacker(sender.slPlayerId)) return SolarSieges.attackerAbandonSiege(sender, siege)
		if (siege.isDefender(sender.slPlayerId)) return SolarSieges.defenderAbandonSiege(sender, siege)
		fail { "You aren't a participant of this siege!" }
	}
}
