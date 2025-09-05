package net.horizonsend.ion.server.features.ai.convoys

import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import org.bukkit.Bukkit
import org.bukkit.Location

class TraceCityCaravanRoute(
	val cites: MutableList<TradeCityData> = TradeCities.getAll().shuffled().toMutableList(),
	val source: TradeCityData = cites.removeFirst()
) : ConvoyRoute {

	init {
		cites.addLast(source)//make the loop closed
	}

	override fun advanceDestination(): Location? {
		while (cites.isNotEmpty()) {
			val next = cites.removeFirst()

			if (next == source && cites.isNotEmpty()) {
				// Source reached in middle of loop: push to end and skip
				cites.add(next)
				continue
			}

			val territory = Regions.get<RegionTerritory>(next.territoryId)

			val location = Location(Bukkit.getWorld(territory.world), territory.centerX.toDouble(), 200.0, territory.centerZ.toDouble())

			return location
		}

		// If we ran out of destinations, disband
		return null
	}

	override fun getSourceLocation(): Location {
		val territory = Regions.get<RegionTerritory>(source.territoryId)

		val location = Location(Bukkit.getWorld(territory.world), territory.centerX.toDouble(), 200.0, territory.centerZ.toDouble())

		return location
	}
}
