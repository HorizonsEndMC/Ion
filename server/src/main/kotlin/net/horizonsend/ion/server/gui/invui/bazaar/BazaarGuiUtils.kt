package net.horizonsend.ion.server.gui.invui.bazaar

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.economy.city.CityNPCs
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.space.body.planet.CachedPlanet.Companion.DEFAULT_ITEM_FACTORY
import net.horizonsend.ion.server.gui.invui.utils.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import org.bson.conversions.Bson
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import xyz.xenondevs.invui.item.impl.AbstractItem

fun getCityButtons(
	cityFilter: (TradeCityData) -> Boolean = { true },
	nameBuilder: (TradeCityData) -> Component = { city -> Component.text("${city.displayName} on ${Regions.get<RegionTerritory>(city.territoryId).world}") },
	loreBuilder: (TradeCityData) -> List<Component> = { listOf() },
	clickHandler: (TradeCityData, ClickType, Player) -> Unit,
): List<AbstractItem> {
	val cities: List<TradeCityData> = CityNPCs.BAZAAR_CITY_TERRITORIES
		.map { Regions.get<RegionTerritory>(it) }
//		.filter { Sector.getSector(it.world) == sector }
		.mapNotNull(TradeCities::getIfCity)
		.filter(cityFilter)

	return cities.map { city ->
		val territoryId = city.territoryId
		val territory: RegionTerritory = Regions[territoryId]

		// attempt to get the planet icon, just use a detonator if unavailable
		(Space.getPlanet(territory.world)?.planetIconFactory?.construct() ?: DEFAULT_ITEM_FACTORY.construct())
			.updateDisplayName(nameBuilder(city))
			.updateLore(loreBuilder(city))
			.makeGuiButton { clickType, player -> clickHandler.invoke(city, clickType, player) }
	}
}

fun getItemButtons(
	filter: Bson,
	sort: BazaarSort,
	ascending: Boolean,
	nameBuilder: (BazaarItem) -> Component = { item -> Component.text(item.itemString) },
	loreBuilder: (BazaarItem) -> List<Component> = { listOf() },
	clickHandler: (BazaarItem, ClickType, Player) -> Unit,
): List<AbstractItem> {
	val items = BazaarItem.find(filter)
	sort.sort(items, ascending)

	return items.mapTo(mutableListOf()) { bazaarItem ->
		fromItemString(bazaarItem.itemString)
			.updateDisplayName(nameBuilder.invoke(bazaarItem))
			.updateLore(loreBuilder.invoke(bazaarItem))
			.makeGuiButton { clickType, player -> clickHandler.invoke(bazaarItem, clickType, player)  }
	}
}
