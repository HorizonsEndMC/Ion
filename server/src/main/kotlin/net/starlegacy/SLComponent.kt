package net.starlegacy

import net.starlegacy.util.redisaction.RedisAction
import net.starlegacy.util.redisaction.RedisActions
import org.bukkit.event.Listener

abstract class SLComponent : Listener {
	protected val log: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(javaClass)

	init {
		if (INITIALIZATION_COMPLETE) error("Initialized ${this.javaClass.simpleName} after plugin initialization!")
	}

	open fun onEnable() {}

	open fun onDisable() {}

	inline fun <reified T, B> ((T) -> B).registerRedisAction(id: String, runSync: Boolean = true): RedisAction<T> {
		return RedisActions.register(id, runSync, this)
	}

	open fun supportsVanilla(): Boolean = false
}
