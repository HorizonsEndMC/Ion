package net.horizonsend.ion.server.gui.invui.bazaar.orders

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.utils.InputResult
import net.horizonsend.ion.common.utils.text.BACKGROUND_EXTENDER
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.icons.GuiIcon
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars.checkValidPrice
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars.checkValidString
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars.checkValidTerritory
import net.horizonsend.ion.server.features.economy.city.CityNPCs.BAZAAR_CITY_TERRITORIES
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.bazaar.REMOTE_WARINING
import net.horizonsend.ion.server.gui.invui.bazaar.getBazaarSettingsButton
import net.horizonsend.ion.server.gui.invui.bazaar.getMenuTitleName
import net.horizonsend.ion.server.gui.invui.bazaar.stripAttributes
import net.horizonsend.ion.server.gui.invui.input.TextInputMenu.Companion.anvilInputText
import net.horizonsend.ion.server.gui.invui.input.TextInputMenu.Companion.searchEntires
import net.horizonsend.ion.server.gui.invui.input.validator.RangeDoubleValidator
import net.horizonsend.ion.server.gui.invui.input.validator.RangeIntegerValidator
import net.horizonsend.ion.server.gui.invui.misc.ConfirmationMenu.Companion.promptConfirmation
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.horizonsend.ion.server.miscellaneous.utils.hasEnoughMoney
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
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

class CreateBuyOrderMenu(viewer: Player) : InvUIWindowWrapper(viewer, true) {
	private var itemString = "DIRT"
	private var cityInfo: TradeCityData? = null
	private var count: Int = 1
	private var unitPrice: Double = 1.0
	private val orderPrice: Double get() = count * unitPrice

	override fun buildWindow(): Window {
		val gui = Gui
			.normal()
			.setStructure(
				"x . . . . . . s i",
				"I n n n n n n n n",
				"T t t t t t t t t",
				". . . . c c c c .",
				". . . . u u u u .",
				". . . . p p p p C",
			)
			.addIngredient('x', parentOrBackButton())
			.addIngredient('s', settingsButton)
			.addIngredient('i', infoButton)

			.addIngredient('I', iconStringButton)
			.addIngredient('n', emptyStringButton)

			.addIngredient('T', iconCityButton)
			.addIngredient('t', emptyCityButton)

			.addIngredient('c', emptyCountButton)
			.addIngredient('u', emptyUnitPriceButton)
			.addIngredient('p', emptyOrderPriceButton)

			.addIngredient('C', confirmButton)

			.build()

		return normalWindow(gui)
	}

	override fun buildTitle(): Component {
		val background = GuiText("Create Bazaar Order")
			.addBackground()
			.setGuiIconOverlay(
				". . . . . . . . .",
				". l c c c c c c r",
				". l c c c c c c r",
				". . . . l c c r .",
				". . . . l c c r .",
				". . . . l c c r .",
			)
			.addIcon('l', GuiIcon.textInputBoxLeft())
			.addIcon('c', GuiIcon.textInputBoxCenter())
			.addIcon('r', GuiIcon.textInputBoxRight())
			.add(text("Count"), line = 6, verticalShift = 4)
			.add(text("Unit Price"), line = 8, verticalShift = 4)
			.add(text("Order Price"), line = 10, verticalShift = 4)
			.build()

		val info = GuiText("")
			.add(getMenuTitleName(text(itemString, WHITE)), line = 2, verticalShift = 4, horizontalShift = 22)
			.add(getMenuTitleName(text(cityInfo?.displayName ?: "No City Selected", WHITE)), line = 4, verticalShift = 4, horizontalShift = 22)
			.add(getMenuTitleName(text(count, WHITE)), line = 6, verticalShift = 4, horizontalShift = 76)
			.add(getMenuTitleName(text(unitPrice, WHITE)), line = 8, verticalShift = 4, horizontalShift = 76)
			.add(getMenuTitleName(text(orderPrice, WHITE)), line = 10, verticalShift = 4, horizontalShift = 76)
			.build()

		return ofChildren(background, info)
	}

	private val settingsButton = getBazaarSettingsButton()
	private val infoButton = GuiItem.INFO.makeItem(text("Info"))

	private val iconStringButton = tracked { uuid -> AsyncItem(
		{ fromItemString(itemString).stripAttributes().updateLore(getStringButtonLore()).updateDisplayName(text("Click to change item string")) },
		{ inputNewItemString(uuid) }
	) }
	private val emptyStringButton = tracked { uuid ->  ItemProvider { GuiItem.EMPTY.makeItem(text("Click to change item string")).updateLore(getStringButtonLore()) }.makeGuiButton { _, _ -> inputNewItemString(uuid) } }

	private fun inputNewItemString(uuid: UUID) {
		viewer.searchEntires(
			entries = Bazaars.strings,
			searchTermProvider = { itemString: String -> listOf(itemString) },
			prompt = text("Search for Bazaar Strings"),
			backButtonHandler = { openGui() },
			componentTransformer = { itemString: String -> text(itemString) },
			itemTransformer = { itemString: String -> fromItemString(itemString) },
			handler = { _: ClickType, newString: String ->
				itemString = newString
				openGui()
				refreshButtons(uuid)
				iconStringButton.update()
			}
		)
	}

	private fun getStringButtonLore() = listOf(template(text("Current value: {0}", HE_MEDIUM_GRAY), itemString))

	private val iconCityButton = tracked { uuid -> AsyncItem(
		{
			val cityInfo = this.cityInfo ?: return@AsyncItem GuiItem.CITY.makeItem(text("No city selected"))

			cityInfo.planetIcon
				.updateDisplayName(getMenuTitleName(text(cityInfo.displayName)))
				.updateLore(getCityButtonLore())
		},
		{ inputNewCity(uuid) }
	) }
	private val emptyCityButton = tracked { uuid -> ItemProvider { GuiItem.EMPTY.makeItem(text("Click to change item string")).updateLore(getCityButtonLore()) }.makeGuiButton { _, _ -> inputNewCity(uuid) } }

	private fun inputNewCity(uuid: UUID) {
		val cities: List<TradeCityData> = TradeCities.getAll().filter { BAZAAR_CITY_TERRITORIES.contains(it.territoryId) }

		viewer.searchEntires(
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
				openGui()
				refreshButtons(uuid)
				iconCityButton.update()
			}
		)
	}

	private fun getCityButtonLore(): List<Component> {
		val cityInfo = this.cityInfo ?: return listOf()
		val territoryRegion = Regions.get<RegionTerritory>(cityInfo.territoryId)

		return listOf(
			ofChildren(
				text("Located at ", HE_MEDIUM_GRAY), text(territoryRegion.name, AQUA),
				text(" on ", HE_MEDIUM_GRAY), text(territoryRegion.world, AQUA), text(".", GRAY)
			)
		)
	}

	private val emptyCountButton = ItemProvider {
		GuiItem.EMPTY.makeItem(text("Set Order Count"))
			.updateLore(listOf(
				template(text("Current value: {0}", HE_MEDIUM_GRAY), count),
				text("The order price will also be updated", HE_MEDIUM_GRAY)
			))
	}.makeGuiButton { _, _ ->
		viewer.anvilInputText(
			prompt = text("Select Item Count"),
			description = text("Must be greater than 0"),
			backButtonHandler = { openGui() },
			inputValidator = RangeIntegerValidator(1..Int.MAX_VALUE),
			handler = { _, (_, validatorResult) ->
				count = validatorResult.result
				openGui()
				refreshButtons()
			}
		)
	}

	private val emptyUnitPriceButton = ItemProvider {
		GuiItem.EMPTY.makeItem(text("Set Order Unit Price"))
			.updateLore(listOf(
				template(text("Current value: {0}", HE_MEDIUM_GRAY), unitPrice.toCreditComponent()),
				text("The order price will also be updated", HE_MEDIUM_GRAY)
			))
	}.makeGuiButton { _, _ ->
		viewer.anvilInputText(
			prompt = text("Select Unit Price"),
			description = text("Must be greater than 0"),
			backButtonHandler = { openGui() },
			inputValidator = RangeDoubleValidator(0.01..Double.MAX_VALUE),
			handler = { _, (_, validatorResult) ->
				unitPrice = validatorResult.result
				openGui()
				refreshButtons()
			}
		)
	}
	private val emptyOrderPriceButton = ItemProvider {
		GuiItem.EMPTY.makeItem(text("Set Order Total Price"))
			.updateLore(listOf(
				template(text("Current value: {0}", HE_MEDIUM_GRAY), orderPrice.toCreditComponent()),
				text("The unit price will also be updated", HE_MEDIUM_GRAY)
			))
	}.makeGuiButton { _, _ ->
		viewer.anvilInputText(
			prompt = text("Select Total Order Price"),
			description = text("Must be greater than 0"),
			backButtonHandler = { openGui() },
			inputValidator = RangeDoubleValidator(1.0..Double.MAX_VALUE),
			handler = { _, (_, validatorResult) ->
				unitPrice = validatorResult.result / count
				openGui()
				refreshButtons()
			}
		)
	}

	private val confirmButton = tracked { _ ->
		ItemProvider {
			val result = validateOrder()

			if (result.isSuccess()) {
				val baseItem = fromItemString(itemString)

				GuiItem.CHECKMARK.makeItem(text("Create Order")).updateLore(listOf(
					text("Click to confirm creation of a bazaar order for:", HE_MEDIUM_GRAY),
					template(text("{0} of {1}", HE_MEDIUM_GRAY), count, baseItem.displayNameComponent),
					template(text("from {0}", HE_MEDIUM_GRAY), cityInfo!!.displayName),
					template(text("for {0} at {1} each.", HE_MEDIUM_GRAY), orderPrice.toCreditComponent(), unitPrice.toCreditComponent()),
				))
			}
			else {
				GuiItem.CANCEL.makeItem(text("Invalid Order!")).updateLore(result.getReason() ?: listOf())
			}
		}.makeGuiButton { _, player ->
			val territory = Regions.get<RegionTerritory>(cityInfo?.territoryId ?: return@makeGuiButton)

			if (validateOrder().isSuccess()) {
				val itemname = getMenuTitleName(fromItemString(itemString))
				val cityName = TradeCities.getIfCity(territory)!!.displayName

				val title = GuiText("")
					.addBackground(GuiText.GuiBackground(BACKGROUND_EXTENDER, verticalShift = -17))
					.add(text("Confirm Buy Order"), line = -3, verticalShift = -2)
					.add(template(text("{0} of {1}"), paramColor = null, count, itemname), line = -2, verticalShift = -2)
					.add(template(text("from {0} for {1}"), paramColor = null, cityName, getMenuTitleName(orderPrice.toCreditComponent())), line = -1, verticalShift = -2)
					.build()

				promptConfirmation(this, title) {
					Tasks.async {
						Bazaars.createOrder(player, territory, itemString, count, unitPrice)

						// Make a new, blank window
						BazaarGUIs.openBuyOrderCreationMenu(player, parentWindow)
					}
				}
			}
		}
	}

	private fun validateOrder(): InputResult {
		// Validate city
		val cityInfo = cityInfo ?: return InputResult.FailureReason(listOf(text("You must enter a trade city!.", RED)))
		val territory = Regions.get<RegionTerritory>(cityInfo.territoryId)
		val territoryResult = checkValidTerritory(territory)
		if (!territoryResult.isSuccess()) return territoryResult

		// Validate item string
		val stringResult = checkValidString(itemString)
		if (!stringResult.isSuccess()) return stringResult

		// Validate order quantity & price
		if (count !in 1 ..< 1_000_000) return InputResult.FailureReason(listOf(template(text("Invalid Order Quantity {0}! Amount must be greater than 0, and less than 1,000,000.", RED), count)))
		val priceResult = checkValidPrice(unitPrice)
		if (!priceResult.isSuccess()) return priceResult

		if (!viewer.hasEnoughMoney(unitPrice * count)) {
			return InputResult.FailureReason(listOf(text("You don't have enough money to create that order!", RED)))
		}

		return InputResult.InputSuccess
	}
}
