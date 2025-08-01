package net.horizonsend.ion.server.features.gui.custom.bazaar

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.command.GlobalCompletions
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars.priceMult
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.utils.setTitle
import net.horizonsend.ion.server.miscellaneous.utils.LegacyItemUtils
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.displayNameString
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.builder.ItemBuilder
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.controlitem.ControlItem
import xyz.xenondevs.invui.window.AnvilWindow
import xyz.xenondevs.invui.window.Window

class BazaarPurchaseMenuGui(
	viewer: Player,
	private val bazaarItem: BazaarItem,
	private val sellerName: String,
	val remote: Boolean,
	val returnCallback: () -> Unit = {},
	val confirmCallback: (Player, BazaarItem, Int, Boolean) -> Unit
) : InvUIWindowWrapper(viewer) {
    private var currentAmount = 0

    private val returnToItemMenuItem = ReturnToItemMenuButton()
    private val confirmPurchaseItem = ConfirmPurchaseButton()

    private fun createGui(): Gui {
        val gui = Gui.normal()

        gui.setStructure(". v x")

        gui.addIngredient('.', BazaarGuiItem(GlobalCompletions.fromItemString(bazaarItem.itemString))) // go back
            .addIngredient('v', returnToItemMenuItem) // purchase
            .addIngredient('x', confirmPurchaseItem) // item

        return gui.build()
    }

	override fun buildWindow(): Window {
		val gui = createGui()

		val window = AnvilWindow.single()
			.setViewer(viewer)
			.setTitle(buildTitle())
			.setGui(gui)
			.addRenameHandler { string ->
				currentAmount = string?.toIntOrNull() ?: 0
				returnToItemMenuItem.notifyWindows()
				confirmPurchaseItem.notifyWindows()
			}
			.build()

		return window
	}

	override fun buildTitle(): Component {
		return text("Buying $sellerName's ${GlobalCompletions.fromItemString(bazaarItem.itemString).displayNameString}")
	}

    // Item being purchased
    private inner class BazaarGuiItem(val itemStack: ItemStack) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            // make this item's name empty so that it does not populate anvil name field
            return ItemBuilder(itemStack.updateDisplayName(empty()))
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {}
    }

    // Back button
    private inner class ReturnToItemMenuButton : ControlItem<Gui>() {
        override fun getItemProvider(gui: Gui?): ItemProvider {
            return ItemBuilder(ItemStack(Material.IRON_DOOR).updateDisplayName(text("Go Back").itemName))
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // openItemMenu from Bazaars.kt
            currentWindow?.close()
            Tasks.syncDelay(1L, returnCallback)
        }
    }

    // Confirm button
    private inner class ConfirmPurchaseButton : ControlItem<Gui>() {
        override fun getItemProvider(gui: Gui?): ItemProvider {
            if (currentAmount <= 0) {
                // Invalid number
                return ItemBuilder(ItemStack(Material.BARRIER).updateDisplayName(text("Buy at least one item").itemName))
            } else {
				// Confirm purchase
				val priceMult = priceMult(remote)
				val name = GlobalCompletions.fromItemString(bazaarItem.itemString).displayNameString

                return ItemBuilder(ItemStack(Material.HOPPER)
					.updateLore(listOf(
						// Buy item + amt
						text("Buy $currentAmount of $name for ${(bazaarItem.price * currentAmount * priceMult).roundToHundredth()}"),

						// Inventory warning
						if (!LegacyItemUtils.canFit(viewer.inventory, GlobalCompletions.fromItemString(bazaarItem.itemString), currentAmount)) {
							ofChildren(
								text("WARNING: Amount is larger than may fit in your inventory.", NamedTextColor.RED),
								text("Adding additional items may result in their stacks getting deleted.", NamedTextColor.RED)
							)
						} else empty(),

						// Remote purchase warning
						if (priceMult > 1) {
							text("(Price multiplied x $priceMult due to browsing remotely)")
						} else empty()
					))
					.updateDisplayName(text("Purchase", NamedTextColor.GREEN).itemName)
				)
            }
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // call tryBuy from Bazaars.kt and close the window
            if (currentAmount > 0) {
                confirmCallback(player, bazaarItem, currentAmount, remote)
                currentWindow?.close()
            }
        }
    }
}
