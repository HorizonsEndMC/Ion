package net.horizonsend.ion.server.gui.invui.bazaar.terminal

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.gui.icons.GuiIcon
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.multiblock.type.economy.BazaarTerminalMultiblock
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.DEPOSIT_COLOR
import net.horizonsend.ion.server.gui.invui.bazaar.WITHDRAW_COLOR
import net.horizonsend.ion.server.gui.invui.utils.buttons.FeedbackLike
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
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
				". . . w w w p p p ",
				". . . w w w p p p ",
				". . . w w w p p p ",
				". . . f f f d d d ",
				". . . f f f d d d ",
				". . . f f f d d d "
			)
			.addIngredient('f', fulfillButton)
			.addIngredient('p', purchaseButton)
			.addIngredient('w', withdrawOrderButton)
			.addIngredient('d', depositButton)
			.build()

		return normalWindow(gui)
	}

	override fun buildTitle(): Component {
		val text = GuiText("Bazaar Terminal Main Menu")
			.addBackground()
			.setGuiIconOverlay(
				". . . . . . . . . ",
				"l c r . f . . p . ",
				". . . . . . . . . ",
				". . . . . . . . . ",
				". . . . w . . d . ",
				". . . . . . . . . "
			)
			.addIcon('l', GuiIcon.textInputBoxLeft())
			.addIcon('c', GuiIcon.textInputBoxCenter())
			.addIcon('r', GuiIcon.textInputBoxRight())
			.add(Component.text("Owner"), line = 1, horizontalShift = 1)

		if (terminalMultiblockEntity.isWithdrawAvailable())
			text.addIcon('f', GuiIcon.withdrawIcon(NamedTextColor.YELLOW, true))
		else text.addIcon('f', GuiIcon.withdrawIcon(NamedTextColor.GRAY, true))

		if (terminalMultiblockEntity.isWithdrawAvailable())
			text.addIcon('p', GuiIcon.withdrawIcon(WITHDRAW_COLOR, true))
		else text.addIcon('p', GuiIcon.withdrawIcon(NamedTextColor.GRAY, true))

		if (terminalMultiblockEntity.isDepositAvailable())
			text.addIcon('w', GuiIcon.depositIcon(NamedTextColor.RED, true))
		else text.addIcon('w', GuiIcon.depositIcon(NamedTextColor.GRAY, true))

		if (terminalMultiblockEntity.isDepositAvailable())
			text.addIcon('d', GuiIcon.depositIcon(DEPOSIT_COLOR, true))
		else text.addIcon('d', GuiIcon.depositIcon(NamedTextColor.GRAY, true))

		return text.build()
	}

	private val fulfillButton = FeedbackLike.withHandler({
		if (terminalMultiblockEntity.isDepositAvailable())
			GuiItem.EMPTY.makeItem(Component.text("Fulfill Buy Orders"))
		else GuiItem.EMPTY.makeItem(Component.text("Buy Order Fulfillment Not Available"))
			.updateLore(listOf(Component.text("Buy order fulfillment is not available outside of trade cities", HE_MEDIUM_GRAY)))

	}) { _, _ -> handleFulfill() }

	private fun handleFulfill() {
		println("Fulfill")
	}

	private val depositButton = FeedbackLike.withHandler({
		if (terminalMultiblockEntity.isDepositAvailable())
			GuiItem.EMPTY.makeItem(Component.text("Deposit Items To Your Sell Orders"))
		else GuiItem.EMPTY.makeItem(Component.text("Item deposit Not Available"))
			.updateLore(listOf(Component.text("Item deposit is not available outside of trade cities", HE_MEDIUM_GRAY)))

	}) { _, _ -> handleDeposit() }

	private fun handleDeposit() {
		val cityCheck = Bazaars.checkInValidCity(viewer)
		if (!cityCheck.isSuccess()) return depositButton.updateWith(cityCheck)
		BazaarBulkDepositMenu(viewer, terminalMultiblockEntity).openGui(this)
	}

	private val purchaseButton = ItemProvider {
		if (terminalMultiblockEntity.isWithdrawAvailable())
			GuiItem.EMPTY.makeItem(Component.text("Purchase Items"))
		else GuiItem.EMPTY.makeItem(Component.text("Item Purchase not available"))
			.updateLore(listOf(Component.text("The left merge port is occupying the withdraw capabilities.", HE_MEDIUM_GRAY)))

	}.makeGuiButton { _, _ -> handlePurchase() }

	private fun handlePurchase() {
		println("Purchase")
	}

	private val withdrawOrderButton = ItemProvider {
		if (terminalMultiblockEntity.isWithdrawAvailable())
			GuiItem.EMPTY.makeItem(Component.text("Withdraw Items From Fulfilled Orders"))
			else GuiItem.EMPTY.makeItem(Component.text("Item withdraw not available"))
				.updateLore(listOf(Component.text("The left merge port is occupying the withdraw capabilities.", HE_MEDIUM_GRAY)))

	}.makeGuiButton { _, _ -> handleWithdrawOrder() }

	private fun handleWithdrawOrder() {
		println("Withdraw")
	}
}
