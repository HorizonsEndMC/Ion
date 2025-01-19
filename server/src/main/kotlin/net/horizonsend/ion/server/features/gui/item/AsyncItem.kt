package net.horizonsend.ion.server.features.gui.item

import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import java.util.function.Supplier

class AsyncItem(
	val resultProvider: Supplier<ItemStack>,
	val handleClick: (InventoryClickEvent) -> Unit
) : AbstractItem() {
	private var provider = loadingItem

	override fun getItemProvider(viewer: Player): ItemProvider {
		return provider
	}

	override fun handleClick(p0: ClickType, p1: Player, event: InventoryClickEvent) {
		handleClick.invoke(event)
	}

	fun update() {
		provider = loadingItem
		notifyWindows()

		Tasks.async {
			val item = buildResultItem()
			provider = ItemProvider { item }

			Tasks.sync { notifyWindows() }
		}
	}

	fun buildResultItem(): ItemStack {
		var result: ItemStack = loadingItem.get()

		runCatching { resultProvider.get() }
			.onFailure { exception -> result = getFailureItem(exception) }
			.onSuccess { value -> result = value  }

		return result
	}

	companion object {
		val loadingItem = ItemProvider {
			GuiItem.LOADING.makeItem(Component.text("Loading..."))
		}

		fun getFailureItem(exception: Throwable) = ItemStack(Material.BARRIER)
			.updateDisplayName(Component.text("ERROR", NamedTextColor.RED))
			.updateLore(listOf(
				Component.text("Sorry, there was an error getting the result, please forward this to staff.", NamedTextColor.RED),
				Component.text("Message: ${exception.message ?: "NULL"}", NamedTextColor.RED),
				*exception.stackTrace.map { element -> Component.text(element.toString(), NamedTextColor.RED) }.toTypedArray()
			))
	}
}
