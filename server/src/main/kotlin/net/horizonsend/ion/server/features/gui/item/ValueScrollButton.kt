package net.horizonsend.ion.server.features.gui.item

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.item.ItemProvider
import java.util.function.Consumer
import java.util.function.Supplier

open class ValueScrollButton(
	providedItem: ItemProvider,
	private val wrap: Boolean,
	protected val value: Supplier<Int>,
	private val increment: Int,
	private val valueRange: IntRange,
	private val valueConsumer: Consumer<Int>
) : FeedbackItem(providedItem, { listOf(Component.text("Current value: ${value.get()}")) }) {
		override fun getResult(event: InventoryClickEvent, player: Player): FeedbackItemResult {
		val nextValueRaw = value.get() + increment
		val range = valueRange.last - valueRange.first

		val formatted = if (wrap) {
			val floorModResult = Math.floorMod(nextValueRaw - valueRange.first, (range + 1))
			floorModResult + valueRange.first
		} else nextValueRaw

		if (!valueRange.contains(formatted)) return FeedbackItemResult.FailureLore(listOf(Component.text("Value $formatted out of range! (${valueRange.first} - ${valueRange.last})", NamedTextColor.RED)))

		valueConsumer.accept(formatted)
		return FeedbackItemResult.SuccessLore(listOf(Component.text("Set value to $formatted", NamedTextColor.GREEN)))
	}

	override fun onSuccess(event: InventoryClickEvent, player: Player) {}
	override fun onFailure(event: InventoryClickEvent, player: Player) {}
}
