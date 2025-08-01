package net.horizonsend.ion.server.gui.invui.utils

import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.window.Window

fun <W : Window, S: Window.Builder<W, S>> S.setTitle(title: Component): S {
	setTitle(AdventureComponentWrapper(title))
	return this
}

fun <W : Window> W.changeTitle(title: Component): W {
	changeTitle(AdventureComponentWrapper(title))
	return this
}

fun ItemStack.asItemProvider(): ItemProvider = ItemProvider { this }
