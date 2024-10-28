package net.horizonsend.ion.server.features.gui.custom.settings

import net.horizonsend.ion.common.utils.luckPerms
import net.horizonsend.ion.server.command.misc.CombatTimerCommand
import net.horizonsend.ion.server.command.misc.EnableProtectionMessagesCommand
import net.horizonsend.ion.server.command.misc.IonSitCommand
import net.horizonsend.ion.server.command.qol.SearchCommand
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.gui.AbstractBackgroundPagedGui
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.TextDecoration
import net.luckperms.api.node.NodeEqualityPredicate
import net.luckperms.api.node.types.PermissionNode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window
import kotlin.math.ceil
import kotlin.math.min

class SettingsOtherGui(val player: Player) : AbstractBackgroundPagedGui {
	private val lpUserManager = luckPerms.userManager
	private val lpUser = lpUserManager.getUser(player.uniqueId)
	private val state: Boolean get() = lpUser?.data()?.contains(sitStateNode, NodeEqualityPredicate.EXACT)?.asBoolean() ?: true

    companion object {
        private const val SETTINGS_PER_PAGE = 5
        private const val PAGE_NUMBER_VERTICAL_SHIFT = 4

		val sitStateNode = PermissionNode.builder("ion.sit.allowed").build()
    }

    override var currentWindow: Window? = null

    private val buttonsList = listOf(
        ShowItemSearchItems(),
        EnableCombatTimerAlert(),
        EnableProtectionMessages(),
		AllowSitting(),
    )

    override fun createGui(): PagedGui<Item> {
        val gui = PagedGui.items()

        gui.setStructure(
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "< v . . . . . . >"
        )

        gui.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('<', GuiItems.LeftPageItem())
            .addIngredient('>', GuiItems.RightPageItem())
            .addIngredient('v', SettingsMainMenuGui(player).ReturnToMainMenuButton())

        for (button in buttonsList) {
            gui.addContent(button)

            for (i in 1..8) {
                gui.addContent(GuiItems.BlankItem(button))
            }
        }

        return gui.build()
    }

    override fun createText(player: Player, currentPage: Int): Component {

        val enabledSettings = listOf(
            PlayerCache[player.uniqueId].showItemSearchItem,
            PlayerCache[player.uniqueId].enableCombatTimerAlerts,
            PlayerCache[player.uniqueId].protectionMessagesEnabled,
			state
        )

        // create a new GuiText builder
        val header = "Other Settings"
        val guiText = GuiText(header)
        guiText.addBackground()

        // get the index of the first setting to display for this page
        val startIndex = currentPage * SETTINGS_PER_PAGE

        for (buttonIndex in startIndex until min(startIndex + SETTINGS_PER_PAGE, buttonsList.size)) {

            val title = buttonsList[buttonIndex].text
            val line = (buttonIndex - startIndex) * 2

            // setting title
            guiText.add(
                component = title,
                line = line,
                horizontalShift = 21
            )

            // setting description
            guiText.add(
                component = if (enabledSettings[buttonIndex]) text("ENABLED", GREEN) else text("DISABLED", RED),
                line = line + 1,
                horizontalShift = 21
            )
        }

        // page number
        val pageNumberString =
            "${currentPage + 1} / ${ceil((buttonsList.size.toDouble() / SETTINGS_PER_PAGE)).toInt()}"
        guiText.add(
            text(pageNumberString),
            line = 10,
            GuiText.TextAlignment.CENTER,
            verticalShift = PAGE_NUMBER_VERTICAL_SHIFT
        )

        return guiText.build()
    }

    fun openMainWindow() {
        currentWindow = open(player).apply { open() }
    }

    private inner class ShowItemSearchItems : GuiItems.AbstractButtonItem(
        text("Show /itemsearch Items").decoration(TextDecoration.ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.COMPASS_NEEDLE.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            val itemSearch = PlayerCache[player.uniqueId].showItemSearchItem
            SearchCommand.itemSearchToggle(player, !itemSearch)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class EnableCombatTimerAlert : GuiItems.AbstractButtonItem(
        text("Enable Combat Timer Alerts").decoration(TextDecoration.ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.LIST.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            val enableCombatTimerAlerts = PlayerCache[player.uniqueId].enableCombatTimerAlerts
            CombatTimerCommand.onToggle(player, !enableCombatTimerAlerts)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class EnableProtectionMessages : GuiItems.AbstractButtonItem(
        text("Enable Protection Messages").decoration(TextDecoration.ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.LIST.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            EnableProtectionMessagesCommand.defaultCase(player)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class AllowSitting : GuiItems.AbstractButtonItem(
        text("Allow Sitting on Stairs / Slabs").decoration(TextDecoration.ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.LIST.customModelData) }
    ) {

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            IonSitCommand.enableSitting(player, !state)

			// Delay a tick to allow for async luckperms db update
            Tasks.sync { currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage))) }
        }
    }
}
