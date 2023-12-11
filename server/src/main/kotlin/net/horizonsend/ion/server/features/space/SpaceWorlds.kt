package net.horizonsend.ion.server.features.space

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.World
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.event.world.WorldUnloadEvent
import java.io.File

object SpaceWorlds : IonServerComponent() {
	private fun getSpaceFlagFile(world: World) = File(world.worldFolder, "data/starlegacy/space.flag")

	private val cache: LoadingCache<World, Boolean> = CacheBuilder.newBuilder()
		.weakKeys()
		.build(CacheLoader.from { world -> world != null && getSpaceFlagFile(world).exists() })

	override fun onEnable() {
		listen<WorldLoadEvent> { event -> cache.get(event.world) }
		listen<WorldUnloadEvent> { event -> cache.invalidate(event.world) }
	}

	fun setSpaceWorld(world: World, space: Boolean) {
		if (space) {
			getSpaceFlagFile(world).apply {
				parentFile.mkdirs()
				createNewFile()
			}
		} else {
			getSpaceFlagFile(world).apply {
				if (exists()) {
					delete()
				}
			}
		}

		cache.invalidate(world)
	}

	fun contains(world: World): Boolean = cache.get(world)

	fun all(): Set<World> = cache.getAll(IonServer.server.worlds).keys

	override fun onDisable() {
		with(cache) { invalidateAll(); cleanUp() }
	}
}
