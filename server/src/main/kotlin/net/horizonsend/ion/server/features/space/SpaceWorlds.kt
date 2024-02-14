package net.horizonsend.ion.server.features.space

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.World
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.event.world.WorldUnloadEvent

@Deprecated("Monolithic space worlds are being phased out", replaceWith = ReplaceWith("WorldFlag", "net.horizonsend.ion.server.features.world.WorldFlag"))
object SpaceWorlds : IonServerComponent() {
	val cache: LoadingCache<World, Boolean> = CacheBuilder.newBuilder()
		.weakKeys()
		.build(CacheLoader.from cache@{ world ->
			if (world == null) return@cache false

			return@cache world.ion.hasFlag(WorldFlag.SPACE_WORLD)
		})

	override fun onEnable() {
		listen<WorldLoadEvent> { event -> cache.get(event.world) }
		listen<WorldUnloadEvent> { event -> cache.invalidate(event.world) }
	}

	@Deprecated("Monolithic space worlds are being phased out", replaceWith = ReplaceWith("world.ion.hasFlag()"))
	fun contains(world: World): Boolean = cache.get(world)

	override fun onDisable() {
		with(cache) { invalidateAll(); cleanUp() }
	}
}
