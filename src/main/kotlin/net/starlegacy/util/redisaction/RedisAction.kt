package net.starlegacy.util.redisaction

import net.starlegacy.util.Tasks
import org.bukkit.Bukkit
import java.lang.reflect.Type

abstract class RedisAction<Data>(val id: String, val type: Type, val runSync: Boolean) {
    abstract fun onReceive(data: Data)

    // can't actually cast it outside of this class in the redis pub sub listener so we have to do it here.
    // ugly, but it works
    @Suppress("UNCHECKED_CAST")
    internal fun castAndReceive(data: Any): Unit = onReceive(data as Data)

    operator fun invoke(data: Data) {
        if (Bukkit.isPrimaryThread() || !runSync) {
            onReceive(data)
        } else {
            Tasks.sync { onReceive(data) }
        }

        RedisActions.publish(id, data, type)
    }
}
