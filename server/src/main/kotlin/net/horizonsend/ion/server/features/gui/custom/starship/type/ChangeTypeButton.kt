package net.horizonsend.ion.server.features.gui.custom.starship.type

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.gui.custom.starship.StarshipComputerMenu
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.miscellaneous.utils.actualType
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.window.Window

class ChangeTypeButton(val main: StarshipComputerMenu) : AbstractItem() {
	private val provider = ItemProvider {
		ItemStack(Material.GHAST_TEAR)
			.updateDisplayName(text("Change Ship Class").itemName)
			.updateLore(listOf(
				ofChildren(text("Current type: ", GRAY), main.data.starshipType.actualType.displayNameComponent).itemName,
				Component.empty(),
				text("Different starship types", GRAY).itemName,
				text("support different block", GRAY).itemName,
				text("counts, weapons, and tools.", GRAY).itemName
			))
	}

	override fun getItemProvider(): ItemProvider = provider

	override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
		openClassMenu(player)
	}

	private fun createButton(type: StarshipType): SelectTypeButton = SelectTypeButton(this, type)

	fun openClassMenu(player: Player) {
		val grouped = StarshipType
			.entries
			.filter { it.displayInMainMenu }
			.filter { it.canUse(player) }
			.map(::createButton)
			.groupBy { it.type.typeCategory }
			.entries
			.sortedBy { it.key.index }
			.withIndex()

		val gui = Gui.normal()
			.setStructure(
				"b . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . ."
			)
			.addIngredient('b', main.mainMenuButton)
			.build()

		for ((yIndex, buttons) in grouped) {
			for ((xIndex, button) in buttons.value.withIndex()) gui.setItem(xIndex, yIndex + 1, button)
		}

		Window.single()
			.setViewer(player)
			.setTitle(AdventureComponentWrapper(text("Change Ship Class").itemName))
			.setGui(gui)
			.build()
			.open()
	}
}
