package net.horizonsend.ion.server.features.gui.custom.settings.button.general.collection

import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsGuiItem
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsPageGui
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.horizonsend.ion.server.gui.invui.ListInvUIWindow
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window
import java.util.function.Consumer
import java.util.function.Supplier

class CollectionModificationButton<T: Any, C: Collection<T>>(
	viewer: Player,
	private val title: Component,
	private val description: String,
	private val collectionSupplier: Supplier<C>,
	private val modifiedConsumer: Consumer<MutableCollection<T>>,
	private val toMutableCollection: (C) -> MutableCollection<T>,
	private val itemTransformer: (T) -> ItemStack,
	private val getItemLines: (T) -> Pair<Component?, Component?>,
	private val playerModifier:  (CollectionModificationButton<T, C>.(T, Consumer<T>) -> Unit)?,
	private val entryCreator: CollectionModificationButton<T, C>.(Consumer<T>) -> Unit,
) : ListInvUIWindow<T>(viewer), SettingsGuiItem {
	override fun generateEntries(): List<T> {
		return collectionSupplier.get().toList()
	}

	override fun createItem(entry: T): Item = AsyncItem(
		{ itemTransformer.invoke(entry) },
		{ _ -> clickEntry(entry) }
	)

	override val listingsPerPage: Int = 5

	override fun buildWindow(): Window? {
		val gui = PagedGui.items()
			.setStructure(
				"x 0 0 0 0 0 0 0 b",
				"x 1 1 1 1 1 1 1 d",
				"x 2 2 2 2 2 2 2 f",
				"x 3 3 3 3 3 3 3 h",
				"x 4 4 4 4 4 4 4 j",
				"< v . . . . p . >"
			)
			.addIngredient('<', GuiItems.PageLeftItem())
			.addIngredient('>', GuiItems.PageRightItem())
			.addIngredient('v', parentOrBackButton())
			.addIngredient('p', createNewEntryButton)

			.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.setContent(items)

			.addIngredient('0', getBackingButton(0).tracked())
			.addIngredient('b', getRemoveButton(0).tracked())
			.addIngredient('1', getBackingButton(1).tracked())
			.addIngredient('d', getRemoveButton(1).tracked())
			.addIngredient('2', getBackingButton(2).tracked())
			.addIngredient('f', getRemoveButton(2).tracked())
			.addIngredient('3', getBackingButton(3).tracked())
			.addIngredient('h', getRemoveButton(3).tracked())
			.addIngredient('4', getBackingButton(4).tracked())
			.addIngredient('j', getRemoveButton(4).tracked())

			.handlePageChange()

			.build()

		val newPageNumber = minOf(pageNumber, maxPageNumber)
		if (newPageNumber != pageNumber) {
			gui.setPage(newPageNumber)
			pageNumber = newPageNumber
			openGui()
			return null
		}

		return normalWindow(gui)
	}

	private val emptyButton = GuiItem.EMPTY.makeGuiButton { _, _ ->  }

	private fun getBackingButton(index: Int): Item {
		val displayed = getDisplayedEntries().getOrNull(index) ?: return emptyButton

		return GuiItem.EMPTY.makeGuiButton { _, _ -> clickEntry(displayed) }
	}

	private fun clickEntry(entry: T) {
		if (playerModifier == null) return

		val consumer = { new: T ->
			Tasks.sync {
				val cloned = toMutableCollection.invoke(collectionSupplier.get())
				cloned.remove(entry)
				cloned.add(new)
				modifiedConsumer.accept(cloned)
				openGui()
			}
		}

		playerModifier.invoke(this, entry, consumer)
	}

	private fun getRemoveButton(index: Int): Item {
		val displayed = getDisplayedEntries().getOrNull(index) ?: return emptyButton
		return GuiItem.CANCEL.makeItem(text("Remove", RED)).makeGuiButton { _, _ -> removeEntry(displayed) }
	}

	private fun removeEntry(entry: T) {
		val cloned = toMutableCollection.invoke(collectionSupplier.get())
		cloned.remove(entry)
		modifiedConsumer.accept(cloned)
		openGui()
	}

	override fun buildTitle(): Component {
		val text = GuiText("Modify List")
			.addBackground()

		fun addLines(index: Int) {
			val entry = getDisplayedEntries().getOrNull(index) ?: return
			val (line1, line2) = getItemLines(entry)

			line1?.let { text.add(it, horizontalShift = 18, line = (index * 2)) }
			line2?.let { text.add(it, horizontalShift = 18, line = (index * 2) + 1) }
		}

		(0..4).forEach(::addLines)

		return withPageNumber(text.build())
	}

	override fun getFirstLine(player: Player): Component {
		return title
	}

	override fun getSecondLine(player: Player): Component {
		return text(description, BLUE)
	}

	override fun makeButton(pageGui: SettingsPageGui): GuiItems.AbstractButtonItem {
		return GuiItem.LIST.makeButton(pageGui, title, description) { _, _, _ -> openGui(pageGui) }
	}

	private val createNewEntryButton = GuiItem.PLUS.makeItem(text("Add Entry")).makeGuiButton { _, _ -> addNewEntry() }

	private fun addNewEntry() {
		val consumer = { new: T ->
			Tasks.sync {
				val cloned = toMutableCollection.invoke(collectionSupplier.get())
				cloned.add(new)
				modifiedConsumer.accept(cloned)
				openGui()
			}
		}

		Tasks.async {
			entryCreator.invoke(this, consumer)
		}
	}
}
