package net.horizonsend.ion.server.miscellaneous.utils

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import org.bukkit.World

class PerWorld<T : Any>(newT: (World) -> T) {
	val cache = CacheBuilder.newBuilder()
		.weakKeys()
		.build<World, T>(CacheLoader.from { world -> newT(world!!) })

	operator fun get(world: World): T = cache[world]
}
