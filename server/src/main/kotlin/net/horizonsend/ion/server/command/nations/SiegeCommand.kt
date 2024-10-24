package net.horizonsend.ion.server.command.nations

import co.aikar.commands.annotation.CommandAlias
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.schema.nations.NationRole
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.nations.sieges.StationSieges
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.entity.Player

internal object SiegeCommand : SLCommand() {
	@CommandAlias("siege")
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

		StationSieges.beginSiege(sender)
	}
}
