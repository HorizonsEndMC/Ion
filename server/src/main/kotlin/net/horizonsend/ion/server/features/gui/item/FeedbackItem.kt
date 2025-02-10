package net.horizonsend.ion.server.features.gui.item

import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem

abstract class FeedbackItem(
	val itemStack: ItemStack,
	fallbackLore: List<Component>
) : AbstractItem() {
	private var currentLore = fallbackLore

	private val itemProvider = ItemProvider {
		itemStack.clone().updateLore(currentLore)
	}

	override fun getItemProvider(): ItemProvider = itemProvider

	override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
		val result = getResult(event, player)

		if (result.success) onSuccess(event, player) else onFailure(event, player)
		updateWith(result)
	}

	abstract fun getResult(event: InventoryClickEvent, player: Player): FeedbackItemResult

	abstract fun onSuccess(event: InventoryClickEvent, player: Player)
	abstract fun onFailure(event: InventoryClickEvent, player: Player)

	private fun updateWith(result: FeedbackItemResult) {
		currentLore = when (result) {
			is FeedbackItemResult.SuccessLore -> result.lore
			is FeedbackItemResult.FailureLore -> result.lore
			else -> return
		}

		notifyWindows()
	}

	sealed interface FeedbackItemResult {
		val success: Boolean

		data class SuccessLore(val lore: List<Component>) : FeedbackItemResult {
			override val success: Boolean = true
		}

		data object Success : FeedbackItemResult {
			override val success: Boolean = true
		}

		data class FailureLore(val lore: List<Component>) : FeedbackItemResult {
			override val success: Boolean = false
		}

		data object Failure : FeedbackItemResult {
			override val success: Boolean = false
		}
	}

	companion object {
		fun builder(itemStack: ItemStack, resultProvier: (InventoryClickEvent, Player) -> FeedbackItemResult): Builder = Builder(itemStack, resultProvier)
	}

	class Builder(val itemStack: ItemStack, val resultProvier: (InventoryClickEvent, Player) -> FeedbackItemResult) {
		var fallbackLore: List<Component> = listOf()
		var onSuccess: ((InventoryClickEvent, Player) -> Unit)? = null
		var onFailure: ((InventoryClickEvent, Player) -> Unit)? = null

		fun build(): FeedbackItem = object : FeedbackItem(itemStack, fallbackLore) {
			override fun getResult(event: InventoryClickEvent, player: Player): FeedbackItemResult {
				return resultProvier.invoke(event, player)
			}

			override fun onSuccess(event: InventoryClickEvent, player: Player) { onSuccess?.invoke(event, player) }

			override fun onFailure(event: InventoryClickEvent, player: Player) { onFailure?.invoke(event, player) }
		}

		fun withSuccessHandler(function: (InventoryClickEvent, Player) -> Unit): Builder {
			onSuccess = function
			return this
		}

		fun withFailureHandler(function: (InventoryClickEvent, Player) -> Unit): Builder {
			onFailure = function
			return this
		}

		fun withFallbackLore(lore: List<Component>): Builder {
			fallbackLore = lore
			return this
		}
	}
}
