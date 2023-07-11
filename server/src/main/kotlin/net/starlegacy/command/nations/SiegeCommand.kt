package net.starlegacy.command.nations

import co.aikar.commands.annotation.CommandAlias
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.starlegacy.command.SLCommand
import net.starlegacy.feature.nations.StationSieges
import org.bukkit.entity.Player

internal object SiegeCommand : SLCommand() {
	@Suppress("unused")
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
		StationSieges.beginSiege(sender)
	}
}
