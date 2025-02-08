package net.horizonsend.ion.server.features.gui.custom.navigation

import net.horizonsend.ion.common.utils.text.DEFAULT_GUI_WIDTH
import net.horizonsend.ion.common.utils.text.GUI_MARGIN
import net.horizonsend.ion.common.utils.text.wrap
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.window.Window

class NavigationInfoGui(
    val player: Player,
    private val name: String,
    private val icon: GuiItem,
    private val oreComponent: Component? = null,
    private val backButtonHandler: () -> Unit
) {

    private var currentWindow: Window? = null

    private fun createGui(): Gui {
        val gui = Gui.normal()

        gui.setStructure(
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            "x v . . o . . . ."
        )

        gui.addIngredient('v', GuiItems.CustomControlItem(Component.text("Return To Galactic Menu").decoration(TextDecoration.ITALIC, false), GuiItem.DOWN) {
            _: ClickType, _: Player, _: InventoryClickEvent -> backButtonHandler.invoke()
        })
            .addIngredient('x', GuiItems.closeMenuItem(player))
            .addIngredient('o', GuiItems.CustomControlItem(Component.text(name).decoration(TextDecoration.ITALIC, false), icon))

        return gui.build()
    }

    private fun createText(): Component {
        val header = "Information: $name"
        val guiText = GuiText(header)

        guiText.addBackground()
        val wrappedText = navigationInfoMap[name]?.wrap(DEFAULT_GUI_WIDTH - GUI_MARGIN)
            ?: Component.text("Unknown destination").wrap(DEFAULT_GUI_WIDTH - GUI_MARGIN)
        val componentList = if (oreComponent != null) listOf(oreComponent) + wrappedText else wrappedText

        for ((index, component) in componentList.withIndex()) {
            guiText.add(component, line = index)
        }

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

    private val navigationInfoMap = mapOf(
        "Asteri" to Component.text("Asteri is an M-type star with five planets.").decoration(TextDecoration.ITALIC, false),
        "Regulus" to Component.text("Regulus is an A-type star with four planets.").decoration(TextDecoration.ITALIC, false),
        "Ilios" to Component.text("Ilios is a G-type star with four planets.").decoration(TextDecoration.ITALIC, false),
        "Sirius" to Component.text("Sirius is a G-type star with four planets.").decoration(TextDecoration.ITALIC, false),
        "Horizon" to Component.text("Horizon is a large black hole with one planet and a smaller sector.").decoration(TextDecoration.ITALIC, false),
        "Trench" to Component.text("Trench is a resource-rich sector filled with asteroids.").decoration(TextDecoration.ITALIC, false),
        "AU-0821" to Component.text("AU-0821 is a sector with many starship wrecks and derelict ruins.").decoration(TextDecoration.ITALIC, false),
        "Chandra" to Component.text("Chandra is a moon-like, cratered planet. Its depressions offer many mineral resources.").decoration(TextDecoration.ITALIC, false),
        "Ilius" to Component.text("Ilius is a temperate planet with many different biomes. Pine forests, snowy mountains, and gravel beaches offer many different places to settle down.").decoration(TextDecoration.ITALIC, false),
        "Luxiterna" to Component.text("Luxiterna is an alien planet with many mushrooms and vines dotting its colorful surface. Coexist with its native flora or extract its valuable resources.").decoration(TextDecoration.ITALIC, false),
        "Herdoli" to Component.text("Herdoli is a barren, rusted and cratered planet. There is nothing but orange sand and more orange sand here.").decoration(TextDecoration.ITALIC, false),
        "Rubaciea" to Component.text("Rubaciea is an icy planet with a burning inner core. The contrast between its outer icy shell and underground lava caverns makes this planet quite the scientific phenomenon.").decoration(TextDecoration.ITALIC, false),
        "Aret" to Component.text("Aret is a dry, desert planet. Rugged mesas and sands conceal many mineral resources ripe for extraction.").decoration(TextDecoration.ITALIC, false),
        "Aerach" to Component.text("Aerach is a temperate, mountainous and cavernous planet. Its pink sakura trees dotting the surface and many lakes make this world the ideal vacation spot.").decoration(TextDecoration.ITALIC, false),
        "Vask" to Component.text("Vask is a temperate, mild planet. A dry savannah and blue hills gives this planet the appearance of oxidized copper.").decoration(TextDecoration.ITALIC, false),
        "Gahara" to Component.text("Gahara is an icy planet. A large, subterranean ocean underneath its planet-wide ice crust harbors much oceanic life.").decoration(TextDecoration.ITALIC, false),
        "Isik" to Component.text("Isik is a hot lava planet. Lava lakes and volcanic mountains makes this planet the ideal location for a dramatic settler.").decoration(TextDecoration.ITALIC, false),
        "Chimgara" to Component.text("Chimgara is a temperate, alien planet. Lush, multicolored plant life covers the many sinkholes on the planet's surface.").decoration(TextDecoration.ITALIC, false),
        "Damkoth" to Component.text("Damkoth is a rocky, cavernous planet. Darkened rock gives way to purple crystalline caverns that host a variety of mineral resources.").decoration(TextDecoration.ITALIC, false),
        "Krio" to Component.text("Krio is an icy, large planet. Ice spikes and dirt boulders are scattered across the terrain, and large ice veins run from the mountains to the valleys.").decoration(TextDecoration.ITALIC, false),
        "Qatra" to Component.text("Qatra is a hot, lava planet. Geographic anomalies indicate the presence of structures that belong to an extinct advanced civilization.").decoration(TextDecoration.ITALIC, false),
        "Kovfefe" to Component.text("Kovfefe is a hot, moon-like planet. Following several controversial news reports after its initial discovery, it has since been settled for its valuable mineral resources.").decoration(TextDecoration.ITALIC, false),
        "Lioda" to Component.text("Lioda is a temperate ocean planet. An archipelago covers its surface and many islands are the ideal location for a scenic retreat.").decoration(TextDecoration.ITALIC, false),
        "Turms" to Component.text("Turms is a small, moon-like planet. Its unremarkable status may give settlers a chance to build a base away from the populated main worlds.").decoration(TextDecoration.ITALIC, false),
        "Eden" to Component.text("CLASSIFIED")
    )
}