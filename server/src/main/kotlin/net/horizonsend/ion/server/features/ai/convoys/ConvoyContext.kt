package net.horizonsend.ion.server.features.ai.convoys

import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import org.bukkit.Bukkit
import org.bukkit.Location

// ConvoyContext.kt
sealed interface ConvoyContext {
	val worldName: String
}

data class CityContext(val city: TradeCityData) : ConvoyContext {

	val territory = Regions.get<RegionTerritory>(city.territoryId)

	val location = Location(Bukkit.getWorld(territory.world), territory.centerX.toDouble(), 200.0, territory.centerZ.toDouble())

	override val worldName get() = location.world.name
}

data class LocationContext(val source: Location) : ConvoyContext {
	override val worldName get() = source.world.name
}

fun TradeCityData.toContext() = CityContext(this)
