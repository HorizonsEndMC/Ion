package net.horizonsend.ion.server.gui.invui.utils

import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.TabGui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.controlitem.TabItem

class TabButton(
	private val selectedProvider: ItemProvider,
	private val unselectedProvider: ItemProvider,
	private val tabNumber: Int
) : TabItem(tabNumber) {
	constructor(
		selected: ItemStack,
		unselected: ItemStack,
		tabNumber: Int
	) : this(ItemProvider { selected }, ItemProvider { unselected }, tabNumber)

	override fun getItemProvider(gui: TabGui): ItemProvider {
		return if (gui.currentTab == tabNumber) selectedProvider else unselectedProvider
	}

	companion object {
		fun standard(tabName: String, tabNumber: Int): TabButton {
			return TabButton(
				ItemStack(Material.GLOWSTONE)
					.updateDisplayName(Component.text(tabName))
					.updateLore(listOf(Component.text("You currently have this tab selected."))),
				ItemStack(Material.GUNPOWDER)
					.updateDisplayName(Component.text(tabName))
					.updateLore(listOf(Component.text("Click to switch to this tab."))),
				tabNumber
			)
		}
	}
}
