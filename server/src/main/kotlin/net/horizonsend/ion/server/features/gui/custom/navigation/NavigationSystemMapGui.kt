package net.horizonsend.ion.server.features.gui.custom.navigation

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.custom.items.CustomItems
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.impl.SimpleItem

class NavigationSystemMapGui(val world: World) {
	fun createGui(): Gui {
		val gui = Gui.empty(9, 5)

		val star = Space.getStars().firstOrNull { star -> star.spaceWorld == world }
		val starItem = if (star != null) getItemStack(star.name) else ItemStack(Material.AIR)
		val planetList = Space.getPlanets()
			.filter { planet -> planet.spaceWorld == world }
			.map { planet -> getItemStack(planet.name) }
		val beaconList = IonServer.configuration.beacons
			.filter { beacon -> beacon.spaceLocation.bukkitWorld() == world }
			.map { beacon -> ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { item ->
				item.setCustomModelData(GuiItem.BEACON.customModelData)
				item.displayName(Component.text(beacon.name).decoration(TextDecoration.ITALIC, false))
			}}

		gui.setItem(0, 1, SimpleItem(starItem))
		for (widthIndex in 0 until planetList.size.coerceAtMost(7)) {
			gui.setItem(widthIndex + 2, 1, SimpleItem(planetList[widthIndex]))
		}
		for (widthIndex in 0 until beaconList.size.coerceAtMost(7)) {
			gui.setItem(widthIndex + 2, 2, SimpleItem(beaconList[widthIndex]))
		}

		gui.setItem(0, 4, SimpleItem(ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
			it.displayName(Component.text("Return to Galactic Map").decoration(TextDecoration.ITALIC, false))
			it.setCustomModelData(GuiItem.DOWN.customModelData)
		}))

		return gui
	}

	fun createText(): Component {
		val header = "${world.name} System Map"
		val guiText = GuiText(header)

		return guiText.build()
	}

	/**
	 * Gets the associated custom item from the planet's name.
	 * @return the custom planet icon ItemStack
	 * @param name the name of the planet
	 */
	private fun getItemStack(name: String): ItemStack {
		return when (name) {
			"Aerach" -> CustomItems.AERACH
			"Aret" -> CustomItems.ARET
			"Chandra" -> CustomItems.CHANDRA
			"Chimgara" -> CustomItems.CHIMGARA
			"Damkoth" -> CustomItems.DAMKOTH
			"Disterra" -> CustomItems.DISTERRA
			"Eden" -> CustomItems.EDEN
			"Gahara" -> CustomItems.GAHARA
			"Herdoli" -> CustomItems.HERDOLI
			"Ilius" -> CustomItems.ILIUS
			"Isik" -> CustomItems.ISIK
			"Kovfefe" -> CustomItems.KOVFEFE
			"Krio" -> CustomItems.KRIO
			"Lioda" -> CustomItems.LIODA
			"Luxiterna" -> CustomItems.LUXITERNA
			"Qatra" -> CustomItems.QATRA
			"Rubaciea" -> CustomItems.RUBACIEA
			"Turms" -> CustomItems.TURMS
			"Vask" -> CustomItems.VASK

			"Asteri" -> CustomItems.ASTERI
			"EdenHack" -> CustomItems.HORIZON
			"Ilios" -> CustomItems.ILIOS
			"Regulus" -> CustomItems.REGULUS
			"Sirius" -> CustomItems.SIRIUS


			else -> CustomItems.AERACH
		}.constructItemStack()
	}

	companion object {
		val ionWorldCache: LoadingCache<WorldFlag, Collection<World>> = CacheBuilder.newBuilder().build(
			CacheLoader.from { worldFlag: WorldFlag ->
				return@from Bukkit.getWorlds().filter { world -> world.ion.hasFlag(worldFlag) }
			}
		)
	}
}
