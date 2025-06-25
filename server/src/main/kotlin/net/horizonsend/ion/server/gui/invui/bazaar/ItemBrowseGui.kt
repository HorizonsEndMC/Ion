package net.horizonsend.ion.server.gui.invui.bazaar

import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.horizonsend.ion.server.gui.invui.misc.util.input.TextInputMenu.Companion.searchEntires
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.item.Item

interface ItemBrowseGui<T : Any> : BrowseGui {
	fun formatItem(entry: T): Item {
		return AsyncItem(
			resultProvider = { formatItemStack(entry) },
			handleClick = { _ -> onClickDisplayedItem(entry) }
		)
	}

	fun formatItemStack(entry: T): ItemStack = fromItemString(getItemString(entry)).stripAttributes().updateLore(getItemLore(entry))

	fun getItemLore(entry: T): List<Component>

	fun onClickDisplayedItem(entry: T)

	fun getSearchTerms(entry: T): List<String>

	fun getSearchEntries(): Collection<T>

	fun search(viewer: Player, entries: Collection<T>) {
		viewer.searchEntires(
			entries = entries,
			prompt = text("Search for Items"),
			description = Component.empty(),
			searchTermProvider = { entry: T -> getSearchTerms(entry) },
			backButtonHandler = { openGui() },
			componentTransformer = { entry: T ->  fromItemString(getItemString(entry)).displayNameComponent },
			itemTransformer = { entry: T ->  formatItemStack(entry) },
		) { _: ClickType, result: T -> onClickDisplayedItem(result) }
	}

	val searchButton get() = GuiItem.MAGNIFYING_GLASS.makeItem(text("Search entries")).makeGuiButton { _, viewer ->
		Tasks.async {
			val searchEntries = getSearchEntries()

			Tasks.sync { search(viewer, searchEntries) }
		}
	}

	fun getItemString(entry: T): String
}
