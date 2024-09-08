package net.horizonsend.ion.server.features.gui.custom.starship.type

import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.text.ITALIC
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.miscellaneous.utils.setDisplayNameAndGet
import net.horizonsend.ion.server.miscellaneous.utils.setLoreAndGet
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem

class SelectTypeButton(val parent: ChangeTypeButton, val type: StarshipType) : AbstractItem() {
	private val subClasses = type.menuSubclasses.get().filter { it.canUse(parent.main.player) }

	private val provider = ItemProvider {
		ItemStack(type.menuItemRaw.get())
			.setDisplayNameAndGet(type.displayNameComponent.decoration(ITALIC, false))
			.setLoreAndGet(listOf(
				ofChildren(text("Minimum Block Count: ", GRAY), text(type.minSize, AQUA)).decoration(ITALIC, false),
				ofChildren(text("Maximum Block Count: ", GRAY), text(type.maxSize, AQUA)).decoration(ITALIC, false),
				empty(),
				text("Left click to select", AQUA).decoration(ITALIC, false),
				if (subClasses.isNotEmpty()) text("Right click to view subclasses", AQUA).decoration(ITALIC, false) else empty(),
			))
	}

	override fun getItemProvider(): ItemProvider = provider

	override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
		if (subClasses.isNotEmpty() && clickType == ClickType.RIGHT) {
			player.success("boop")
			return
		}

		DeactivatedPlayerStarships.updateType(parent.main.data, type)
		player.closeInventory()
		parent.main.open()

		player.success("Changed type to $type")
	}
}
