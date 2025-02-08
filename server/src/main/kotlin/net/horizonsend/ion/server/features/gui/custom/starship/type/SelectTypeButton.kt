package net.horizonsend.ion.server.features.gui.custom.starship.type

import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import org.bukkit.Material.BARRIER
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.window.Window

class SelectTypeButton(val parent: ChangeTypeButton, val type: StarshipType) : AbstractItem() {
	private val subClasses = type.menuSubclasses.get().filter { it.canUse(parent.main.player) }

	private val provider = ItemProvider {
		ItemStack(type.menuItemRaw.get())
			.updateDisplayName(type.displayNameComponent.itemName)
			.updateLore(listOf(
				ofChildren(text("Minimum Block Count: ", GRAY), text(type.minSize, AQUA)).itemName,
				ofChildren(text("Maximum Block Count: ", GRAY), text(type.maxSize, AQUA)).itemName,
				empty(),
				text("Left click to select", AQUA).itemName,
				if (subClasses.isNotEmpty()) text("Right click to view [${subClasses.size}] subclass(es)", AQUA).itemName else empty(),
			))
	}

	override fun getItemProvider(): ItemProvider = provider

	override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
		if (subClasses.isNotEmpty() && clickType == ClickType.RIGHT) {
			openSubclassMenu(player)
			return
		}

		DeactivatedPlayerStarships.updateType(parent.main.data, type)
		player.closeInventory()
		parent.main.open()

		player.success("Changed type to $type")
	}

	fun openSubclassMenu(player: Player) {
		val returnToPilotMenu = GuiItems.createButton(
			ItemStack(BARRIER).updateDisplayName(text("Go back to class selection menu", WHITE).itemName)
		) { _, _, _ ->
			player.closeInventory()
			parent.openClassMenu(player)
		}

		val subclassButtons = type.menuSubclasses.get().map { SelectTypeButton(parent, it) }

		val gui = PagedGui.items()
			.setStructure(
				"b . . . . . . l r",
				"x x x x x x x x x",
				"x x x x x x x x x",
			)
			.addIngredient('b', returnToPilotMenu)
			.addIngredient('l', GuiItems.PageLeftItem())
			.addIngredient('r', GuiItems.PageRightItem())
			.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.setContent(subclassButtons)
			.build()

		Window.single()
			.setViewer(player)
			.setTitle(AdventureComponentWrapper(ofChildren(type.displayNameComponent, text("'s subclasses")).itemName))
			.setGui(gui)
			.build()
			.open()
	}
}
