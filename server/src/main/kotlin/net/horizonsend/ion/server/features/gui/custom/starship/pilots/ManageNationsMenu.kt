package net.horizonsend.ion.server.features.gui.custom.starship.pilots

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.starships.PlayerStarshipData
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.custom.starship.StarshipComputerMenu
import net.horizonsend.ion.server.features.nations.gui.item
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.pull
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.ScrollGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.AsyncItem
import xyz.xenondevs.invui.window.Window
import java.util.function.Supplier

class ManageNationsMenu(val main: StarshipComputerMenu) : AbstractItem() {
    override fun getItemProvider(): ItemProvider = ItemProvider {
        ItemStack(Material.PLAYER_HEAD).updateDisplayName(text("Add / Remove nations", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
    }

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        val data = main.data as? PlayerStarshipData

        if (data == null) {
            player.userError("You can only add nations to player ships!")
            return
        }

        openAddNationMenu(player, data)
    }

    private fun asyncNationItem(id: Oid<Nation>): AsyncItem = object : AsyncItem(
        { ItemStack(Material.PLAYER_HEAD) },
        Supplier {
            ItemProvider {
                item(Material.DIRT).updateDisplayName(text(NationCache[id].name).decoration(TextDecoration.ITALIC, false))
                    .updateLore(listOf(text("Left click to remove", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)))
            }
        }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            Tasks.async {
                val nationName = Nation.findById(id)?.name
                val data = main.data

                PlayerStarshipData.updateById(data._id, pull(PlayerStarshipData::nations, id))
                data.nations?.remove(id)

                player.success("Removed $nationName")

                Tasks.sync {
                    player.closeInventory()
                    openAddNationMenu(player, data)
                }
            }
        }
    }

    fun openAddNationMenu(player: Player, data: PlayerStarshipData) {
        val nationItems = data.nations?.map(::asyncNationItem) ?: listOf()
        val gui = ScrollGui.items()
            .setStructure(
                "b . a . . . . l r",
                "x x x x x x x x x",
                "x x x x x x x x x"
            )
            .addIngredient('b', main.mainMenuButton)
            .addIngredient('a', AddNewNationButton(this))
            .addIngredient('l', GuiItems.ScrollLeftItem())
            .addIngredient('r', GuiItems.ScrollRightItem())
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .setContent(nationItems)
            .build()

        Window.single()
            .setViewer(player)
            .setTitle(AdventureComponentWrapper(text("Add / Remove Nations").decoration(TextDecoration.ITALIC, false)))
            .setGui(gui)
            .build()
            .open()
    }
}