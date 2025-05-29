package net.horizonsend.ion.server.gui.invui.bazaar.terminal

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.gui.icons.GuiIcon
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.multiblock.type.economy.BazaarTerminalMultiblock
import net.horizonsend.ion.server.features.transport.items.util.ItemReference
import net.horizonsend.ion.server.features.transport.items.util.getRemovableItems
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.DEPOSIT_COLOR
import net.horizonsend.ion.server.gui.invui.bazaar.WITHDRAW_COLOR
import net.horizonsend.ion.server.gui.invui.input.ItemMenu
import net.horizonsend.ion.server.gui.invui.utils.buttons.FeedbackLike
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.window.Window

class BazaarTerminalMainMenu(
	viewer: Player,
	private val terminalMultiblockEntity: BazaarTerminalMultiblock.BazaarTerminalMultiblockEntity
) : InvUIWindowWrapper(viewer, async = true) {
	val availableForDeposit = mutableMapOf<ItemStack, ArrayDeque<ItemReference>>()

	override fun firstTimeSetup() {
		availableForDeposit.clear()

		val references = terminalMultiblockEntity.getBazaarDepositInventories()

		for (reference in references) {
			for ((index, item: ItemStack) in getRemovableItems(reference.inventory)) {
				availableForDeposit.getOrPut(item.asOne()) { ArrayDeque() }.add(ItemReference(reference.inventory, index))
			}
		}
	}

	override fun buildWindow(): Window {
		val gui = Gui.normal()
			.setStructure(
				". . . . . . w w w ",
				". . . . . . w w w ",
				". . . . . . w w w ",
				". . . . . . d d d ",
				". . . . i . d d d ",
				". . . . . . d d d "
			)
			.addIngredient('d', depositButton)
			.addIngredient('w', withdrawButton)
			.addIngredient('i', depositInfoButton)
			.build()

		return normalWindow(gui)
	}

	private val depositButton = ItemProvider {
		if (terminalMultiblockEntity.isDepositAvailable())
			GuiItem.EMPTY.makeItem(Component.text("Deposit Items"))
			else GuiItem.EMPTY.makeItem(Component.text("Item deposit not available outside of trade cities"))

	}.makeGuiButton { _, _ -> handleDeposit() }

	private fun handleDeposit() {
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

	val depositInfoButton = FeedbackLike.withHandler(GuiItem.DOWN.makeItem(Component.text("BB"))) { _, _ -> openDepositInfo() }

	private fun openDepositInfo() {
		val items: List<Item> = availableForDeposit.map { (asOne, references) ->
			asOne.clone()
				.updateLore(listOf(template(Component.text("{0} available for deposit", HE_MEDIUM_GRAY), references.sumOf { it.get()?.amount ?: 0 })))
				.makeGuiButton { _, _ ->  }
		}

		val itemMenuTitle = GuiText("Available For Deposit")
			.setSlotOverlay(
				"# # # # # # # # #",
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				"# # # # # # # # #"
			)
			.build()

		ItemMenu(
			title = itemMenuTitle,
			viewer = viewer,
			guiItems = items,
			backButtonHandler = { this@BazaarTerminalMainMenu.openGui() }
		).openGui()
	}
}
