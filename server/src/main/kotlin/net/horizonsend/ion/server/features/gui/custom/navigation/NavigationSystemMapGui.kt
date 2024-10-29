package net.horizonsend.ion.server.features.gui.custom.navigation

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.horizonsend.ion.common.utils.text.SPACE_BACKGROUND_CHARACTER
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItems
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.sidebar.command.BookmarkCommand
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.impl.SimpleItem

class NavigationSystemMapGui(val world: World, val player: Player) {

	private val gui = Gui.empty(9, 6)
	private var planetListShift = 0
	private var beaconListShift = 0
	private var bookmarkListShift = 0

	private var planetsInWorld = 0
	private var beaconsInWorld = 0
	private var bookmarkCount = 0

	private val planetItems = mutableListOf<Item>()
	private val beaconItems = mutableListOf<Item>()
	private val bookmarkItems = mutableListOf<Item>()

	private val leftPlanetButton = GuiItems.CustomControlItem("Previous Planet In This System", GuiItem.LEFT) {
		planetListShift--
		updateGui()
	}

	private val rightPlanetButton = GuiItems.CustomControlItem("Next Planet In This System", GuiItem.RIGHT) {
		planetListShift++
		updateGui()
	}

	private val leftBeaconButton = GuiItems.CustomControlItem("Previous Beacon In This System", GuiItem.LEFT) {
		beaconListShift--
		updateGui()
	}

	private val rightBeaconButton = GuiItems.CustomControlItem("Next Beacon In This System", GuiItem.RIGHT) {
		beaconListShift++
		updateGui()
	}

	private val leftBookmarkButton = GuiItems.CustomControlItem("Previous Bookmark In This System", GuiItem.LEFT) {
		bookmarkListShift--
		updateGui()
	}

	private val rightBookmarkButton = GuiItems.CustomControlItem("Next Bookmark In This System", GuiItem.RIGHT) {
		bookmarkListShift++
		updateGui()
	}

	companion object {
		val ionWorldCache: LoadingCache<WorldFlag, Collection<World>> = CacheBuilder.newBuilder().build(
			CacheLoader.from { worldFlag: WorldFlag ->
				return@from Bukkit.getWorlds().filter { world -> world.ion.hasFlag(worldFlag) }
			}
		)

		private const val MIDDLE_COLUMN = 4
		private const val MAX_ELEMENTS_PER_ROW = 7
		private const val PLANET_ROW = 1
		private const val BEACON_ROW = 2
		private const val BOOKMARK_ROW = 3
	}

	fun createGui(): Gui {

		val star = Space.getStars().firstOrNull { star -> star.spaceWorld == world }
		val starItem = if (star != null) getPlanetItems(star.name) else null

		planetItems.addAll(Space.getPlanets()
			.filter { planet -> planet.spaceWorld == world }
			.sortedBy { planet -> planet.orbitDistance }
			.map { planet -> GuiItems.CustomItemControlItem(getPlanetItems(planet.name)) {
				// TODO: Add click handler here
			} }
		)
		planetsInWorld = planetItems.size

		beaconItems.addAll(IonServer.configuration.beacons
			.filter { beacon -> beacon.spaceLocation.bukkitWorld() == world }
			.sortedBy { beacon -> beacon.name }
			.map { beacon -> GuiItems.CustomControlItem(beacon.name, GuiItem.BEACON) {
				// TODO: Add click handler here
			} }
		)
		beaconsInWorld = beaconItems.size

		bookmarkItems.addAll(BookmarkCommand.getBookmarks(player)
			.filter { bookmark -> bookmark.worldName == world.name }
			.sortedBy { bookmark -> bookmark.name }
			.map { bookmark -> GuiItems.CustomControlItem(bookmark.name, GuiItem.BOOKMARK) {
				// TODO: Add click handler here
			} }
		)
		bookmarkCount = bookmarkItems.size

		if (starItem != null) gui.setItem(MIDDLE_COLUMN, 0, SimpleItem(starItem.constructItemStack()))

		gui.setItem(0, 5, SimpleItem(ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
			it.displayName(Component.text("Return to Galactic Map").decoration(TextDecoration.ITALIC, false))
			it.setCustomModelData(GuiItem.DOWN.customModelData)
		}))

		updateGui()

		return gui
	}

	private fun updateGui() {
		gui.setItem(0, PLANET_ROW, if (planetListShift > 0) leftPlanetButton else GuiItems.EmptyItem())
		gui.setItem(8, PLANET_ROW, if (planetListShift + MAX_ELEMENTS_PER_ROW < planetsInWorld) rightPlanetButton else GuiItems.EmptyItem())
		gui.setItem(0, BEACON_ROW, if (beaconListShift > 0) leftBeaconButton else GuiItems.EmptyItem())
		gui.setItem(8, BEACON_ROW, if (beaconListShift + MAX_ELEMENTS_PER_ROW < beaconsInWorld) rightBeaconButton else GuiItems.EmptyItem())
		gui.setItem(0, BOOKMARK_ROW, if (bookmarkListShift > 0) leftBookmarkButton else GuiItems.EmptyItem())
		gui.setItem(8, BOOKMARK_ROW, if (bookmarkListShift + MAX_ELEMENTS_PER_ROW < bookmarkCount) rightBookmarkButton else GuiItems.EmptyItem())

		for (widthIndex in 0 until planetItems.size.coerceAtMost(MAX_ELEMENTS_PER_ROW)) {
			gui.setItem(widthIndex + 1, PLANET_ROW, planetItems[widthIndex + planetListShift])
		}

		for (widthIndex in 0 until beaconItems.size.coerceAtMost(MAX_ELEMENTS_PER_ROW)) {
			gui.setItem(widthIndex + 1, BEACON_ROW, beaconItems[widthIndex + beaconListShift])
		}

		for (widthIndex in 0 until bookmarkItems.size.coerceAtMost(MAX_ELEMENTS_PER_ROW)) {
			gui.setItem(widthIndex + 1, BOOKMARK_ROW, bookmarkItems[widthIndex + bookmarkListShift])
		}
	}

	fun createText(): Component {
		val header = "${world.name} System Map"
		val guiText = GuiText(header)

		guiText.addBackground(GuiText.GuiBackground(backgroundChar = SPACE_BACKGROUND_CHARACTER))

		return guiText.build()
	}

	/**
	 * Gets the associated custom item from the planet's name.
	 * @return the custom planet icon ItemStack
	 * @param name the name of the planet
	 */
	private fun getPlanetItems(name: String): CustomItem {
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
		}
	}
}
