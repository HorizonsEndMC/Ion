package net.horizonsend.ion.server.features.custom.items.components

import io.papermc.paper.event.block.BlockPreDispenseEvent
import net.horizonsend.ion.server.features.custom.NewCustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack

class ListenerComponent<E: Event, T: NewCustomItem>(
	val customItem: T,
	private val eventReceiver: (E, T, ItemStack) -> Unit
) : CustomItemComponent {
	fun handleEvent(event: E, itemStack: ItemStack) = eventReceiver.invoke(event, customItem, itemStack)

	override fun decorateBase(baseItem: ItemStack) {}
	override fun getAttributes(baseItem: ItemStack): Iterable<CustomItemAttribute> = listOf()

	companion object {
		fun <T: NewCustomItem> interactListener(
			customItem: T,
			handleEvent: (PlayerInteractEvent, T, ItemStack) -> Unit
		): ListenerComponent<PlayerInteractEvent, T> = ListenerComponent(customItem, handleEvent)

		fun <T: NewCustomItem> playerSwapHandsListener(
			customItem: T,
			handleEvent: (PlayerSwapHandItemsEvent, T, ItemStack) -> Unit
		): ListenerComponent<PlayerSwapHandItemsEvent, T> = ListenerComponent(customItem, handleEvent)

		fun <T: NewCustomItem> dispenseListener(
			customItem: T,
			handleEvent: (BlockPreDispenseEvent, T, ItemStack) -> Unit
		): ListenerComponent<BlockPreDispenseEvent, T> = ListenerComponent(customItem, handleEvent)

		fun <T: NewCustomItem> entityShootBowListener(
			customItem: T,
			handleEvent: (EntityShootBowEvent, T, ItemStack) -> Unit
		): ListenerComponent<EntityShootBowEvent, T> = ListenerComponent(customItem, handleEvent)
	}
}
