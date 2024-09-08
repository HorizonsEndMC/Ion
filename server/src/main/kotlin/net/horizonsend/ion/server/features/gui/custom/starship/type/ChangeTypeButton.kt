package net.horizonsend.ion.server.features.gui.custom.starship.type

import net.horizonsend.ion.common.utils.text.ITALIC
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.gui.custom.starship.StarshipComputerMenu
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.miscellaneous.utils.actualType
import net.horizonsend.ion.server.miscellaneous.utils.setDisplayNameAndGet
import net.horizonsend.ion.server.miscellaneous.utils.setLoreAndGet
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
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
			.setDisplayNameAndGet(text("Change Ship Class").decoration(ITALIC, false))
			.setLoreAndGet(listOf(
				ofChildren(text("Current type: ", NamedTextColor.GRAY), main.data.starshipType.actualType.displayNameComponent).decoration(ITALIC, false),
				Component.empty(),
				text("Different starship types", NamedTextColor.GRAY).decoration(ITALIC, false),
				text("support different block", NamedTextColor.GRAY).decoration(ITALIC, false),
				text("counts, weapons, and tools.", NamedTextColor.GRAY).decoration(ITALIC, false)
			))
	}

	override fun getItemProvider(): ItemProvider = provider

	override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
		openClassMenu(player)
	}

	private fun createButton(type: StarshipType): SelectTypeButton = SelectTypeButton(this, type)

	private fun openClassMenu(player: Player) {
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
			.setTitle(AdventureComponentWrapper(text("Change Ship Class").decoration(ITALIC, false)))
			.setGui(gui)
			.build()
			.open()
	}
}
