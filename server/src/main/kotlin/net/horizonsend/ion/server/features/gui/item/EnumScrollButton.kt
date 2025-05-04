package net.horizonsend.ion.server.features.gui.item

import net.horizonsend.ion.common.utils.InputResult
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.miscellaneous.utils.map
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.item.ItemProvider
import java.util.function.Consumer
import java.util.function.Supplier

class EnumScrollButton<T : Enum<T>>(
	providedItem: ItemProvider,
	increment: Int,
	value: Supplier<T>,
	private val enum: Class<T>,
	val nameFormatter: (T) -> Component,
	private val subEntry: Array<T> = enum.enumConstants,
	valueConsumer: Consumer<T>
) : ValueScrollButton(providedItem, true, value.map { it.ordinal }, increment, 0..subEntry.lastIndex, { valueConsumer.accept(subEntry[it]) }) {
	override var currentLore: Supplier<List<Component>> = Supplier { listOf(ofChildren(Component.text("Current value: "), nameFormatter.invoke(value.get()))) }

	override fun getResult(event: InventoryClickEvent, player: Player): InputResult {
		val parentResult = super.getResult(event, player)

		if (parentResult.isSuccess()) {
			val newEntry = value.get()
			val enumEntry = subEntry[newEntry]
			return InputResult.SuccessReason(listOf(ofChildren(Component.text("Set value to ", NamedTextColor.GREEN), nameFormatter(enumEntry))))
		}

		return parentResult
	}
}
