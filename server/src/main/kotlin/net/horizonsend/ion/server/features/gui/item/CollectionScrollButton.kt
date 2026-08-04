package net.horizonsend.ion.server.features.gui.item

import net.horizonsend.ion.common.utils.input.InputResult
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.item.ItemProvider
import java.util.function.BiConsumer
import java.util.function.Supplier

class CollectionScrollButton<T : Any?>(
	private val entries: List<T>,
	providedItem: ItemProvider,
	value: Supplier<Int>,
	val nameFormatter: (T) -> Component,
	valueConsumer: BiConsumer<Int, T>
) : ValueScrollButton(providedItem, true, value, 1, 0..entries.size, { valueConsumer.accept(it, entries[it]) }) {
	override var currentLore: Supplier<List<Component>> =
		Supplier { listOf(ofChildren(Component.text("Current value: "), nameFormatter.invoke(entries[value.get()]))) }

	override fun getResult(event: InventoryClickEvent, player: Player): InputResult {
		val parentResult = super.getResult(event, player)

		if (parentResult.isSuccess()) {
			val nextEntry = entries[value.get()]
			return InputResult.SuccessReason(listOf(template(Component.text("Set value to {0}", NamedTextColor.GREEN), nameFormatter(nextEntry))))
		}

		return parentResult
	}
}
