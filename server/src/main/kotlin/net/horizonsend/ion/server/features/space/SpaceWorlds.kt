package net.horizonsend.ion.server.features.space

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.IonWorld
import net.horizonsend.ion.server.miscellaneous.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.World
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.event.world.WorldUnloadEvent

object SpaceWorlds : IonServerComponent() {
	val cache: LoadingCache<World, Boolean> = CacheBuilder.newBuilder()
		.weakKeys()
		.build(CacheLoader.from cache@{ world ->
			if (world == null) return@cache false

			return@cache world.ion().hasFlag(IonWorld.WorldFlag.SPACE_ENVIRONMENT)
		})

	override fun onEnable() {
		listen<WorldLoadEvent> { event -> cache.get(event.world) }
		listen<WorldUnloadEvent> { event -> cache.invalidate(event.world) }
	}

	fun contains(world: World): Boolean = cache.get(world)

	fun all(): Set<World> = cache.getAll(IonServer.server.worlds).keys

	override fun onDisable() {
		with(cache) { invalidateAll(); cleanUp() }
	}
}
