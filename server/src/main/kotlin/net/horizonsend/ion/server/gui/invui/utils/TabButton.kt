package net.horizonsend.ion.server.gui.invui.utils

import xyz.xenondevs.invui.gui.TabGui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.controlitem.TabItem

class TabButton(private val provider: ItemProvider, tabNumber: Int) : TabItem(tabNumber) {
	override fun getItemProvider(gui: TabGui?): ItemProvider {
		return provider
	}
}
