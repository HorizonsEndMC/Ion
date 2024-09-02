package net.horizonsend.ion.server.features.gui.custom.bazaar

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.command.GlobalCompletions
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.miscellaneous.utils.LegacyItemUtils
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.displayNameString
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.builder.ItemBuilder
import xyz.xenondevs.invui.item.impl.SimpleItem
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
    private inner class BazaarGuiItem(itemStack: ItemStack) : SimpleItem(itemStack)

    // Back button
    private inner class ReturnToItemMenuButton : ControlItem<Gui>() {
        override fun getItemProvider(gui: Gui?): ItemProvider {
            return ItemBuilder(ItemStack(Material.IRON_DOOR).updateMeta {
                it.displayName(text("Go Back").decoration(ITALIC, false))
            })
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
                return ItemBuilder(ItemStack(Material.BARRIER).updateMeta {
                    it.displayName(text("Buy at least one item").decoration(ITALIC, false))
                })
            } else {
                return ItemBuilder(ItemStack(Material.HOPPER).updateMeta {
                    // Confirm purchase
                    val priceMult = Bazaars.priceMult(remote)
                    val name = GlobalCompletions.fromItemString(bazaarItem.itemString).displayNameString

                    it.displayName(text("Purchase", NamedTextColor.GREEN).decoration(ITALIC, false))

                    it.lore(listOf(
                        // Buy item + amt
                        text("Buy $currentAmount of $name for ${(bazaarItem.price * currentAmount * priceMult).roundToHundredth()}",
                            NamedTextColor.WHITE)
                            .decoration(ITALIC, false),

                        // Inventory warning
                        if (!LegacyItemUtils.canFit(player.inventory, GlobalCompletions.fromItemString(bazaarItem.itemString), currentAmount)) {
                            ofChildren(
                                text("WARNING: Amount is larger than may fit in your inventory.", NamedTextColor.RED).decoration(ITALIC, false),
                                text("Adding additional items may result in their stacks getting deleted.", NamedTextColor.RED).decoration(ITALIC, false)
                            )
                        } else Component.empty(),

                        // Remote purchase warning
                        if (priceMult > 1) {
                            text("(Price multiplied x $priceMult due to browsing remotely)").decoration(ITALIC, false)
                        } else Component.empty()
                    ))
                })
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