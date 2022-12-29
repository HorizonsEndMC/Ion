package net.starlegacy.feature.nations.gui

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane
import net.md_5.bungee.api.ChatColor.GREEN
import net.md_5.bungee.api.ChatColor.RED
import net.starlegacy.util.Tasks
import org.bukkit.Material
import org.bukkit.Material.BARRIER
import org.bukkit.Material.LILY_PAD
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import kotlin.math.min

fun Player.openConfirmMenu(
	title: String,
	onConfirm: InventoryClickEvent.() -> Unit,
	onCancel: InventoryClickEvent.() -> Unit,
	confirmLore: String? = null,
	cancelLore: String? = null
) = Tasks.sync {
	val confirmButton = guiButton(LILY_PAD, onConfirm).name("${GREEN}CONFIRM")
	if (confirmLore != null) confirmButton.lore(confirmLore)
	val cancelButton = guiButton(BARRIER) { whoClicked.closeInventory(); onCancel() }.name("${RED}CANCEL")
	if (cancelLore != null) cancelButton.lore(cancelLore)
	gui(1, title)
		.withPane(
			staticPane(0, 0, 9, 1)
				.withItem(confirmButton, 3, 0)
				.withItem(cancelButton, 5, 0)
		)
		.show(this@openConfirmMenu)
}

fun Player.openPaginatedMenu(title: String, items: List<GuiItem>, titleItems: List<GuiItem> = listOf()) = Tasks.sync {
	require(titleItems.size <= 6)

	val rows = min(items.size / 9 + 1, 5)
	val gui = gui(rows + 1, title)
	val rootPane = PaginatedPane(0, 0, 9, gui.rows)
	val pages = (items.size / 49) + 1
	for (page in 0 until pages) {
		val startIndex = page * 49

		rootPane.addPane(page, outlinePane(3, 0, 6, 1).apply {
			titleItems.forEach { addItem(it) }
		})

		val topBar = staticPane(0, 0, 3, 1)

		topBar.addItem(guiButton(item(Material.WHITE_STAINED_GLASS_PANE)).name("Page ${page + 1}/$pages"), 1, 0)
		if (pages > 0) {
			if (page != 0) {
				val previous = page - 1
				topBar.addItem(guiButton(Material.REDSTONE_BLOCK) {
					rootPane.page = previous; gui.update()
				}.name("Previous Page (${previous + 1})"), 0, 0)
			}

			if (page < pages - 1) {
				val next = page + 1
				topBar.addItem(guiButton(Material.EMERALD_BLOCK) {
					rootPane.page = next; gui.update()
				}.name("Next Page (${next + 1})"), 2, 0)
			}
		}

		rootPane.addPane(page, topBar)

		val pageItems = items.filterIndexed { index, _ -> index >= startIndex && index < startIndex + 49 }
		val contents = outlinePane(0, 1, 9, rows)
		pageItems.forEach(contents::addItem)
		rootPane.addPane(page, contents)
	}
	gui.withPane(rootPane).show(this)
}
