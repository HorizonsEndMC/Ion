package net.horizonsend.ion.server.features.gui.item

import net.horizonsend.ion.common.utils.text.ofChildren
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer
import java.util.function.Supplier

class EnumScrollButton<T : Enum<T>>(
	item: ItemStack,
	increment: Int,
	value: Supplier<Int>,
	private val enum: Class<T>,
	val nameFormatter: (T) -> Component,
	valueConsumer: Consumer<Int>
) : ValueScrollButton(item, true, value, increment, 0..enum.enumConstants.lastIndex, valueConsumer) {
	override fun getResult(event: InventoryClickEvent, player: Player): FeedbackItemResult {
		val parentResult = super.getResult(event, player)

		if (parentResult.success) {
			val newEntry = value.get()
			val enumEntry = enum.enumConstants[newEntry]
			return FeedbackItemResult.SuccessLore(listOf(ofChildren(Component.text("Set value to ", NamedTextColor.GREEN), nameFormatter(enumEntry))))
		}

		return parentResult
	}
}
