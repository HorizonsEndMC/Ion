package net.horizonsend.ion.server.gui.invui.utils.buttons

import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.gui.invui.utils.asItemProvider
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem

fun ItemProvider.makeGuiButton(clickConsumer: (ClickType, Player) -> Unit): AbstractItem = GuiItems.createButton(this) { type, player, _ -> clickConsumer.invoke(type, player) }
fun ItemStack.makeGuiButton(clickConsumer: (ClickType, Player) -> Unit): AbstractItem = GuiItems.createButton(this) { type, player, _ -> clickConsumer.invoke(type, player) }

fun makeInformationButton(title: Component, vararg loreLines: Component) = GuiItem.INFO.makeItem(title).updateLore(listOf(*loreLines)).asItemProvider()
