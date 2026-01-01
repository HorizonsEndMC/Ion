package net.horizonsend.ion.server.gui.invui.misc

import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.starships.Blueprint
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.server.command.starship.BlueprintCommand.blueprintInfo
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.horizonsend.ion.server.features.gui.item.CollectionScrollButton
import net.horizonsend.ion.server.features.nations.gui.playerClicker
import net.horizonsend.ion.server.gui.invui.ListInvUIWindow
import net.horizonsend.ion.server.gui.invui.misc.util.input.TextInputMenu.Companion.openSearchMenu
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.actualType
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import org.litote.kmongo.descendingSort
import org.litote.kmongo.eq
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window
import kotlin.reflect.KMutableProperty1

class BlueprintMenu(viewer: Player, val target: SLPlayerId = viewer.slPlayerId, val consumer: (Blueprint, Player) -> Unit) : ListInvUIWindow<Blueprint>(viewer, async = true) {
	override val listingsPerPage: Int = 36

	private var sortTypes: List<KMutableProperty1<Blueprint, out Any>> = listOf(
		Blueprint::size,
		Blueprint::type,
		Blueprint::name
	)

	private var filterType: Int = 0

	override fun generateEntries(): List<Blueprint> {
		return Blueprint
			.find(Blueprint::owner eq target)
			.descendingSort(sortTypes[filterType])
			.toList()
	}

	override fun createItem(entry: Blueprint): Item {
		return AsyncItem(
			{
				entry.type.actualType.menuItem.clone()
					.updateDisplayName(text(entry.name))
					.updateLore(blueprintInfo(entry).map(String::miniMessage))
			},
			{ event ->
				consumer.invoke(entry, event.playerClicker)
			}
		)
	}

	override fun buildWindow(): Window {
		val gui = PagedGui.items()
			.setStructure(
				"b . . . . . . s F",
				"x x x x x x x x x",
				"x x x x x x x x x",
				"x x x x x x x x x",
				"x x x x x x x x x",
				"< . . . . . . . >",
			)

			.addIngredient('b', parentOrBackButton())
			.addIngredient('s', searchButton)
			.addIngredient('F', sortButton)

			.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.addIngredient('<', GuiItems.PageLeftItem())
			.addIngredient('>', GuiItems.PageRightItem())
			.setContent(items)

			.handlePageChange()
			.build()

		return normalWindow(gui, viewer)
	}

	val searchButton = GuiItem.MAGNIFYING_GLASS.makeItem(text("Search Blueprints")).makeGuiButton { _, _ ->
		viewer.openSearchMenu(
			entries = entries,
			searchTermProvider = { listOf(it.name, it.type) },
			prompt = text("Search for Blueprint"),
			backButtonHandler = { this@BlueprintMenu.openGui() },
			componentTransformer = { text(it.name, it.type.actualType.textColor) },
			itemTransformer = { blueprint ->
				blueprint.type.actualType.menuItem.clone()
					.updateDisplayName(text(blueprint.name))
					.updateLore(blueprintInfo(blueprint).map(String::miniMessage))
			},
			handler = { _, blueprint ->
				consumer.invoke(blueprint, viewer)
			}
		)
	}

	private val sortButton = CollectionScrollButton(
		entries = sortTypes,
		providedItem = { GuiItem.SORT.makeItem(text("Change Sorting Method")) },
		value = { filterType },
		nameFormatter = { filterProperty -> text(filterProperty.name.replaceFirstChar { it.uppercase() }) },
		valueConsumer = { index, _ ->
			filterType = index
			openGui()
		}
	)

	override fun buildTitle(): Component {
		return withPageNumber(GuiText("Your blueprints").addBackground())
	}
}
