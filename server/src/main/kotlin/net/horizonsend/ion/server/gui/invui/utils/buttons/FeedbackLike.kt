package net.horizonsend.ion.server.gui.invui.utils.buttons

import net.horizonsend.ion.common.utils.InputResult
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import java.util.function.Supplier

abstract class FeedbackLike(
	protected val providedItem: ItemProvider,
	private val fallbackLoreProvider: Supplier<List<Component>>,
) : AbstractItem() {
	protected open var currentLore = fallbackLoreProvider

	fun resetLore() {
		currentLore = fallbackLoreProvider
	}

	private val itemProvider = ItemProvider {
		providedItem.get().updateLore(currentLore.get())
	}

	override fun getItemProvider(): ItemProvider {
		return itemProvider
	}

	fun updateWith(result: InputResult) {
		currentLore = when (result) {
			is InputResult.SuccessReason -> Supplier { result.reasonText }
			is InputResult.FailureReason -> Supplier { result.reasonText }
			else -> return
		}

		notifyWindows()
	}

	companion object {
		fun withHandler(
			providedItem: ItemProvider,
			fallbackLoreProvider: Supplier<List<Component>>,
			clickHandler: (ClickType, Player) -> Unit,
		) = object : FeedbackLike(providedItem, fallbackLoreProvider) {
			override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
				clickHandler.invoke(clickType, player)
			}
		}
	}
}
