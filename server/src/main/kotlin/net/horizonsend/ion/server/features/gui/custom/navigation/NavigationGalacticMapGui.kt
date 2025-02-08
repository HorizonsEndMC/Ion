package net.horizonsend.ion.server.features.gui.custom.navigation

import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.utils.text.SPACE_MAIN_HYPERSPACE_ROUTES_CHARACTER
import net.horizonsend.ion.common.utils.text.SPACE_MINOR_HYPERSPACE_ROUTES_CHARACTER
import net.horizonsend.ion.common.utils.text.SPACE_SCREEN_CHARACTER
import net.horizonsend.ion.common.utils.text.SPACE_STARRY_BACKGROUND_CHARACTER
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.repeatString
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.window.Window

class NavigationGalacticMapGui(val player: Player) {

    private var currentWindow: Window? = null
    private val gui = Gui.empty(9, 6)

    companion object {
        private const val MENU_ROW = 5
    }

    private fun createGui(): Gui {
        gui.setItem(1, 4, createSystemCustomControlItem(
            "Asteri",
            Component.text("High Security", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false),
            GuiItem.ASTERI_2))
        gui.setItem(3, 4, createSystemCustomControlItem(
            "Regulus",
            Component.text("High Security", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false),
            GuiItem.REGULUS_2))
        gui.setItem(5, 4, createSystemCustomControlItem(
            "Ilios",
            Component.text("High Security", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false),
            GuiItem.ILIOS_2
        ))
        gui.setItem(1, 2, createSystemCustomControlItem(
            "Sirius",
            Component.text("High Security", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false),
            GuiItem.SIRIUS_2
        ))
        gui.setItem(4, 2, createSystemCustomControlItem(
            "Horizon",
            Component.text("No Security", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false),
            GuiItem.HORIZON_2
        ))
        gui.setItem(3, 0, createSystemCustomControlItem(
            "Trench",
            Component.text("No Security", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false),
            GuiItem.EMPTY_STAR
        ))
        gui.setItem(7, 2, createSystemCustomControlItem(
            "AU-0821",
            Component.text("No Security", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false),
            GuiItem.EMPTY_STAR
        ))

        gui.setItem(0, MENU_ROW, GuiItems.closeMenuItem(player))

        gui.setItem(2, MENU_ROW, GuiItems.CustomControlItem(Component.text("Search For Destination"), GuiItem.MAGNIFYING_GLASS) {
                _: ClickType, player: Player, _: InventoryClickEvent ->
            NavigationGuiCommon.openSearchMenu(player, player.world, gui) {
                NavigationGalacticMapGui(player).openMainWindow()
            }
        })

        NavigationGuiCommon.updateGuiRoute(gui, player)

        return gui
    }

    private fun createSystemCustomControlItem(worldName: String, status: Component, item: GuiItem) = GuiItems.CustomControlItem(
        Component.text(worldName).decoration(TextDecoration.ITALIC, false),
        item,
        listOf(
            status,
            Component.text(repeatString("=", 30)).decorate(TextDecoration.STRIKETHROUGH).color(NamedTextColor.DARK_GRAY),
            enterSystemComponent()
        )
    ) {
        _: ClickType, _: Player, _: InventoryClickEvent ->
        val world = Bukkit.getWorld(worldName)
        if (world == null) {
            player.serverError("World '${worldName}' not found")
            return@CustomControlItem
        }

        NavigationSystemMapGui(player, world).openMainWindow()
    }

    private fun enterSystemComponent(): Component = template(
        "{0} to view this system's map",
        color = HE_LIGHT_GRAY,
        paramColor = NamedTextColor.AQUA,
        useQuotesAroundObjects = false,
        "Click"
    ).decoration(TextDecoration.ITALIC, false)

    private fun createText(): Component {
        val header = "Galactic Map"
        val guiText = GuiText(header)

        guiText.addBackground(GuiText.GuiBackground(backgroundChar = SPACE_SCREEN_CHARACTER))
        guiText.addBackground(GuiText.GuiBackground(backgroundChar = SPACE_STARRY_BACKGROUND_CHARACTER, backgroundWidth = 143))
        guiText.addBackground(GuiText.GuiBackground(backgroundChar = SPACE_MINOR_HYPERSPACE_ROUTES_CHARACTER, backgroundWidth = 122))
        guiText.addBackground(GuiText.GuiBackground(backgroundChar = SPACE_MAIN_HYPERSPACE_ROUTES_CHARACTER, backgroundWidth = 136))

        return guiText.build()
    }

    fun openMainWindow() {
        val gui = createGui()

        val window = Window.single()
            .setViewer(player)
            .setGui(gui)
            .setTitle(AdventureComponentWrapper(createText()))
            .build()

        currentWindow = window.apply { open() }
    }
}