package net.horizonsend.ion.server.features.misc

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.nations.CapturableStation
import net.horizonsend.ion.common.database.schema.nations.FrontierNation
import net.horizonsend.ion.common.database.schema.nations.KothStation
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit
import org.bukkit.Location

object KothStationCache : IonServerComponent() {
    val stations = mutableListOf<CachedKothStation>()

    // Trashy siege station cache, perhaps find a better implementation later on
    override fun onEnable() {
        Tasks.sync {
            for (station in KothStation.Companion.all()) {
                stations.add(
                    CachedKothStation(
                        station.name,
						station.nation,
                        Location(Bukkit.getWorld(station.world), station.x.toDouble(), 192.0, station.z.toDouble())
                    )
                )
            }
        }
    }
}

data class CachedKothStation(val name: String, val nation: Oid<FrontierNation>?, val loc: Location)
