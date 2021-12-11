package net.starlegacy

import net.starlegacy.util.redisaction.RedisAction
import net.starlegacy.util.redisaction.RedisActions
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

abstract class SLComponent : Listener {
    protected val log: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(javaClass)

    protected val plugin get() = PLUGIN

    init {
        if (StarLegacy.INITIALIZATION_COMPLETE) {
            error("Initialized ${this.javaClass.simpleName} after plugin initialization!")
        }
    }

    open fun onEnable() {}

    open fun onDisable() {}

    protected inline fun <reified T : Event> subscribe(
        priority: EventPriority = EventPriority.NORMAL,
        ignoreCancelled: Boolean = false,
        noinline block: (T) -> Unit
    ): Unit = plugin.listen(priority, ignoreCancelled, block)

    protected inline fun <reified T : Event> subscribe(
        priority: EventPriority = EventPriority.NORMAL,
        ignoreCancelled: Boolean = false,
        noinline block: (Listener, T) -> Unit
    ): Unit = plugin.listen(priority, ignoreCancelled, block)

    inline fun <reified T, B> ((T) -> B).registerRedisAction(id: String, runSync: Boolean = true): RedisAction<T> {
        return RedisActions.register(id, runSync, this)
    }

    open fun supportsVanilla(): Boolean = false
}
