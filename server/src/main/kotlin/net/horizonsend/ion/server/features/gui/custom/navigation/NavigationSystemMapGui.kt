package net.horizonsend.ion.server.features.gui.custom.navigation

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.horizonsend.ion.server.features.custom.items.CustomItems
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import org.bukkit.Bukkit
import org.bukkit.World
import xyz.xenondevs.invui.gui.ScrollGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.impl.SimpleItem

class NavigationSystemMapGui(val world: World) {
	fun createGui(): ScrollGui<Item> {
		val gui = ScrollGui.items()

		gui.setStructure(
			"x x x x x x x x x"
		)

		gui.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
		gui.setContent(Space.getPlanets().filter { planet -> planet.spaceWorld == world }.map { SimpleItem(CustomItems.AERACH.constructItemStack()) })
		return gui.build()
	}

	companion object {
		val ionWorldCache: LoadingCache<WorldFlag, Collection<World>> = CacheBuilder.newBuilder().build(
			CacheLoader.from { worldFlag: WorldFlag ->
				return@from Bukkit.getWorlds().filter { world -> world.ion.hasFlag(worldFlag) }
			}
		)
	}
}
