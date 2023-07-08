package net.starlegacy.feature.space

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.horizonsend.ion.server.IonComponent
import net.starlegacy.listen
import org.bukkit.World
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.event.world.WorldUnloadEvent
import java.io.File

object SpaceWorlds : IonComponent() {
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

	override fun onDisable() {
		with(cache) { invalidateAll(); cleanUp() }
	}

	override fun supportsVanilla(): Boolean {
		return true
	}
}
