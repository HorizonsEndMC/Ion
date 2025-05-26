package net.horizonsend.ion.server.gui.invui.bazaar

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers
import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.Colors
import net.horizonsend.ion.common.utils.text.withShadowColor
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.horizonsend.ion.server.miscellaneous.utils.updateData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor
import org.bukkit.inventory.ItemStack

val REMOTE_WARINING = bracketed(text("REMOTE", TextColor.color(Colors.ALERT)))

fun ItemStack.stripAttributes() = updateData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().build())

const val BAZAAR_SHADOW_COLOR = "#252525FF"

fun getMenuTitleName(component: Component) = component.withShadowColor(BAZAAR_SHADOW_COLOR)
fun getMenuTitleName(itemStack: ItemStack) = getMenuTitleName(itemStack.displayNameComponent)
fun getMenuTitleName(bazaarItem: BazaarItem) = getMenuTitleName(fromItemString(bazaarItem.itemString))

fun InvUIWindowWrapper.getBazaarSettingsButton() = GuiItem.GEAR.makeItem(text("View settings")).makeGuiButton { _, player -> BazaarGUIs.openBazaarSettings(player, this) }
