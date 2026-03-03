package net.horizonsend.ion.server.features.misc

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.nations.FrontierNation
import net.horizonsend.ion.common.database.schema.nations.SiegeTerritory
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit
import org.bukkit.Location

class SiegeTerritoryCache : IonServerComponent() {
	val territories = mutableListOf<CachedSiegeTerritory>()

	override fun onEnable() {
		Tasks.sync {
			for (territory in SiegeTerritory.all()) {
				territories.add(
					CachedSiegeTerritory(
						territory.name,
						territory.nation,
						Location(Bukkit.getWorld(territory.world), territory.x.toDouble(), 192.0, territory.z.toDouble())
					)
				)
			}
		}
	}
}

data class CachedSiegeTerritory(val name: String, val nation: Oid<FrontierNation>?, val loc: Location)
