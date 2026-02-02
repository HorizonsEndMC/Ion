package net.horizonsend.ion.server.core.registration.registries

import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys
import net.horizonsend.ion.server.core.registration.keys.WrappedListenerTypeKeys
import net.horizonsend.ion.server.features.world.environment.listener.WrappedListenerType
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.player.PlayerMoveEvent

class WrappedListenerRegistry : Registry<WrappedListenerType<*>>(RegistryKeys.WRAPPED_LISTENER_TYPE) {
	override fun getKeySet(): KeyRegistry<WrappedListenerType<*>> = WrappedListenerTypeKeys

	override fun boostrap() {
		register(WrappedListenerTypeKeys.ITEM_SPAWN_EVENT, WrappedListenerType<ItemSpawnEvent>(WrappedListenerTypeKeys.ITEM_SPAWN_EVENT, ItemSpawnEvent::class, ItemSpawnEvent.getHandlerList(), EventPriority.NORMAL, false))
		register(WrappedListenerTypeKeys.PLAYER_MOVE_EVENT, WrappedListenerType<PlayerMoveEvent>(WrappedListenerTypeKeys.PLAYER_MOVE_EVENT, PlayerMoveEvent::class, PlayerMoveEvent.getHandlerList(), EventPriority.NORMAL, false))
		register(WrappedListenerTypeKeys.ENTITY_CHANGE_BLOCK, WrappedListenerType<EntityChangeBlockEvent>(WrappedListenerTypeKeys.ENTITY_CHANGE_BLOCK, EntityChangeBlockEvent::class, EntityChangeBlockEvent.getHandlerList(), EventPriority.NORMAL, false))
	}
}
