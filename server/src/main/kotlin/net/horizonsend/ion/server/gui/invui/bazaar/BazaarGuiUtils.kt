package net.horizonsend.ion.server.gui.invui.bazaar

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers
import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.utils.input.InputResult
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.Colors
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.withShadowColor
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.economy.bazaar.PlayerFilters
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.utils.buttons.FeedbackLike
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.horizonsend.ion.server.miscellaneous.utils.updateData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.TextColor
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.item.Item
import kotlin.reflect.KMutableProperty1

val REMOTE_WARINING = bracketed(text("REMOTE", TextColor.color(Colors.ALERT)))

fun ItemStack.stripAttributes() = updateData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().build())

const val BAZAAR_SHADOW_COLOR = "#252525FF"

val DEPOSIT_COLOR = TextColor.fromHexString("#1E83FF")!!
val WITHDRAW_COLOR = TextColor.fromHexString("#00FF7B")!!

fun getMenuTitleName(component: Component) = component.withShadowColor(BAZAAR_SHADOW_COLOR)
fun getMenuTitleName(itemStack: ItemStack) = getMenuTitleName(itemStack.displayNameComponent)
fun getMenuTitleName(bazaarItem: BazaarItem) = getMenuTitleName(fromItemString(bazaarItem.itemString))

fun InvUIWindowWrapper.getBazaarSettingsButton() = GuiItem.GEAR.makeItem(text("View settings")).makeGuiButton { _, player -> BazaarGUIs.openBazaarSettings(player, this) }

fun getFilterButton(gui: InvUIWindowWrapper, property: KMutableProperty1<PlayerSettings, String>): Pair<PlayerFilters, Item> {
	val filterData = PlayerFilters.get(gui.viewer, property)

	return filterData to FeedbackLike.withHandler(
		providedItem = {
			GuiItem.FILTER.makeItem(text("Filter Sell Orders"))
		},
		fallbackLoreProvider = {
			listOf(
				template(text("You have {0} filters.", HE_MEDIUM_GRAY), filterData.filters.size),
				text("Shift click to save filters.", HE_MEDIUM_GRAY)
			)
		},
	) { clickType, _ ->
		if (clickType.isShiftClick) {
			filterData.save(gui.viewer, PlayerSettings::bazaarSellManageFilters)
			updateWith(InputResult.SuccessReason(listOf(template(text("Saved {0} filters.", GREEN), filterData.filters.size))))
			return@withHandler
		}

		BazaarGUIs.openBazaarFilterMenu(gui.viewer, filterData, gui)
	}
}
