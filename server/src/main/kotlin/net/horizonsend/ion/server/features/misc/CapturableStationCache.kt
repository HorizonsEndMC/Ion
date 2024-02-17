package net.horizonsend.ion.server.features.misc

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.nations.CapturableStation
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit
import org.bukkit.Location

object CapturableStationCache : IonServerComponent() {
    val stations = mutableListOf<CachedCapturableStation>()

    // Trashy siege station cache, perhaps find a better implementation later on
    override fun onEnable() {
        Tasks.sync {
            for (station in CapturableStation.Companion.all()) {
                stations.add(
                    CachedCapturableStation(
                        station.name,
                        station.nation,
                        Location(Bukkit.getWorld(station.world), station.x.toDouble(), 192.0, station.z.toDouble())
                    )
                )
            }
        }
    }
}

data class CachedCapturableStation(val name: String, val nation: Oid<Nation>?, val loc: Location)