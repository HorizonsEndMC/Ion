package net.horizonsend.ion.server.features.gui.custom.navigation

import net.horizonsend.ion.common.utils.text.SPACE_BLUE_NEBULA_CHARACTER
import net.horizonsend.ion.common.utils.text.SPACE_RED_NEBULA_CHARACTER
import net.horizonsend.ion.common.utils.text.SPACE_SCREEN_CHARACTER
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.sidebar.command.BookmarkCommand
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window

class NavigationSystemMapGui(val player: Player, val world: World) {

	private var currentWindow: Window? = null
	private val gui = Gui.empty(9, 6)

	// stores amount of shift per horizontal scroller
	private var planetListShift = 0
	private var beaconListShift = 0
	private var bookmarkListShift = 0

	// stores quantity of objects
	private var planetsInWorld = 0
	private var beaconsInWorld = 0
	private var bookmarkCount = 0

	// stores gui item of each object
	private val planetItems = mutableListOf<Item>()
	private val beaconItems = mutableListOf<Item>()
	private val bookmarkItems = mutableListOf<Item>()

	// objects for the left/right buttons
	private val leftPlanetButton = GuiItems.CustomControlItem(Component.text("Previous Planet In This System")
		.decoration(TextDecoration.ITALIC, false), GuiItem.LEFT) {
		_: ClickType, _: Player, _: InventoryClickEvent ->
		planetListShift--
		updateGuiShift()
	}

	private val rightPlanetButton = GuiItems.CustomControlItem(Component.text("Next Planet In This System")
		.decoration(TextDecoration.ITALIC, false), GuiItem.RIGHT) {
		_: ClickType, _: Player, _: InventoryClickEvent ->
		planetListShift++
		updateGuiShift()
	}

	private val leftBeaconButton = GuiItems.CustomControlItem(Component.text("Previous Beacon In This System")
		.decoration(TextDecoration.ITALIC, false), GuiItem.LEFT) {
		_: ClickType, _: Player, _: InventoryClickEvent ->
		beaconListShift--
		updateGuiShift()
	}

	private val rightBeaconButton = GuiItems.CustomControlItem(Component.text("Next Beacon In This System")
		.decoration(TextDecoration.ITALIC, false), GuiItem.RIGHT) {
		_: ClickType, _: Player, _: InventoryClickEvent ->
		beaconListShift++
		updateGuiShift()
	}

	private val leftBookmarkButton = GuiItems.CustomControlItem(Component.text("Previous Bookmark In This System")
		.decoration(TextDecoration.ITALIC, false), GuiItem.LEFT) {
		_: ClickType, _: Player, _: InventoryClickEvent ->
		bookmarkListShift--
		updateGuiShift()
	}

	private val rightBookmarkButton = GuiItems.CustomControlItem(Component.text("Next Bookmark In This System")
		.decoration(TextDecoration.ITALIC, false), GuiItem.RIGHT) {
		_: ClickType, _: Player, _: InventoryClickEvent ->
		bookmarkListShift++
		updateGuiShift()
	}

	companion object {

		private const val MIDDLE_COLUMN = 4
		private const val MAX_ELEMENTS_PER_ROW = 7
		private const val PLANET_ROW = 1
		private const val BEACON_ROW = 2
		private const val BOOKMARK_ROW = 3
		private const val MENU_ROW = 5
	}

	/**
	 * Initializes the Navigation (System) GUI
	 */
	private fun createGui(): Gui {

		val star = Space.getStars().firstOrNull { star -> star.spaceWorld == world }
		val starItem = if (star != null) NavigationGuiCommon.createStarCustomControlItem(star, player, world, gui) { NavigationSystemMapGui(player, world).openMainWindow() } else null

		planetItems.addAll(Space.getOrbitingPlanets()
			.filter { planet -> planet.spaceWorld == world }
			.sortedBy { planet -> planet.orbitDistance }
			.map { planet -> NavigationGuiCommon.createPlanetCustomControlItem(planet, player, world, gui) { NavigationSystemMapGui(player, world).openMainWindow() } }
		)
		planetsInWorld = planetItems.size

		beaconItems.addAll(ConfigurationFiles.serverConfiguration().beacons
			.filter { beacon -> beacon.spaceLocation.bukkitWorld() == world }
			.sortedBy { beacon -> beacon.name }
			.map { beacon -> NavigationGuiCommon.createBeaconCustomControlItem(beacon, player, world, gui) }
		)
		beaconsInWorld = beaconItems.size

		bookmarkItems.addAll(BookmarkCommand.getBookmarks(player)
			.filter { bookmark -> bookmark.worldName == world.name }
			.sortedBy { bookmark -> bookmark.name }
			.map { bookmark -> NavigationGuiCommon.createBookmarkCustomControlItem(bookmark, player, world, gui) }
		)
		bookmarkCount = bookmarkItems.size

		if (starItem != null) gui.setItem(MIDDLE_COLUMN, 0, starItem)

		gui.setItem(0, MENU_ROW, GuiItems.closeMenuItem(player))

		gui.setItem(1, MENU_ROW, GuiItems.CustomControlItem(Component.text("Return To Galactic Menu").decoration(TextDecoration.ITALIC, false), GuiItem.DOWN) {
			_: ClickType, _: Player, _: InventoryClickEvent -> NavigationGalacticMapGui(player).openMainWindow()
		})

		gui.setItem(2, MENU_ROW, GuiItems.CustomControlItem(Component.text("Search For Destination").decoration(TextDecoration.ITALIC, false), GuiItem.MAGNIFYING_GLASS) {
			_: ClickType, player: Player, _: InventoryClickEvent ->
			NavigationGuiCommon.openSearchMenu(player, world, gui) {
				// create a new instance and do not use this old instance
				NavigationSystemMapGui(player, world).openMainWindow()
			}
		})

		updateGuiShift()
		NavigationGuiCommon.updateGuiRoute(gui, player)

		return gui
	}

	private fun open(player: Player): Window {
		val gui = createGui()

		val window = Window.single()
				.setViewer(player)
				.setGui(gui)
				.setTitle(AdventureComponentWrapper(createText()))
				.build()

		return window
	}

	fun openMainWindow() {
		currentWindow = open(player).apply { open() }
	}


	/**
	 * Updates the GUI shifts every time something changes (such as the player clicking a button)
	 */
	private fun updateGuiShift() {
		// populate arrows
		gui.setItem(0, PLANET_ROW, if (planetListShift > 0) leftPlanetButton else GuiItems.EmptyItem())
		gui.setItem(8, PLANET_ROW, if ((planetListShift + 1) * MAX_ELEMENTS_PER_ROW < planetsInWorld) rightPlanetButton else GuiItems.EmptyItem())
		gui.setItem(0, BEACON_ROW, if (beaconListShift > 0) leftBeaconButton else GuiItems.EmptyItem())
		gui.setItem(8, BEACON_ROW, if ((beaconListShift + 1) * MAX_ELEMENTS_PER_ROW < beaconsInWorld) rightBeaconButton else GuiItems.EmptyItem())
		gui.setItem(0, BOOKMARK_ROW, if (bookmarkListShift > 0) leftBookmarkButton else GuiItems.EmptyItem())
		gui.setItem(8, BOOKMARK_ROW, if ((bookmarkListShift + 1) * MAX_ELEMENTS_PER_ROW < bookmarkCount) rightBookmarkButton else GuiItems.EmptyItem())

		// populate buttons; ensure that index of items will not be out of bounds first
		for (widthIndex in 0 until MAX_ELEMENTS_PER_ROW) {
			if (planetItems.size > widthIndex + (planetListShift * MAX_ELEMENTS_PER_ROW)) {
				gui.setItem(widthIndex + 1, PLANET_ROW, planetItems[widthIndex + (planetListShift * MAX_ELEMENTS_PER_ROW)])
			} else gui.setItem(widthIndex + 1, PLANET_ROW, GuiItems.EmptyItem())
		}

		for (widthIndex in 0 until MAX_ELEMENTS_PER_ROW) {
			if (beaconItems.size > widthIndex + (beaconListShift * MAX_ELEMENTS_PER_ROW)) {
				gui.setItem(widthIndex + 1, BEACON_ROW, beaconItems[widthIndex + (beaconListShift * MAX_ELEMENTS_PER_ROW)])
			} else gui.setItem(widthIndex + 1, BEACON_ROW, GuiItems.EmptyItem())
		}

		for (widthIndex in 0 until MAX_ELEMENTS_PER_ROW) {
			if (bookmarkItems.size > widthIndex + (bookmarkListShift * MAX_ELEMENTS_PER_ROW)) {
				gui.setItem(widthIndex + 1, BOOKMARK_ROW, bookmarkItems[widthIndex + (bookmarkListShift * MAX_ELEMENTS_PER_ROW)])
			} else gui.setItem(widthIndex + 1, BOOKMARK_ROW, GuiItems.EmptyItem())
		}
	}

	/**
	 * Creates the GUI text and background
	 */
	private fun createText(): Component {
		val header = "${world.name} System Map"
		val guiText = GuiText(header)

		guiText.addBackground(GuiText.GuiBackground(backgroundChar = SPACE_SCREEN_CHARACTER))

		if (world.hasFlag(WorldFlag.NO_SHIP_LOCKS)) {
			guiText.addBackground(GuiText.GuiBackground(
				backgroundChar = SPACE_RED_NEBULA_CHARACTER,
				backgroundWidth = 143 // width of left edge to right side of image (150) - 7 for text margin
			))
		} else {
			guiText.addBackground(GuiText.GuiBackground(
				backgroundChar = SPACE_BLUE_NEBULA_CHARACTER,
				backgroundWidth = 143
			))
		}

		return guiText.build()
	}

}
