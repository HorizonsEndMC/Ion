package net.horizonsend.ion.server.gui.invui.utils.buttons

import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.horizonsend.ion.server.features.nations.gui.skullItem
import net.horizonsend.ion.server.gui.invui.utils.asItemProvider
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import java.util.UUID
import java.util.function.Consumer

fun ItemProvider.makeGuiButton(clickConsumer: (ClickType, Player) -> Unit): AbstractItem = GuiItems.createButton(this) { type, player, _ -> clickConsumer.invoke(type, player) }
fun ItemStack.makeGuiButton(clickConsumer: (ClickType, Player) -> Unit): AbstractItem = GuiItems.createButton(this) { type, player, _ -> clickConsumer.invoke(type, player) }

fun makeInformationButton(title: Component, vararg loreLines: Component) = GuiItem.INFO.makeItem(title).updateLore(listOf(*loreLines)).asItemProvider()

fun getHeadItem(uuid: UUID, playerName: String, headEditor: Consumer<ItemStack>, handleClick: (InventoryClickEvent) -> Unit): AsyncItem =
	AsyncItem(
		{ skullItem(uuid, playerName).apply { headEditor.accept(this) } },
		handleClick
	)
