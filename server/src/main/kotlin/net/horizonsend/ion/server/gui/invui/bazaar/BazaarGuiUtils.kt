package net.horizonsend.ion.server.gui.invui.bazaar

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers
import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.Colors
import net.horizonsend.ion.common.utils.text.withShadowColor
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.economy.city.CityNPCs
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.horizonsend.ion.server.features.nations.gui.playerClicker
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.horizonsend.ion.server.miscellaneous.utils.updateData
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor
import org.bson.conversions.Bson
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.item.impl.AbstractItem

val REMOTE_WARINING = bracketed(text("REMOTE", TextColor.color(Colors.ALERT)))

fun ItemStack.stripAttributes() = updateData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().build())

const val BAZAAR_SHADOW_COLOR = "#252525FF"

fun getMenuTitleName(component: Component) = component.withShadowColor(BAZAAR_SHADOW_COLOR)
fun getMenuTitleName(itemStack: ItemStack) = getMenuTitleName(itemStack.displayNameComponent)
fun getMenuTitleName(bazaarItem: BazaarItem) = getMenuTitleName(fromItemString(bazaarItem.itemString))

fun getCityButtons(
	cityFilter: (TradeCityData) -> Boolean = { true },
	nameBuilder: (TradeCityData) -> Component,
	loreBuilder: (TradeCityData) -> List<Component>,
	clickHandler: (TradeCityData, ClickType, Player) -> Unit,
): List<AbstractItem> {
	val cities: List<TradeCityData> = CityNPCs.BAZAAR_CITY_TERRITORIES
		.map { Regions.get<RegionTerritory>(it) }
		.mapNotNull(TradeCities::getIfCity)
		.filter(cityFilter)

	return cities.map { city -> AsyncItem(
		resultProvider = { city.planetIcon.updateDisplayName(nameBuilder(city)).updateLore(loreBuilder(city)) },
		handleClick = { event -> clickHandler.invoke(city, event.click, event.playerClicker) }
	) }
}

fun getItemButtons(
	bson: Bson,
	sort: BazaarSort,
	filter: (BazaarItem) -> Boolean = { true },
	nameBuilder: ((String, List<BazaarItem>) -> Component)? = null,
	loreBuilder: (String, List<BazaarItem>) -> List<Component> = { _, _ ->listOf() },
	clickHandler: (String, List<BazaarItem>, ClickType, Player) -> Unit,
): List<AbstractItem> {
	val items = BazaarItem.find(bson)

	return items
		.filter { TradeCities.isCity(Regions[it.cityTerritory]) && filter(it) }
		.groupBy { it.itemString }
		.entries
		.mapTo(mutableListOf()) { entry ->
			val (itemString, grouped) = entry

			entry to AsyncItem(
				resultProvider = { with(fromItemString(itemString)) {
					nameBuilder?.let { updateDisplayName(it.invoke(itemString, grouped)) }
					updateLore(loreBuilder.invoke(itemString, grouped))
					// Clear attributes
					stripAttributes()
				} },
				handleClick = { event -> clickHandler.invoke(itemString, grouped, event.click, event.playerClicker) }
			)
		}
		.apply { sort.sort(this) }
		.map { it.second }
}

fun InvUIWindowWrapper.getBazaarSettingsButton() = GuiItem.GEAR.makeItem(text("View settings")).makeGuiButton { _, player -> BazaarGUIs.openBazaarSettings(player, this) }
