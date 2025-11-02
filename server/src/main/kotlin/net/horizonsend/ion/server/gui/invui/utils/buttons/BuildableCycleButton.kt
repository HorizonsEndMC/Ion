package net.horizonsend.ion.server.gui.invui.utils.buttons

import net.horizonsend.ion.server.features.gui.GuiItem
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.impl.CycleItem

/**
 * Cycle button that handles the click logic when selected
 **/
class BuildableCycleButton private constructor(
	private val buttons: List<Item>
) : CycleItem() {
	override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
		super.handleClick(clickType, player, event)
		buttons[state].handleClick(clickType, player, event)
	}

	class Builder private constructor(private val buttons: MutableList<Item> = mutableListOf()) {
		fun addButton(item: Item): Builder {
			buttons.add(item)
			return this
		}

		fun addButton(icon: GuiItem, clickLogic: (Player) -> Unit = {}): Builder {
			buttons.add(icon.makeGuiButton { _, player -> clickLogic.invoke(player) })
			return this
		}

		fun build(): BuildableCycleButton = BuildableCycleButton(buttons)
	}
}
