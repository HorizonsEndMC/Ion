package net.horizonsend.ion.server.gui.invui.bazaar.terminal

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.gui.icons.GuiIcon
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.multiblock.type.economy.BazaarTerminalMultiblock
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.getMenuTitleName
import net.horizonsend.ion.server.gui.invui.utils.buttons.FeedbackLike
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.window.Window

class BazaarTerminalMainMenu(
	viewer: Player,
	private val terminalMultiblockEntity: BazaarTerminalMultiblock.BazaarTerminalMultiblockEntity
) : InvUIWindowWrapper(viewer, async = true) {
	override fun buildWindow(): Window {
		val gui = Gui.normal()
			.setStructure(
				". . . . . . . . . ",
				". . . b b b d d d ",
				". . . b b b d d d ",
				". . . . . . . . . ",
				". . . r r r f f f ",
				". . . r r r f f f "
			)
			.addIngredient('b', buyButton)
			.addIngredient('d', depositButton)

			.addIngredient('r', recieveButton)
			.addIngredient('f', fulfillButton)
			.build()

		return normalWindow(gui)
	}

	override fun buildTitle(): Component {
		val text = GuiText("Bazaar Terminal Main Menu")
			.addBackground()
			.setGuiIconOverlay(
				". . . . . . . . . ",
				". . . . p . . d . ",
				". . . . . . . . . ",
				". . . . . . . . . ",
				". . . . r . . f . ",
				". . . . . . . . . "
			)

		if (terminalMultiblockEntity.isWithdrawAvailable())
			text.addIcon('p', GuiIcon.bazaarSellOrder(TextColor.fromHexString("#67C2D4")!!))
		else text.addIcon('p', GuiIcon.bazaarSellOrder(NamedTextColor.GRAY))

		if (terminalMultiblockEntity.isDepositAvailable())
			text.addIcon('d', GuiIcon.bazaarSellOrder(TextColor.fromHexString("#B4E28C")!!))
		else text.addIcon('d', GuiIcon.bazaarSellOrder(NamedTextColor.GRAY))

		if (terminalMultiblockEntity.isWithdrawAvailable())
			text.addIcon('r', GuiIcon.bazaarBuyOrder(TextColor.fromHexString("#EFC275")!!))
		else text.addIcon('r', GuiIcon.bazaarBuyOrder(NamedTextColor.GRAY))

		if (terminalMultiblockEntity.isDepositAvailable())
			text.addIcon('f', GuiIcon.bazaarBuyOrder(TextColor.fromHexString("#CB625F")!!))
		else text.addIcon('f', GuiIcon.bazaarBuyOrder(NamedTextColor.GRAY))

		val buttonLabels1 = GuiText("", guiWidth = 48)
			.add(getMenuTitleName(text("Purchase", WHITE)), alignment = GuiText.TextAlignment.CENTER, line = 0, horizontalShift = 48 + 12)
			.add(getMenuTitleName(text("Items", WHITE)), alignment = GuiText.TextAlignment.CENTER, line = 1, horizontalShift = 48 + 12)
			.add(getMenuTitleName(text("Receive", WHITE)), alignment = GuiText.TextAlignment.CENTER, line = 6, horizontalShift = 48 + 12)
			.add(getMenuTitleName(text("Orders", WHITE)), alignment = GuiText.TextAlignment.CENTER, line = 7, horizontalShift = 48 + 12)
			.build()

		val buttonLabels2 = GuiText("", guiWidth = 48)
			.add(getMenuTitleName(text("Restock", WHITE)), alignment = GuiText.TextAlignment.CENTER, line = 0, horizontalShift = 96 + 12 + 5)
			.add(getMenuTitleName(text("Listings", WHITE)), alignment = GuiText.TextAlignment.CENTER, line = 1, horizontalShift = 96 + 12 + 5)
			.add(getMenuTitleName(text("Fulfill", WHITE)), alignment = GuiText.TextAlignment.CENTER, line = 6, horizontalShift = 96 + 12 + 5)
			.add(getMenuTitleName(text("Orders", WHITE)), alignment = GuiText.TextAlignment.CENTER, line = 7, horizontalShift = 96 + 12 + 5)
			.build()

		return ofChildren(text.build(), buttonLabels1, buttonLabels2)
	}

	private val fulfillButton = FeedbackLike.withHandler({
		if (terminalMultiblockEntity.isDepositAvailable())
			GuiItem.EMPTY.makeItem(text("Fulfill Buy Orders"))
		else GuiItem.EMPTY.makeItem(text("Buy Order Fulfillment Not Available"))

	}) { _, _ -> handleFulfill() }

	private fun handleFulfill() {
		val cityCheck = Bazaars.checkInValidCity(viewer)
		if (!cityCheck.isSuccess()) return fulfillButton.updateWith(cityCheck)

		println("Fulfill")
	}

	private val depositButton = FeedbackLike.withHandler({
		if (terminalMultiblockEntity.isDepositAvailable())
			GuiItem.EMPTY.makeItem(text("Deposit Items To Your Sell Orders"))
		else GuiItem.EMPTY.makeItem(text("Item deposit Not Available"))

	}) { _, _ -> handleDeposit() }

	private fun handleDeposit() {
		val cityCheck = Bazaars.checkInValidCity(viewer)
		if (!cityCheck.isSuccess()) return depositButton.updateWith(cityCheck)
		BazaarBulkDepositMenu(viewer, terminalMultiblockEntity).openGui(this)
	}

	private val buyButton = ItemProvider {
		if (terminalMultiblockEntity.isWithdrawAvailable())
			GuiItem.EMPTY.makeItem(text("Purchase Items"))
		else GuiItem.EMPTY.makeItem(text("Item Purchase not available"))
			.updateLore(listOf(text("The left merge port is occupying the withdraw capabilities.", HE_MEDIUM_GRAY)))

	}.makeGuiButton { _, _ -> handlePurchase() }

	private fun handlePurchase() {
		println("Purchase")
	}

	private val recieveButton = ItemProvider {
		if (terminalMultiblockEntity.isWithdrawAvailable())
			GuiItem.EMPTY.makeItem(text("Receive Items From Fulfilled Orders"))
			else GuiItem.EMPTY.makeItem(text("Item withdraw not available"))
				.updateLore(listOf(text("The left merge port is occupying the withdraw capabilities.", HE_MEDIUM_GRAY)))

	}.makeGuiButton { _, _ -> handleWithdrawOrder() }

	private fun handleWithdrawOrder() {
		println("Withdraw")
	}
}
