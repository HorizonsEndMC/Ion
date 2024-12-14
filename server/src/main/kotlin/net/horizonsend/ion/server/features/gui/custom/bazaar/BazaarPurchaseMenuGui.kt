package net.horizonsend.ion.server.features.gui.custom.bazaar

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.command.GlobalCompletions
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars.priceMult
import net.horizonsend.ion.server.miscellaneous.utils.LegacyItemUtils
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.applyDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.applyLore
import net.horizonsend.ion.server.miscellaneous.utils.displayNameString
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.builder.ItemBuilder
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.controlitem.ControlItem
import xyz.xenondevs.invui.window.AnvilWindow
import xyz.xenondevs.invui.window.Window

class BazaarPurchaseMenuGui(
    val player: Player,
    private val bazaarItem: BazaarItem,
    private val sellerName: String,
    val remote: Boolean,
    val returnCallback: () -> Unit = {},
    val confirmCallback: (Player, BazaarItem, Int, Boolean) -> Unit
) {

    private var currentWindow: Window? = null
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

    fun open(player: Player): Window {
        val gui = createGui()

        val window = AnvilWindow.single()
            .setViewer(player)
            .setTitle(AdventureComponentWrapper(text("Buying $sellerName's ${GlobalCompletions.fromItemString(bazaarItem.itemString).displayNameString}")))
            .setGui(gui)
            .addRenameHandler { string ->
                currentAmount = string?.toIntOrNull() ?: 0
                returnToItemMenuItem.notifyWindows()
                confirmPurchaseItem.notifyWindows()
            }
            .build()

        return window
    }

    fun openMainWindow() {
        currentWindow = open(player).apply { open() }
    }

    // Item being purchased
    private inner class BazaarGuiItem(val itemStack: ItemStack) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            // make this item's name empty so that it does not populate anvil name field
            return ItemBuilder(itemStack.applyDisplayName(empty()))
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {}
    }

    // Back button
    private inner class ReturnToItemMenuButton : ControlItem<Gui>() {
        override fun getItemProvider(gui: Gui?): ItemProvider {
            return ItemBuilder(ItemStack(Material.IRON_DOOR).applyDisplayName(text("Go Back").itemName))
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
                return ItemBuilder(ItemStack(Material.BARRIER).applyDisplayName(text("Buy at least one item").itemName))
            } else {
				// Confirm purchase
				val priceMult = priceMult(remote)
				val name = GlobalCompletions.fromItemString(bazaarItem.itemString).displayNameString

                return ItemBuilder(ItemStack(Material.HOPPER)
					.applyLore(listOf(
						// Buy item + amt
						text("Buy $currentAmount of $name for ${(bazaarItem.price * currentAmount * priceMult).roundToHundredth()}"),

						// Inventory warning
						if (!LegacyItemUtils.canFit(player.inventory, GlobalCompletions.fromItemString(bazaarItem.itemString), currentAmount)) {
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
					.applyDisplayName(text("Purchase", NamedTextColor.GREEN).itemName)
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
