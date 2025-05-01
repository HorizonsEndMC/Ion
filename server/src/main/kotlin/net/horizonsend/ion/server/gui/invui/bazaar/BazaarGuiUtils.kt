package net.horizonsend.ion.server.gui.invui.bazaar

import com.mongodb.client.FindIterable
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers
import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.economy.city.CityNPCs
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.horizonsend.ion.server.features.nations.gui.playerClicker
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.space.body.planet.CachedPlanet.Companion.DEFAULT_ITEM_FACTORY
import net.horizonsend.ion.server.miscellaneous.utils.updateData
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
		AsyncItem({
			(Space.getPlanet(territory.world)?.planetIconFactory?.construct() ?: DEFAULT_ITEM_FACTORY.construct())
				.updateDisplayName(nameBuilder(city))
				.updateLore(loreBuilder(city))
		}) { event -> clickHandler.invoke(city, event.click, event.playerClicker) }
	}
}

fun getItemButtons(
	bson: Bson,
	sort: BazaarSort,
	ascending: Boolean,
	filter: (BazaarItem) -> Boolean = { true },
	nameBuilder: ((String, FindIterable<BazaarItem>) -> Component)? = null,
	loreBuilder: (String, FindIterable<BazaarItem>) -> List<Component> = { _, _ ->listOf() },
	clickHandler: (String, ClickType, Player) -> Unit,
): List<AbstractItem> {
	val items = BazaarItem.find(bson)
	sort.sort(items, ascending)

	return items
		.filter { TradeCities.isCity(Regions[it.cityTerritory]) && filter(it) }
		.map { it.itemString }
		.distinct()
		.mapTo(mutableListOf()) { itemString ->
			AsyncItem({
				with(fromItemString(itemString)) {
					nameBuilder?.let { updateDisplayName(it.invoke(itemString, items)) }
					updateLore(loreBuilder.invoke(itemString, items))
					// Clear attributes
					updateData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().build())
				}
			}) { event ->
				clickHandler.invoke(itemString, event.click, event.playerClicker)
			}
		}
}
