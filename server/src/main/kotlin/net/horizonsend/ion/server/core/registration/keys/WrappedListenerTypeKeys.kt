package net.horizonsend.ion.server.core.registration.keys

import net.horizonsend.ion.server.core.registration.keys.RegistryKeys.WRAPPED_LISTENER_TYPE
import net.horizonsend.ion.server.features.world.environment.listener.WrappedListenerType
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.player.PlayerMoveEvent

object WrappedListenerTypeKeys : KeyRegistry<WrappedListenerType<*>>(WRAPPED_LISTENER_TYPE, WrappedListenerType::class) {
	val ITEM_SPAWN_EVENT = registerTypedKey<WrappedListenerType<ItemSpawnEvent>>("ITEM_SPAWN_EVENT")
	val PLAYER_MOVE_EVENT = registerTypedKey<WrappedListenerType<PlayerMoveEvent>>("PLAYER_MOVE_EVENT")
	val ENTITY_CHANGE_BLOCK = registerTypedKey<WrappedListenerType<EntityChangeBlockEvent>>("ENTITY_CHANGE_BLOCK")
}
