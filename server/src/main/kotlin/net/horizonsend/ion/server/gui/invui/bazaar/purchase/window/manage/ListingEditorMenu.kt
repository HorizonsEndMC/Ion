package net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.manage

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.utils.text.BACKGROUND_EXTENDER
import net.horizonsend.ion.common.utils.text.BAZAAR_LISTING_HEADER_ICON
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_BLUE
import net.horizonsend.ion.common.utils.text.gui.GuiBorder
import net.horizonsend.ion.common.utils.text.gui.icons.GuiIcon
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars.cityName
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.DEPOSIT_COLOR
import net.horizonsend.ion.server.gui.invui.bazaar.WITHDRAW_COLOR
import net.horizonsend.ion.server.gui.invui.bazaar.getMenuTitleName
import net.horizonsend.ion.server.gui.invui.bazaar.stripAttributes
import net.horizonsend.ion.server.gui.invui.input.TextInputMenu.Companion.anvilInputText
import net.horizonsend.ion.server.gui.invui.input.validator.RangeDoubleValidator
import net.horizonsend.ion.server.gui.invui.input.validator.RangeIntegerValidator
import net.horizonsend.ion.server.gui.invui.misc.ConfirmationMenu
import net.horizonsend.ion.server.gui.invui.utils.asItemProvider
import net.horizonsend.ion.server.gui.invui.utils.buttons.FeedbackLike
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeInformationButton
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.window.Window
import java.util.function.Supplier

class ListingEditorMenu(viewer: Player, private val listing: BazaarItem) : InvUIWindowWrapper(viewer, true) {
	override fun buildWindow(): Window {
		val gui = Gui.normal()
			.setStructure(
				"c . . . . . . . i",
				"e . . . . . . . .",
				". . . . p p p p p",
				"d d d w w w r r r",
				"d D d w W w r r r",
				"d d d w w w r r r"
			)
			.addIngredient('e', fromItemString(listing.itemString).stripAttributes())
			.addIngredient('c', parentOrBackButton())
			.addIngredient('p', setPriceButton)
			.addIngredient('d', depositStockButton)
			.addIngredient('w', withdrawStockButton)
			.addIngredient('D', depositStockButtonVisible)
			.addIngredient('W', withdrawStockButtonVisible)
			.addIngredient('r', deleteListingButton)
			.addIngredient('i', informationButton)
			.build()

		return normalWindow(gui)
	}

	override fun buildTitle(): Component {
		val text =  GuiText("")
			.addBorder(GuiBorder.regular(
				color = HE_DARK_BLUE,
				headerIcon = GuiBorder.HeaderIcon(BAZAAR_LISTING_HEADER_ICON, 48, HE_DARK_BLUE),
				leftText = text("Modifying"),
				rightText = text("Sell Order")
			))
			.addBackground()
			.setGuiIconOverlay(
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . l c c c r",
				". . . . . . . . .",
				". d . . w . . D .",
				". . . . . . . . ."
			)
			.addIcon('l', GuiIcon.textInputBoxLeft())
			.addIcon('c', GuiIcon.textInputBoxCenter())
			.addIcon('r', GuiIcon.textInputBoxRight())
			.addIcon('d', GuiIcon.depositIcon(DEPOSIT_COLOR, true))
			.addIcon('w', GuiIcon.withdrawIcon(WITHDRAW_COLOR, true))
			.addIcon('D', GuiIcon.trashCanIcon(NamedTextColor.RED, true))

		text.add(getMenuTitleName(listing), line = 2, horizontalShift = 18, verticalShift = -2)
		text.add(template(text("@ {0}"), paramColor = null, useQuotesAroundObjects = false, cityName(Regions[listing.cityTerritory])), line = 3, horizontalShift = 18, verticalShift = -2)

		text.add(text("Set Price"), line = 4, verticalShift = 4)

		val displayedInfo = GuiText("")
			.add(getMenuTitleName(listing.price.toCreditComponent()), line = 4, verticalShift = 4, horizontalShift = 76)
			.build()

		return ofChildren(text.build(), displayedInfo)
	}

	private val setPriceButton = tracked { FeedbackLike.withHandler(
		GuiItem.EMPTY.makeItem(text("Set Listing Price")).asItemProvider(),
		{ listOf() },
		{ _, _ -> setPrice() }
	) }

	private val depositStockButton = FeedbackLike.withHandler(
		GuiItem.EMPTY.makeItem(text("Deposit Stock")).asItemProvider(),
		{ listOf() }
	) { _, _ -> deposit() }
	private val depositStockButtonVisible = FeedbackLike.withHandler(
		GuiItem.DOWN.makeItem(text("Deposit Stock")).asItemProvider(),
		{ listOf() }
	) { _, _ -> deposit() }

	private val withdrawStockButton = FeedbackLike.withHandler(
		GuiItem.EMPTY.makeItem(text("Withdraw Stock")).asItemProvider(),
		{ listOf() }
	) { _, _ -> withdraw() }
	private val withdrawStockButtonVisible = FeedbackLike.withHandler(
		GuiItem.UP.makeItem(text("Withdraw Stock")).asItemProvider(),
		{ listOf() }
	) { _, _ -> withdraw() }

	private val deleteListingButton = FeedbackLike.withHandler(
		GuiItem.EMPTY.makeItem(text("Delete Listing")).asItemProvider(),
		{ listOf() }
	) { _, _ -> delete() }

	private fun setPrice() {
		val region = Regions.get<RegionTerritory>(listing.cityTerritory)

		viewer.anvilInputText(
			prompt = Supplier { text("Enter new price.") },
			description = Supplier { text("Value above 0.01.") },
			backButtonHandler = { openGui() },
			inputValidator = RangeDoubleValidator(0.01..Double.MAX_VALUE)
		) { _, validatorResult ->
			val changePriceResult = Bazaars.setListingPrice(viewer, region, listing.itemString, validatorResult.second.result)

			if (changePriceResult.isSuccess()) {
				// Update it so the menu isn't outdated. Regular safeguards will still be in place
				listing.price = validatorResult.second.result
				openGui()
				changePriceResult.sendReason(viewer)
				setPriceButton.updateWith(changePriceResult)
			}
			else {
				openGui()
				withdrawStockButton.updateWith(changePriceResult)
				withdrawStockButtonVisible.updateWith(changePriceResult)
				changePriceResult.sendReason(viewer)
				setPriceButton.updateWith(changePriceResult)
			}
		}
	}

	private fun deposit() {
		val region: RegionTerritory = Regions[listing.cityTerritory]
		val presenceCheckResult = Bazaars.checkTerritoryPresence(viewer, region)
		if (!presenceCheckResult.isSuccess()) {
			depositStockButton.updateWith(presenceCheckResult)
			depositStockButtonVisible.updateWith(presenceCheckResult)
			return
		}

		val depositResult = Bazaars.depositListingStock(viewer, viewer.inventory, region, listing.itemString, Int.MAX_VALUE)
		depositStockButton.updateWith(depositResult)
		depositStockButtonVisible.updateWith(depositResult)
	}

	private fun withdraw() {
		val region: RegionTerritory = Regions[listing.cityTerritory]
		val presenceCheckResult = Bazaars.checkTerritoryPresence(viewer, region)
		if (!presenceCheckResult.isSuccess()) {
			withdrawStockButton.updateWith(presenceCheckResult)
			withdrawStockButtonVisible.updateWith(presenceCheckResult)
			return
		}

		viewer.anvilInputText(
			prompt = Supplier { text("Enter amount to withdraw.") },
			description = Supplier { text("Value between 1 and ${listing.stock}.") },
			backButtonHandler = { openGui() },
			inputValidator = RangeIntegerValidator(1..listing.stock)
		) { _, validatorResult ->
			val withdrawResult = Bazaars.withdrawListingBalance(viewer, region, listing.itemString, validatorResult.second.result)

			if (withdrawResult.isSuccess()) {
				// Update it so the menu isn't outdated. Regular safeguards will still be in place
				listing.stock -= validatorResult.second.result
				openGui()
				withdrawResult.sendReason(viewer)
			}
			else {
				openGui()
				withdrawStockButton.updateWith(withdrawResult)
				withdrawStockButtonVisible.updateWith(withdrawResult)
				withdrawResult.sendReason(viewer)
			}
		}
	}

	private fun delete() {
		val region = Regions.get<RegionTerritory>(listing.cityTerritory)

		val title = GuiText("")
			.addBackground(GuiText.GuiBackground(BACKGROUND_EXTENDER, verticalShift = -17))
			.add(text("Confirm Listing Removal:"), line = -3, verticalShift = -2)
			.add(template(text("{0} at {1}"), paramColor = null, listing.itemString, cityName(region)), line = -2, verticalShift = -1)

		ConfirmationMenu.promptConfirmation(this, title) {
			Tasks.async {
				val result = Bazaars.removeListing(viewer, region, listing.itemString)

				if (result.isSuccess()) {
					parentWindow?.openGui()
					result.sendReason(viewer)
				}
				else {
					openGui()
					deleteListingButton.updateWith(result)
					result.sendReason(viewer)
				}
			}
		}
	}

	private val informationButton = makeInformationButton(title = text("Info"),
		text("Here, you may update an item that you have listed for sale."),
		text("In order for the item to be visible to other players, you must deposit"),
		text("some stock into it; that can be done through this menu."),
		empty(),
		text("You may also edit the price of the item, withdraw some stock, or remove the listing."),
	)
}
