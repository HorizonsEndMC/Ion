package net.horizonsend.ion.server.features.gui.item

import net.horizonsend.ion.common.utils.InputResult
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import java.util.function.Supplier

abstract class FeedbackItem(
	private val providedItem: ItemProvider,
	private val fallbackLoreProvider: Supplier<List<Component>>
) : AbstractItem() {
	protected open var currentLore = fallbackLoreProvider

	fun resetLore() {
		currentLore = fallbackLoreProvider
	}

	private val itemProvider = ItemProvider {
		providedItem.get().updateLore(currentLore.get())
	}

	override fun getItemProvider(): ItemProvider = itemProvider

	override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
		val result = getResult(event, player)

		if (result.isSuccess()) onSuccess(event, player) else onFailure(event, player)
		updateWith(result)
	}

	abstract fun getResult(event: InventoryClickEvent, player: Player): InputResult

	abstract fun onSuccess(event: InventoryClickEvent, player: Player)
	abstract fun onFailure(event: InventoryClickEvent, player: Player)

	private fun updateWith(result: InputResult) {
		currentLore = when (result) {
			is InputResult.SuccessReason -> Supplier { result.reasonText }
			is InputResult.FailureReason -> Supplier { result.reasonText }
			else -> return
		}

		notifyWindows()
	}

	companion object {
		fun builder(itemStack: ItemStack, resultProvier: (InventoryClickEvent, Player) -> InputResult): Builder = Builder({ itemStack }, resultProvier)
		fun builder(itemStack: ItemProvider, resultProvier: (InventoryClickEvent, Player) -> InputResult): Builder = Builder(itemStack, resultProvier)
	}

	class Builder(val providedItem: ItemProvider, val resultProvier: (InventoryClickEvent, Player) -> InputResult) {
		private var fallbackLore: List<Component> = listOf()
		private var onSuccess: (FeedbackItem.(InventoryClickEvent, Player) -> Unit)? = null
		private var onFailure: (FeedbackItem.(InventoryClickEvent, Player) -> Unit)? = null

		fun build(): FeedbackItem = object : FeedbackItem(providedItem, { fallbackLore }) {
			override fun getResult(event: InventoryClickEvent, player: Player): InputResult {
				return resultProvier.invoke(event, player)
			}

			override fun onSuccess(event: InventoryClickEvent, player: Player) { onSuccess?.invoke(this, event, player) }

			override fun onFailure(event: InventoryClickEvent, player: Player) { onFailure?.invoke(this, event, player) }
		}

		fun withSuccessHandler(function: FeedbackItem.(InventoryClickEvent, Player) -> Unit): Builder {
			onSuccess = function
			return this
		}

		fun withFailureHandler(function: FeedbackItem.(InventoryClickEvent, Player) -> Unit): Builder {
			onFailure = function
			return this
		}

		fun withStaticFallbackLore(lore: List<Component>): Builder {
			fallbackLore = lore
			return this
		}
	}
}
