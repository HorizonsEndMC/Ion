package net.horizonsend.ion.server.gui.invui.bazaar.terminal

import net.horizonsend.ion.common.utils.text.gui.icons.GuiIcon
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.multiblock.type.economy.BazaarTerminalMultiblock
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.DEPOSIT_COLOR
import net.horizonsend.ion.server.gui.invui.bazaar.WITHDRAW_COLOR
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
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
				". . . . . . w w w ",
				". . . . . . w w w ",
				". . . . . . w w w ",
				". . . . . . d d d ",
				". . . . . . d d d ",
				". . . . . . d d d "
			)
			.addIngredient('d', depositButton)
			.addIngredient('w', withdrawButton)
			.build()

		return normalWindow(gui)
	}

	private val depositButton = ItemProvider {
		if (terminalMultiblockEntity.isDepositAvailable())
			GuiItem.EMPTY.makeItem(Component.text("Deposit Items"))
			else GuiItem.EMPTY.makeItem(Component.text("Item deposit not available outside of trade cities"))

	}.makeGuiButton { _, _ -> handleDeposit() }

	private fun handleDeposit() {
		BazaarBulkDepositMenu(viewer, terminalMultiblockEntity).openGui(this)
		println("Deposit")
	}

	private val withdrawButton = ItemProvider {
		if (terminalMultiblockEntity.isWithdrawAvailable())
			GuiItem.EMPTY.makeItem(Component.text("Withdraw Items"))
			else GuiItem.EMPTY.makeItem(Component.text("Item withdraw not available"))

	}.makeGuiButton { _, _ -> handleWithdraw() }

	private fun handleWithdraw() {
		println("Withdraw")
	}

	override fun buildTitle(): Component {
		val text = GuiText("Bazaar Terminal Main Menu")
			.addBackground()
			.setGuiIconOverlay(
				". . . . . . . . . ",
				". . . . . . . w . ",
				". . . . . . . . . ",
				". . . . . . . . . ",
				". . . . . . . d . ",
				". . . . . . . . . "
			)

		if (terminalMultiblockEntity.isWithdrawAvailable())
			text.addIcon('w', GuiIcon.withdrawIcon(WITHDRAW_COLOR, true))
			else text.addIcon('w', GuiIcon.withdrawIcon(NamedTextColor.GRAY, true))

		if (terminalMultiblockEntity.isDepositAvailable())
			text.addIcon('d', GuiIcon.depositIcon(DEPOSIT_COLOR, true))
			else text.addIcon('d', GuiIcon.depositIcon(NamedTextColor.GRAY, true))

		return text.build()
	}
}
