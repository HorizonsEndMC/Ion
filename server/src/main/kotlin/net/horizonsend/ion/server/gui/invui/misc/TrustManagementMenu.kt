package net.horizonsend.ion.server.gui.invui.misc

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.DbObjectCompanion
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.horizonsend.ion.server.features.nations.gui.skullItem
import net.horizonsend.ion.server.gui.CommonGuiWrapper
import net.horizonsend.ion.server.gui.invui.ListInvUIWindow
import net.horizonsend.ion.server.gui.invui.misc.util.input.TextInputMenu.Companion.openSearchMenu
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.Id
import org.litote.kmongo.pull
import org.litote.kmongo.push
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window
import kotlin.reflect.KProperty1

class TrustManagementMenu<D : DbObject, I : Oid<D>, C : DbObjectCompanion<D, I>, E: Id<*>, T : Iterable<E>>(
	viewer: Player,
	val id: I,
	val companion: C,
	private val property: KProperty1<D, T>,
	private val available: T,
	private val itemTransformer: (E, String) -> ItemStack,
	private val nameProvider: (E) -> String
) : ListInvUIWindow<E>(viewer, async = true) {
	override val listingsPerPage: Int = 4

	override fun generateEntries(): List<E> {
		val document = companion.col[id] ?: return listOf()
		return property.get(document).toList()
	}

	private val nameCache = mutableMapOf<E, String>()

	fun getName(entry: E): String = nameCache.getOrPut(entry) { nameProvider.invoke(entry) }

	override fun createItem(entry: E): Item {
		return AsyncItem({ itemTransformer.invoke(entry, getName(entry)) }) {}
	}

	override fun buildWindow(): Window {
		val gui = PagedGui.items()
			.setStructure(
				"x . . . . . . . b",
				"x . . . . . . . d",
				"x . . . . . . . f",
				"x . . . . . . . h",
				"x . . . . . . . j",
				"< v . . . . p . >"
			)
			.addIngredient('<', GuiItems.PageLeftItem())
			.addIngredient('>', GuiItems.PageRightItem())
			.addIngredient('v', parentOrBackButton())
			.addIngredient('p', createNewEntryButton)

			.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.setContent(items)

			.addIngredient('b', getRemoveButton(0).tracked())
			.addIngredient('d', getRemoveButton(1).tracked())
			.addIngredient('f', getRemoveButton(2).tracked())
			.addIngredient('h', getRemoveButton(3).tracked())
			.addIngredient('j', getRemoveButton(4).tracked())

		return normalWindow(gui.build())
	}

	override fun buildTitle(): Component {
		val text = GuiText("Modify Trusted")
			.addBackground()

		fun addLines(index: Int) {
			val entry = getDisplayedEntries().getOrNull(index) ?: return

			 text.add(text(getName(entry)), horizontalShift = 20, line = (index * 2), verticalShift = 5)
//			 text.add(it, horizontalShift = 18, line = (index * 2) + 1)
		}

		(0..4).forEach(::addLines)

		if (entries.isEmpty()) {
			text.add(text("No entries..."))
		}

		return withPageNumber(text.build())
	}

	private val emptyButton = GuiItem.EMPTY.makeItem(empty()).makeGuiButton { _, _ ->  }

	private fun getRemoveButton(index: Int): Item {
		val displayed = getDisplayedEntries().getOrNull(index) ?: return emptyButton
		return GuiItem.CANCEL.makeItem(text("Remove", RED)).makeGuiButton { _, _ -> removeEntry(displayed) }
	}

	private fun removeEntry(entry: E) {
		Tasks.async {
			companion.updateById(id, pull(property, entry))
			openGui()
		}
	}

	private val createNewEntryButton = GuiItem.PLUS.makeItem(text("Add Entry")).makeGuiButton { _, _ -> addNewEntry() }

	private fun addNewEntry() = Tasks.async {
		viewer.openSearchMenu(
			entries = available.toList(),
			searchTermProvider = { listOf(getName(it)) },
			prompt = text("Add to ${property.name}"),
			backButtonHandler = { this@TrustManagementMenu.openGui() },
			componentTransformer = { text(getName(it)) },
			itemTransformer = { itemTransformer.invoke(it, getName(it)) },
			handler = { _, result ->
				companion.updateById(id, push(property, result))
				this@TrustManagementMenu.openGui()
			}
		)
	}

	companion object {
		fun <D : DbObject, I: Oid<D>, C : DbObjectCompanion<D, I>> player(viewer: Player, id: I, companion: C, property: KProperty1<D, Iterable<SLPlayerId>>, parent: CommonGuiWrapper?) {
			Tasks.async {
				TrustManagementMenu(
					viewer,
					id,
					companion,
					property,
					SLPlayer.allIds().toSet(),
					{ entry, name -> skullItem(entry.uuid, name) },
				) { SLPlayer.getName(it)!! }.openGui(parent)
			}
		}
		fun <D : DbObject, I: Oid<D>, C : DbObjectCompanion<D, I>> settlement(viewer: Player, id: I, companion: C, property: KProperty1<D, Iterable<Oid<Settlement>>>, parent: CommonGuiWrapper?) {
			Tasks.async {
				TrustManagementMenu(
					viewer,
					id,
					companion,
					property,
					Settlement.allIds().toSet(),
					{ _, name -> GuiItem.CITY.makeItem(text(name)) }
				) { SettlementCache[it].name }.openGui(parent)
			}
		}
		fun <D : DbObject, I: Oid<D>, C : DbObjectCompanion<D, I>> nation(viewer: Player, id: I, companion: C, property: KProperty1<D, Iterable<Oid<Nation>>>, parent: CommonGuiWrapper?) {
			Tasks.async {
				TrustManagementMenu(
					viewer,
					id,
					companion,
					property,
					Nation.allIds().toSet(),
					{ _, name -> GuiItem.CITY.makeItem(text(name)) }
				) { NationCache[it].name }.openGui(parent)
			}
		}
	}
}
