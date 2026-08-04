package net.horizonsend.ion.server.features.gui.custom.starship.pilots

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.starships.PlayerStarshipData
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.text.BOLD
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.addToSet
import org.litote.kmongo.setValue
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.window.AnvilWindow

class AddNewNationButton(val nationMenu: ManageNationsMenu) : AbstractItem() {
    var currentName = ""

    private val nameConfirmButton = NameConfirmButton(this)

    override fun getItemProvider(): ItemProvider = ItemProvider {
        ItemStack(Material.BEACON).updateDisplayName(text("Add Nation").itemName)
    }

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        open(player)
    }

    private val returnToNationMenu = GuiItems.createButton(
        ItemStack(Material.BARRIER).updateDisplayName(text("Go back to nation menu", WHITE).itemName)
    ) { _, player, _ ->
        nationMenu.openAddNationMenu(player, nationMenu.main.data as PlayerStarshipData)
    }

    private val namePreset = SimpleItem(ItemStack(Material.PAPER).updateDisplayName(Component.empty()))

    fun open(player: Player) {
        val gui = Gui.normal()
            .setStructure("n v x")
            .addIngredient('n', namePreset)
            .addIngredient('v', returnToNationMenu)
            .addIngredient('x', nameConfirmButton)

        AnvilWindow.single()
            .setViewer(player)
            .setTitle(AdventureComponentWrapper(text("Enter Nation Name")))
            .setGui(gui)
            .addRenameHandler { string ->
                currentName = string
                nameConfirmButton.update()
            }
            .build()
            .open()
    }

    private class NameConfirmButton(val addNation: AddNewNationButton) : AbstractItem() {
        val nationNotFoundItem = ItemStack(Material.BARRIER)
            .updateDisplayName(text("Nation not found!", NamedTextColor.RED, BOLD).itemName)

        val loadingItem = ItemStack(Material.STONE)
            .updateDisplayName(text("Loading...", NamedTextColor.GRAY).itemName)

        var currentProvider: ItemProvider = ItemProvider { loadingItem }
        var id: Oid<Nation>? = null

        override fun getItemProvider(): ItemProvider {
            return currentProvider
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            val nationId = id
            if (nationId != null) Tasks.async {
                val data = addNation.nationMenu.main.data

                val nationsSet = PlayerStarshipData.findPropById(data._id, PlayerStarshipData::nations)
                if (nationsSet == null) {
                    PlayerStarshipData.updateById(data._id, setValue(PlayerStarshipData::nations, mutableSetOf()))
                }
                (data as PlayerStarshipData).nations?.plusAssign(nationId)
                PlayerStarshipData.updateById(data._id, addToSet(PlayerStarshipData::nations, id))

                player.success("Added ${addNation.currentName} as pilots to starship.")

                Tasks.sync {
                    player.closeInventory()
                    addNation.nationMenu.openAddNationMenu(player, data)
                }
            }
        }

        fun update() {
            currentProvider = ItemProvider { loadingItem }
            notifyWindows()

            Tasks.async {
                val nation = Nation.findByName(addNation.currentName)

                currentProvider = if (nation != null) ItemProvider {
                    id = nation
                    ItemStack(Material.DIRT).updateDisplayName(text(addNation.currentName).itemName)
                } else {
                    id = null
                    ItemProvider { nationNotFoundItem }
                }

                Tasks.sync { notifyWindows() }
            }
        }
    }
}