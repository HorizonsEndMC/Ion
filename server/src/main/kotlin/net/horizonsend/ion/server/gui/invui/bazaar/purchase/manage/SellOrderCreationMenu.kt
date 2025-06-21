package net.horizonsend.ion.server.gui.invui.bazaar.purchase.manage

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.utils.input.InputResult
import net.horizonsend.ion.common.utils.text.BAZAAR_LISTING_HEADER_ICON
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_BLUE
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.gui.GuiBorder
import net.horizonsend.ion.common.utils.text.gui.icons.GuiIcon
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.features.economy.city.CityNPCs.BAZAAR_CITY_TERRITORIES
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.REMOTE_WARINING
import net.horizonsend.ion.server.gui.invui.bazaar.getMenuTitleName
import net.horizonsend.ion.server.gui.invui.bazaar.stripAttributes
import net.horizonsend.ion.server.gui.invui.misc.util.input.TextInputMenu.Companion.openInputMenu
import net.horizonsend.ion.server.gui.invui.misc.util.input.TextInputMenu.Companion.openSearchMenu
import net.horizonsend.ion.server.gui.invui.misc.util.input.validator.RangeDoubleValidator
import net.horizonsend.ion.server.gui.invui.utils.buttons.FeedbackLike
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeInformationButton
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.litote.kmongo.eq
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.window.Window
import java.util.UUID

class SellOrderCreationMenu(viewer: Player) : InvUIWindowWrapper(viewer, async = true) {
	private var itemString = "DIRT"
	private var cityInfo: TradeCityData? = null
	private var price: Double = 0.0

	override fun buildWindow(): Window {

		val gui = Gui.normal()
			.setStructure(
				"I n n n n n n n n",
				"T t t t t t t t t",
				"P p p p p p p p p",
				". b b b . c c c .",
				". b b b . c c c .",
				". b b b . c c c .",
			)
			.addIngredient('x', parentOrBackButton())
			.addIngredient('i', infoButton)

			.addIngredient('I', iconStringButton)
			.addIngredient('n', emptyStringButton)

			.addIngredient('T', iconCityButton)
			.addIngredient('t', emptyCityButton)

			.addIngredient('P', iconPriceButton)
			.addIngredient('p', emptyPriceButton)

			.addIngredient('b', parentOrBackButton(GuiItem.EMPTY))
			.addIngredient('c', confirmButton)

			.build()
		return normalWindow(gui)
	}

	override fun buildTitle(): Component {
		val background = GuiText("")
			.addBackground()
			.addBorder(GuiBorder.regular(
				color = HE_DARK_BLUE,
				headerIcon = GuiBorder.HeaderIcon(BAZAAR_LISTING_HEADER_ICON, 48, HE_DARK_BLUE),
				leftText = text("Creating"),
				rightText = text("Sell Order")
			))
			.setGuiIconOverlay(
				". l c c c c c c r",
				". l c c c c c c r",
				". l c c c c c c r",
				". . . . . . . . .",
				". . x . . . g . .",
				". . . . . . . . .",
			)
			.addIcon('l', GuiIcon.textInputBoxLeft())
			.addIcon('c', GuiIcon.textInputBoxCenter())
			.addIcon('r', GuiIcon.textInputBoxRight())
			.addIcon('g', GuiIcon.checkmarkIcon(NamedTextColor.GREEN, true))
			.addIcon('x', GuiIcon.crossIcon(NamedTextColor.RED, true))
			.build()

		val info = GuiText("")
			.add(getMenuTitleName(text(itemString, WHITE)), line = 0, verticalShift = 4, horizontalShift = 22)
			.add(getMenuTitleName(text(cityInfo?.displayName ?: "No City Selected", WHITE)), line = 2, verticalShift = 4, horizontalShift = 22)
			.add(getMenuTitleName(price.toCreditComponent()), line = 4, verticalShift = 4, horizontalShift = 22)
			.build()

		return ofChildren(background, info)
	}

	private val infoButton = makeInformationButton(text("Information"),
		text(""),
		text(""),
		text(""),
	)

	private val iconStringButton = tracked { uuid -> AsyncItem(
		{ fromItemString(itemString).stripAttributes().updateLore(getStringButtonLore()).updateDisplayName(text("Click to change item string")) },
		{ inputNewItemString(uuid) }
	) }
	private val emptyStringButton = tracked { uuid ->  ItemProvider { GuiItem.EMPTY.makeItem(text("Click to change item string")).updateLore(getStringButtonLore()) }.makeGuiButton { _, _ -> inputNewItemString(uuid) } }

	private fun inputNewItemString(uuid: UUID) {
		viewer.openSearchMenu(
			entries = Bazaars.strings,
			searchTermProvider = { itemString: String -> listOf(itemString) },
			prompt = text("Search for Bazaar Strings"),
			backButtonHandler = { openGui() },
			componentTransformer = { itemString: String -> text(itemString) },
			itemTransformer = { itemString: String -> fromItemString(itemString) },
			handler = { _: ClickType, newString: String ->
				itemString = newString
				this@SellOrderCreationMenu.openGui()
				refreshButtons(uuid)
				iconStringButton.update()
			}
		)
	}

	private fun getStringButtonLore() = listOf(template(text("Current value: {0}", HE_MEDIUM_GRAY), itemString))

	private val iconCityButton = tracked { uuid -> AsyncItem(
		resultProvider = {
			val cityInfo = this.cityInfo ?: return@AsyncItem GuiItem.CITY.makeItem(text("No city selected"))

			cityInfo.planetIcon
				.updateDisplayName(getMenuTitleName(text(cityInfo.displayName)))
				.updateLore(getCityButtonLore())
		},
		handleClick = { inputNewCity(uuid) }
	) }

	private val emptyCityButton = tracked { uuid -> ItemProvider { GuiItem.EMPTY.makeItem(text("Click to change trade city")).updateLore(getCityButtonLore()) }.makeGuiButton { _, _ -> inputNewCity(uuid) } }

	private fun inputNewCity(uuid: UUID) {
		val cities: List<TradeCityData> = TradeCities.getAll().filter { BAZAAR_CITY_TERRITORIES.contains(it.territoryId) }

		viewer.openSearchMenu(
			entries = cities,
			searchTermProvider = { cityData: TradeCityData -> listOf(cityData.displayName, cityData.type.name) },
			prompt = text("Search for Trade Cities"),
			backButtonHandler = { openGui() },
			componentTransformer = { city: TradeCityData -> text(city.displayName) },
			itemTransformer = { city: TradeCityData ->
				val planet = city.planetIcon.updateDisplayName(text(city.displayName))

				val listingCount = BazaarItem.count(BazaarItem::cityTerritory eq city.territoryId)
				val territoryRegion = Regions.get<RegionTerritory>(city.territoryId)

				val lore = listOf(
					ofChildren(
						text("Located at ", HE_MEDIUM_GRAY), text(territoryRegion.name, AQUA),
						text(" on ", HE_MEDIUM_GRAY), text(territoryRegion.world, AQUA), text(".", GRAY)
					),
					template(text("{0} item listing${if (listingCount != 1L) "s" else ""}.", HE_MEDIUM_GRAY), listingCount)
				)

				planet.updateLore(if (!territoryRegion.contains(viewer.location)) lore.plus(REMOTE_WARINING) else lore)
			},
			handler = { _: ClickType, newCity: TradeCityData ->
				cityInfo = newCity
				this@SellOrderCreationMenu.openGui()
				refreshButtons(uuid)
				iconCityButton.update()
			}
		)
	}

	private fun getCityButtonLore(): List<Component> {
		val cityInfo = this.cityInfo ?: return listOf()
		val territoryRegion = Regions.get<RegionTerritory>(cityInfo.territoryId)

		return listOf(ofChildren(
			text("Located at ", HE_MEDIUM_GRAY), text(territoryRegion.name, AQUA),
			text(" on ", HE_MEDIUM_GRAY), text(territoryRegion.world, AQUA), text(".", GRAY)
		))
	}

	private val iconPriceButton = AsyncItem(
		resultProvider = { GuiItem.CITY.makeItem(text("Click to change listing price")).updateLore(getPriceButtonLore()) },
		handleClick = { inputNewPrice() }
	)

	private val emptyPriceButton = ItemProvider { GuiItem.EMPTY.makeItem(text("Click to change listing price")).updateLore(getPriceButtonLore()) }.makeGuiButton { _, _ -> inputNewPrice() }

	private fun inputNewPrice() {
		viewer.openInputMenu(
			prompt = text("Enter new value"),
			description = text("Between 0.01 & 10,000,000"),
			backButtonHandler = { this.openGui() },
			componentTransformer = { double: Double -> double.toCreditComponent() },
			inputValidator = RangeDoubleValidator(0.001..10_000_000.0),
			handler = { _, validator ->
				price = validator.result
				this@SellOrderCreationMenu.openGui()
			}
		)
	}

	private fun getPriceButtonLore(): List<Component> {
		val cityInfo = this.cityInfo ?: return listOf()
		val territoryRegion = Regions.get<RegionTerritory>(cityInfo.territoryId)

		return listOf(ofChildren(
			text("Located at ", HE_MEDIUM_GRAY), text(territoryRegion.name, AQUA),
			text(" on ", HE_MEDIUM_GRAY), text(territoryRegion.world, AQUA), text(".", GRAY)
		))
	}

	private val confirmButton = FeedbackLike.withHandler(GuiItem.EMPTY.makeItem(text("Click to Confirm"))) { _, _ ->
		val city = cityInfo
		if (city == null) {
			updateWith(InputResult.FailureReason(listOf(text("You must specify a city!", RED))))

			return@withHandler
		}

		val result = Bazaars.createListing(viewer, Regions[city.territoryId], itemString, price)
		updateWith(result)

		if (result.isSuccess()) {
			cityInfo = null
			itemString = "DIRT"
			price = 0.0

			iconCityButton.update()
			iconStringButton.update()

			refreshAll()
			this@SellOrderCreationMenu.openGui()
		}
	}
}
