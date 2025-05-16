package net.horizonsend.ion.server.gui.invui.utils.buttons

import net.horizonsend.ion.server.features.gui.GuiItems
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem

fun ItemProvider.makeGuiButton(clickConsumer: (ClickType, Player) -> Unit): AbstractItem = GuiItems.createButton(this) { type, player, _ -> clickConsumer.invoke(type, player) }
fun ItemStack.makeGuiButton(clickConsumer: (ClickType, Player) -> Unit): AbstractItem = GuiItems.createButton(this) { type, player, _ -> clickConsumer.invoke(type, player) }
