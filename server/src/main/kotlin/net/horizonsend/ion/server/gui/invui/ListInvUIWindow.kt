package net.horizonsend.ion.server.gui.invui

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import xyz.xenondevs.invui.item.Item
import kotlin.math.ceil

abstract class ListInvUIWindow<T: Any?>(viewer: Player, protected var pageNumber: Int = 0, async: Boolean = true) : InvUIWindowWrapper(viewer, async) {
	protected lateinit var entries: List<T>
	protected lateinit var items: List<Item>

	abstract val listingsPerPage: Int
	protected val displayRange get() = (pageNumber * listingsPerPage).. minOf(items.size, (pageNumber + 1) * listingsPerPage)

	abstract fun generateEntries(): List<T>

	abstract fun createItem(entry: T): Item

	/** Generates the items and entries */
	protected fun reGenerateItems() {
		entries = generateEntries()
		items = entries.map(::createItem)
	}

	override fun openGui() {
		if (async) Tasks.async {
			reGenerateItems()
			currentWindow = buildWindow()
			Tasks.sync { currentWindow?.open() }
		}

		else Tasks.sync {
			reGenerateItems()
			currentWindow = buildWindow()
			currentWindow?.open()
		}
	}

	protected open val pageNumberLine = 10

	protected fun getPageNumberText(): Component {
		val maxPageNumber = ceil(items.size.toDouble() / (listingsPerPage.toDouble())).toInt()
		val pageNumberString = "${pageNumber + 1} / $maxPageNumber"

		return GuiText("").add(
			text(pageNumberString),
			line = pageNumberLine,
			GuiText.TextAlignment.CENTER,
			verticalShift = 4
		).build()
	}

	/** Builds the provided GUI text and returns a component containing it, and the page number text. */
	protected fun withPageNumber(guiText: GuiText) = ofChildren(guiText.build(), getPageNumberText())

	/** Builds the provided GUI text and returns a component containing it, and the page number text. */
	protected fun withPageNumber(guiText: Component) = ofChildren(guiText, getPageNumberText())

	/** Returns a list of entries that are currently displayed */
	fun getDisplayedEntries(): List<T> {
		return entries.subList(displayRange.first, displayRange.last)
	}

	/** Returns a list of entries that are currently displayed */
	fun getDisplayedItems(): List<Item> {
		return items.subList(displayRange.first, displayRange.last)
	}
}
