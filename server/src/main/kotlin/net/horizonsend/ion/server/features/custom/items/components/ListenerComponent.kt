package net.horizonsend.ion.server.features.custom.items.components

import io.papermc.paper.event.block.BlockPreDispenseEvent
import net.horizonsend.ion.server.features.custom.NewCustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

class ListenerComponent<E: Event, T: NewCustomItem>(
	val customItem: T,
	val eventType: KClass<out E>,
	private val eventReceiver: (E, T, ItemStack) -> Unit
) : CustomItemComponent {
	fun handleEvent(event: E, itemStack: ItemStack) = eventReceiver.invoke(event, customItem, itemStack)

	override fun decorateBase(baseItem: ItemStack) {}
	override fun getAttributes(baseItem: ItemStack): Iterable<CustomItemAttribute> = listOf()

	companion object {
		inline fun <reified T: NewCustomItem> interactListener(
			customItem: T,
			noinline handleEvent: (PlayerInteractEvent, T, ItemStack) -> Unit
		): ListenerComponent<PlayerInteractEvent, T> = ListenerComponent(customItem, PlayerInteractEvent::class, handleEvent)

		inline fun <reified T: NewCustomItem> playerSwapHandsListener(
			customItem: T,
			noinline handleEvent: (PlayerSwapHandItemsEvent, T, ItemStack) -> Unit
		): ListenerComponent<PlayerSwapHandItemsEvent, T> = ListenerComponent(customItem, PlayerSwapHandItemsEvent::class, handleEvent)

		inline fun <reified T: NewCustomItem> dispenseListener(
			customItem: T,
			noinline handleEvent: (BlockPreDispenseEvent, T, ItemStack) -> Unit
		): ListenerComponent<BlockPreDispenseEvent, T> = ListenerComponent(customItem, BlockPreDispenseEvent::class, handleEvent)

		inline fun <reified T: NewCustomItem> entityShootBowListener(
			customItem: T,
			noinline handleEvent: (EntityShootBowEvent, T, ItemStack) -> Unit
		): ListenerComponent<EntityShootBowEvent, T> = ListenerComponent(customItem, EntityShootBowEvent::class, handleEvent)
	}
}
