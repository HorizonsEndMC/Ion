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
import java.util.EnumSet
import java.util.LinkedList

class ChangeClassButton(val main: StarshipComputerMenu) : AbstractItem() {
	private val tradeClasses = EnumSet.of(
		StarshipType.SHUTTLE,
		StarshipType.TRANSPORT,
		StarshipType.LIGHT_FREIGHTER,
		StarshipType.MEDIUM_FREIGHTER,
		StarshipType.HEAVY_FREIGHTER,
		StarshipType.BARGE,
	)

	private val warshipClasses = EnumSet.of(
		StarshipType.STARFIGHTER,
		StarshipType.GUNSHIP,
		StarshipType.CORVETTE,
		StarshipType.FRIGATE,
		StarshipType.DESTROYER,
		StarshipType.CRUISER,
		StarshipType.BATTLECRUISER,
		StarshipType.BATTLESHIP,
		StarshipType.DREADNOUGHT,
	)

	private val miscClasses = EnumSet.of(
		StarshipType.SPEEDER,
		StarshipType.PLATFORM,
		StarshipType.TANK,
		StarshipType.UNIDENTIFIEDSHIP
	)

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
		val tradeButtons = tradeClasses.filter { it.canUse(player) }.mapTo(LinkedList(), ::createButton)
			.sortedBy { it.type.minLevel }
		val warshipButtons = warshipClasses.filter { it.canUse(player) }.mapTo(LinkedList(), ::createButton)
			.sortedBy { it.type.minLevel }
		val miscButtons = miscClasses.filter { it.canUse(player) }.mapTo(LinkedList(), ::createButton)
			.sortedBy { it.type.minLevel }

		val gui = Gui.normal()
			.setStructure(
				"b . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . ."
			)
			.addIngredient('b', main.mainMenuButton)
			.build()

		for ((index, tradeClass) in tradeButtons.withIndex()) {
			println("index: $index")
			gui.setItem(index, 1, tradeClass)
		}
		for ((index, warshipClass) in warshipButtons.withIndex()) gui.setItem(index, 2, warshipClass)
		for ((index, miscClass) in miscButtons.withIndex()) gui.setItem(index, 3, miscClass)

		Window.single()
			.setViewer(player)
			.setTitle(AdventureComponentWrapper(text("Change Ship Class").decoration(ITALIC, false)))
			.setGui(gui)
			.build()
			.open()
	}
}
