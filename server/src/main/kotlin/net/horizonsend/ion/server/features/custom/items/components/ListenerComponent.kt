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
	val eventClass: Class<E>,
	val customItem: T,
	private val eventReceiver: (E, ItemStack) -> Unit
) : CustomItemComponent {
	fun handleEvent(event: E, itemStack: ItemStack) = eventReceiver.invoke(event, itemStack)

	override fun decorateBase(baseItem: ItemStack) {}
	override fun getAttributes(baseItem: ItemStack): Iterable<CustomItemAttribute> = listOf()

	companion object {
		fun <T: NewCustomItem> interactListeners(
			customItem: T,
			handleEvent: (PlayerInteractEvent, ItemStack) -> Unit
		): ListenerComponent<PlayerInteractEvent, T> = ListenerComponent(PlayerInteractEvent::class.java, customItem, handleEvent)

		fun <T: NewCustomItem> playerSwapHandsListener(
			customItem: T,
			handleEvent: (PlayerSwapHandItemsEvent, ItemStack) -> Unit
		): ListenerComponent<PlayerSwapHandItemsEvent, T> = ListenerComponent(PlayerSwapHandItemsEvent::class.java, customItem, handleEvent)

		fun <T: NewCustomItem> dispenseListener(
			customItem: T,
			handleEvent: (BlockPreDispenseEvent, ItemStack) -> Unit
		): ListenerComponent<BlockPreDispenseEvent, T> = ListenerComponent(BlockPreDispenseEvent::class.java, customItem, handleEvent)

		fun <T: NewCustomItem> entityShootBowListener(
			customItem: T,
			handleEvent: (EntityShootBowEvent, ItemStack) -> Unit
		): ListenerComponent<EntityShootBowEvent, T> = ListenerComponent(EntityShootBowEvent::class.java, customItem, handleEvent)
	}
}
