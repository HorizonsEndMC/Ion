package net.starlegacy.command.nations

import net.starlegacy.cache.nations.NationCache
import net.starlegacy.command.SLCommand
import net.starlegacy.feature.nations.StationSieges
import co.aikar.commands.annotation.CommandAlias
import net.starlegacy.util.msg
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
            val x = it.x
            val z = it.z
            "[$nationName's $stationName ($x, $z)]"
        }
        sender msg "Current Stations: $currentStationNames"
    }

    private fun ensurePilotingStarship(sender: Player) {
        getStarshipPiloting(sender)
    }

    private fun beginSiege(sender: Player) {
        StationSieges.beginSiege(sender)
    }
}
