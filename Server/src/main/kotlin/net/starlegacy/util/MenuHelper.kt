package net.starlegacy.util

import co.aikar.commands.ACFBukkitUtil
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.pane.OutlinePane
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane
import com.github.stefvanschie.inventoryframework.pane.Pane
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import kotlin.math.min
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

object MenuHelper {
	fun gui(rows: Int, title: String) = ChestGui(rows, ACFBukkitUtil.color(title)).apply {
		setOnGlobalClick { event: InventoryClickEvent ->
			val item = event.currentItem
			if (item?.type?.isAir != false) {
				event.isCancelled = true
			}
		}
	}

	fun ChestGui.withPane(pane: Pane): ChestGui {
		addPane(pane); return this
	}

	fun staticPane(x: Int, y: Int, length: Int, height: Int) = StaticPane(x, y, length, height)

	fun outlinePane(x: Int, y: Int, length: Int, height: Int) = OutlinePane(x, y, length, height)

	fun guiButton(itemStack: ItemStack, action: (InventoryClickEvent.() -> Unit) = {}) =
		GuiItem(itemStack.ensureServerConversions()) { event ->
			event.isCancelled = true; event.action()
		}

	fun guiButton(type: Material, action: (InventoryClickEvent.() -> Unit) = {}) =
		guiButton(ItemStack(type), action)

	fun GuiItem.setName(text: String?): GuiItem {
		if (text != null) {
			item.setDisplayNameAndGet(text)
		}

		return this
	}

	fun GuiItem.setName(text: Component?): GuiItem {
		if (text != null) {
			item.setDisplayNameAndGet(text)
		}

		return this
	}

	fun GuiItem.setLore(text: String): GuiItem = this@setLore.setLore(text.split("\n"))

	fun GuiItem.setLore(vararg lines: String): GuiItem = this@setLore.setLore(lines.toList())

	fun GuiItem.setLore(lines: List<String>): GuiItem = apply {
		item.itemMeta = item.itemMeta?.apply {
			lore = lines.map(String::colorize)
		}
	}

	fun StaticPane.withItem(item: GuiItem, x: Int, z: Int): StaticPane {
		addItem(item, x, z); return this
	}

	fun OutlinePane.withItem(item: GuiItem): OutlinePane {
		addItem(item); return this
	}

	fun OutlinePane.withItems(items: Iterable<GuiItem>): OutlinePane {
		items.forEach { addItem(it) }; return this
	}

	fun Player.openConfirmMenu(
		title: String,
		onConfirm: InventoryClickEvent.() -> Unit,
		onCancel: InventoryClickEvent.() -> Unit,
		confirmLore: String? = null,
		cancelLore: String? = null
	) = Tasks.sync {
		val confirmButton = guiButton(Material.LILY_PAD, onConfirm).setName("${ChatColor.GREEN}CONFIRM")

		if (confirmLore != null) {
			confirmButton.setLore(confirmLore)
		}
		val cancelButton = guiButton(Material.BARRIER) {
			whoClicked.closeInventory(); onCancel()
		}.setName("${ChatColor.RED}CANCEL")

		if (cancelLore != null) {
			cancelButton.setLore(cancelLore)
		}

		val pane = staticPane(0, 0, 9, 1)
			.withItem(confirmButton, 3, 0)
			.withItem(cancelButton, 5, 0)
		gui(1, title).withPane(pane).show(this@openConfirmMenu)
	}

	fun Player.openPaginatedMenu(
		title: String,
		items: List<GuiItem>,
		titleItems: List<GuiItem> = listOf()
	) = Tasks.sync {
		require(titleItems.size <= 6)

		val itemRows = 5
		val itemCount = itemRows * 9

		val rows = min(items.size / 9 + 1, itemRows)
		val gui = gui(rows + 1, title)
		val rootPane = PaginatedPane(0, 0, 9, gui.rows)
		val pages = (items.size / itemCount) + 1
		for (page in 0 until pages) {
			val startIndex = page * itemCount

			rootPane.addPane(page, outlinePane(3, 0, 6, 1).apply {
				titleItems.forEach { addItem(it) }
			})

			val topBar = staticPane(0, 0, 3, 1)

			if (pages != 1) {
				val button = guiButton(ItemStack(Material.WHITE_STAINED_GLASS_PANE)).setName("Page ${page + 1}/$pages")
				topBar.addItem(button, 1, 0)
			}

			if (page != 0) {
				val previous = page - 1
				topBar.addItem(guiButton(Material.REDSTONE_BLOCK) {
					rootPane.page = previous; gui.update()
				}.setName("Previous Page (${previous + 1})"), 0, 0)
			}

			if (page < pages - 1) {
				val next = page + 1
				topBar.addItem(guiButton(Material.EMERALD_BLOCK) {
					rootPane.page = next; gui.update()
				}.setName("Next Page (${next + 1})"), 2, 0)
			}

			rootPane.addPane(page, topBar)

			val pageItems = items.subList(startIndex, min(items.size, startIndex + 45))
			val contents = outlinePane(0, 1, 9, rows)
			pageItems.forEach(contents::addItem)
			rootPane.addPane(page, contents)
		}
		gui.withPane(rootPane).show(this)
	}
}