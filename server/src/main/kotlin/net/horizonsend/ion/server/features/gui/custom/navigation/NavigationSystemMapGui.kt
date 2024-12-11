package net.horizonsend.ion.server.features.gui.custom.navigation

import net.horizonsend.ion.common.database.schema.misc.Bookmark
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.SPACE_BLUE_NEBULA_CHARACTER
import net.horizonsend.ion.common.utils.text.SPACE_RED_NEBULA_CHARACTER
import net.horizonsend.ion.common.utils.text.SPACE_SCREEN_CHARACTER
import net.horizonsend.ion.common.utils.text.colors.Colors
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.formatLink
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.repeatString
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.starship.MiscStarshipCommands
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.custom.misc.ItemMenu
import net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.TextInputMenu
import net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.validator.ValidatorResult
import net.horizonsend.ion.server.features.ores.generation.PlanetOreSettings
import net.horizonsend.ion.server.features.sidebar.command.BookmarkCommand
import net.horizonsend.ion.server.features.space.CachedPlanet
import net.horizonsend.ion.server.features.space.CachedStar
import net.horizonsend.ion.server.features.space.CelestialBody
import net.horizonsend.ion.server.features.space.NamedCelestialBody
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.waypoint.WaypointManager
import net.horizonsend.ion.server.features.waypoint.command.WaypointCommand
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
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
	private val leftPlanetButton = GuiItems.CustomControlItem("Previous Planet In This System", GuiItem.LEFT) {
		_: ClickType, _: Player, _: InventoryClickEvent ->
		planetListShift--
		updateGuiShift()
	}

	private val rightPlanetButton = GuiItems.CustomControlItem("Next Planet In This System", GuiItem.RIGHT) {
		_: ClickType, _: Player, _: InventoryClickEvent ->
		planetListShift++
		updateGuiShift()
	}

	private val leftBeaconButton = GuiItems.CustomControlItem("Previous Beacon In This System", GuiItem.LEFT) {
		_: ClickType, _: Player, _: InventoryClickEvent ->
		beaconListShift--
		updateGuiShift()
	}

	private val rightBeaconButton = GuiItems.CustomControlItem("Next Beacon In This System", GuiItem.RIGHT) {
		_: ClickType, _: Player, _: InventoryClickEvent ->
		beaconListShift++
		updateGuiShift()
	}

	private val leftBookmarkButton = GuiItems.CustomControlItem("Previous Bookmark In This System", GuiItem.LEFT) {
		_: ClickType, _: Player, _: InventoryClickEvent ->
		bookmarkListShift--
		updateGuiShift()
	}

	private val rightBookmarkButton = GuiItems.CustomControlItem("Next Bookmark In This System", GuiItem.RIGHT) {
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
		val starItem = if (star != null) createStarCustomControlItem(star) else null

		planetItems.addAll(Space.getPlanets()
			.filter { planet -> planet.spaceWorld == world }
			.sortedBy { planet -> planet.orbitDistance }
			.map { planet -> createPlanetCustomControlItem(planet) }
		)
		planetsInWorld = planetItems.size

		beaconItems.addAll(IonServer.configuration.beacons
			.filter { beacon -> beacon.spaceLocation.bukkitWorld() == world }
			.sortedBy { beacon -> beacon.name }
			.map { beacon -> createBeaconCustomControlItem(beacon) }
		)
		beaconsInWorld = beaconItems.size

		bookmarkItems.addAll(BookmarkCommand.getBookmarks(player)
			.filter { bookmark -> bookmark.worldName == world.name }
			.sortedBy { bookmark -> bookmark.name }
			.map { bookmark -> createBookmarkCustomControlItem(bookmark) }
		)
		bookmarkCount = bookmarkItems.size

		if (starItem != null) gui.setItem(MIDDLE_COLUMN, 0, starItem)

		gui.setItem(0, MENU_ROW, GuiItems.CustomControlItem("Close Menu", GuiItem.CANCEL) {
			_: ClickType, _: Player, _: InventoryClickEvent -> player.closeInventory()
		})

		gui.setItem(1, MENU_ROW, GuiItems.CustomControlItem("Return To Galactic Menu", GuiItem.DOWN) {
			_: ClickType, _: Player, _: InventoryClickEvent -> NavigationGalacticMapGui(player).openMainWindow()
		})

		gui.setItem(2, MENU_ROW, GuiItems.CustomControlItem("Search For Destination", GuiItem.MAGNIFYING_GLASS) {
			_: ClickType, player: Player, _: InventoryClickEvent ->
			openSearchMenu(player)
		})

		updateGuiShift()
		updateGuiRoute()

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

	private fun openSearchMenu(player: Player) {
		TextInputMenu(
			player, Component.text("Search for destination"), Component.text("Enter a destination name"),
			backButtonHandler = { this.openMainWindow() },
			inputValidator = { input ->
				if (input.isEmpty()) ValidatorResult.FailureResult(Component.text("Search for a destination first!", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)) else {
					val searchResults = getSearchResults(input)

					if (searchResults.isNotEmpty()) ValidatorResult.ResultsResult(searchResults.map { obj ->
						when (obj) {
							is NamedCelestialBody -> Component.text(obj.name, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
							is ServerConfiguration.HyperspaceBeacon -> Component.text(obj.name, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
							is Bookmark -> Component.text(obj.name, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
							else -> Component.empty()
						}
					}) else ValidatorResult.FailureResult(Component.text("No destinations found!", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false))
				}
			},
			successfulInputHandler = { input ->
				val searchResults = getSearchResults(input)
				val newGui = ItemMenu(Component.text("Search result: $input"), player, searchResults.map { obj ->
					// converts the list of matching objects to the corresponding GUI item
					when (obj) {
						is CachedPlanet -> createPlanetCustomControlItem(obj)
						is CachedStar -> createStarCustomControlItem(obj)
						is ServerConfiguration.HyperspaceBeacon -> createBeaconCustomControlItem(obj)
						is Bookmark -> createBookmarkCustomControlItem(obj)
						else -> GuiItems.EmptyItem()
					}
				}, backButtonHandler = { this.openMainWindow() })
				newGui.openMainWindow()
			},
		).open()
	}

	private fun getSearchResults(input: String): List<Any> {
		val celestialBodies: List<CelestialBody> = Space.getStars().plus(Space.getPlanets())
			.filter { body -> input.split(' ').all { splitInput -> body.name.contains(splitInput, ignoreCase = true) } }
		val beacons: List<ServerConfiguration.HyperspaceBeacon> = IonServer.configuration.beacons
			.filter { beacon -> input.split(' ').all { splitInput -> beacon.name.contains(splitInput, ignoreCase = true) } }
		val bookmarks: List<Bookmark> = BookmarkCommand.getBookmarks(player)
			.filter { bookmark -> input.split(' ').all { splitInput -> bookmark.name.contains(splitInput, ignoreCase = true) } }

		return celestialBodies.plus(beacons).plus(bookmarks)
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
	 * Updates the GUI for route-related components
	 */
	private fun updateGuiRoute() {
		gui.setItem(5, MENU_ROW, GuiItems.CustomControlItem("Current Route:", GuiItem.ROUTE_SEGMENT_2, waypointComponents()))

		if (WaypointManager.getNextWaypoint(player) != null) {
			gui.setItem(6, MENU_ROW, GuiItems.CustomControlItem("Cancel All Route Waypoints", GuiItem.ROUTE_CANCEL) {
					_: ClickType, _: Player, _: InventoryClickEvent ->
				WaypointCommand.onClearWaypoint(player)
				updateGuiRoute()
			})
			gui.setItem(7, MENU_ROW, GuiItems.CustomControlItem("Undo The Last Waypoint", GuiItem.ROUTE_UNDO) {
					_: ClickType, _: Player, _: InventoryClickEvent ->
				WaypointCommand.onUndoWaypoint(player)
				updateGuiRoute()
			})
			gui.setItem(8, MENU_ROW, GuiItems.CustomControlItem("Jump To The Next Waypoint", GuiItem.ROUTE_JUMP) {
					_: ClickType, _: Player, _: InventoryClickEvent ->
				MiscStarshipCommands.onJump(player, "auto", null)
				updateGuiRoute()
			})
		} else {
			gui.setItem(6, MENU_ROW, GuiItems.CustomControlItem("Cancel All Route Waypoints", GuiItem.ROUTE_CANCEL_GRAY, listOf(needWaypointComponent())))
			gui.setItem(7, MENU_ROW, GuiItems.CustomControlItem("Undo The Last Waypoint", GuiItem.ROUTE_UNDO_GRAY, listOf(needWaypointComponent())))
			gui.setItem(8, MENU_ROW, GuiItems.CustomControlItem("Jump To The Next Waypoint", GuiItem.ROUTE_JUMP_GRAY, listOf(needWaypointComponent())))
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

	/**
	 * Gets the associated custom item from the planet's name.
	 * @return the custom planet icon GuiItem
	 * @param name the name of the planet
	 */
	private fun getPlanetItems(name: String): GuiItem {
		return when (name) {
			"Aerach" -> GuiItem.AERACH_2
			"Aret" -> GuiItem.ARET_2
			"Chandra" -> GuiItem.CHANDRA_2
			"Chimgara" -> GuiItem.CHIMGARA_2
			"Damkoth" -> GuiItem.DAMKOTH_2
			"Disterra" -> GuiItem.DISTERRA_2
			"Eden" -> GuiItem.EDEN_2
			"Gahara" -> GuiItem.GAHARA_2
			"Herdoli" -> GuiItem.HERDOLI_2
			"Ilius" -> GuiItem.ILIUS_2
			"Isik" -> GuiItem.ISIK_2
			"Kovfefe" -> GuiItem.KOVFEFE_2
			"Krio" -> GuiItem.KRIO_2
			"Lioda" -> GuiItem.LIODA_2
			"Luxiterna" -> GuiItem.LUXITERNA_2
			"Qatra" -> GuiItem.QATRA_2
			"Rubaciea" -> GuiItem.RUBACIEA_2
			"Turms" -> GuiItem.TURMS_2
			"Vask" -> GuiItem.VASK_2

			"Asteri" -> GuiItem.ASTERI_2
			"EdenHack" -> GuiItem.HORIZON_2
			"Ilios" -> GuiItem.ILIOS_2
			"Regulus" -> GuiItem.REGULUS_2
			"Sirius" -> GuiItem.SIRIUS_2


			else -> GuiItem.AERACH_2
		}
	}

	/**
	 * Constructs a list of Components used for the lore of objects
	 */
	private fun navigationInstructionComponents(obj: Any, worldName: String, x: Int, y: Int, z: Int): List<Component> {
		val list = mutableListOf(
			locationComponent(worldName, x, y, z),
		)

		// Ore quantity and star
		if (obj is CachedPlanet) {
			list.add(oreComponent(obj))
		}

		list.add(Component.text(repeatString("=", 30)).decorate(TextDecoration.STRIKETHROUGH).color(NamedTextColor.DARK_GRAY))
		if (world == player.world) list.add(initiateJumpComponent())
		list.add(setWaypointComponent())
		if (obj is CachedPlanet) {
			list.add(displayInfoComponent())
		} else if (obj is ServerConfiguration.HyperspaceBeacon) {
			list.add(nextSystemComponent())
		}
		list.add(seeDynmapComponent())

		return list
	}

	private fun createBookmarkCustomControlItem(bookmark: Bookmark) =
		GuiItems.CustomControlItem(
			bookmark.name, GuiItem.BOOKMARK,
			navigationInstructionComponents(
				bookmark,
				bookmark.worldName,
				bookmark.x,
				bookmark.y,
				bookmark.z
			)
		)
		{ clickType: ClickType, _: Player, _: InventoryClickEvent ->

			when (clickType) {
				ClickType.LEFT -> if (bookmark.worldName == player.world.name) jumpAction(player, bookmark.name)
				ClickType.RIGHT -> waypointAction(player, bookmark.name)
				ClickType.SHIFT_LEFT -> {}
				ClickType.SHIFT_RIGHT -> {
					val spaceWorld = Bukkit.getWorld(bookmark.worldName) ?: player.world
					dynmapLinkAction(bookmark.name, player, spaceWorld, bookmark.x, bookmark.z)
				}

				else -> {}
			}
		}

	private fun createBeaconCustomControlItem(beacon: ServerConfiguration.HyperspaceBeacon) =
		GuiItems.CustomControlItem(
			beacon.name, GuiItem.BEACON, navigationInstructionComponents(
				beacon,
				beacon.spaceLocation.world,
				beacon.spaceLocation.x,
				beacon.spaceLocation.y,
				beacon.spaceLocation.z
			)
		) { clickType: ClickType, _: Player, _: InventoryClickEvent ->

			when (clickType) {
				ClickType.LEFT -> if (beacon.spaceLocation.world == player.world.name) jumpAction(
					player,
					beacon.name.replace(' ', '_')
				)

				ClickType.RIGHT -> waypointAction(player, beacon.name.replace(' ', '_'))
				ClickType.SHIFT_LEFT -> {
					NavigationSystemMapGui(player, beacon.destination.bukkitWorld()).openMainWindow()
				}

				ClickType.SHIFT_RIGHT -> dynmapLinkAction(
					beacon.name,
					player,
					beacon.spaceLocation.bukkitWorld(),
					beacon.spaceLocation.x,
					beacon.spaceLocation.z
				)

				else -> {}
			}
		}

	private fun createPlanetCustomControlItem(planet: CachedPlanet) =
		GuiItems.CustomControlItem(
			planet.name, getPlanetItems(planet.name),
			navigationInstructionComponents(
				planet,
				planet.spaceWorldName,
				planet.location.x,
				planet.location.y,
				planet.location.z
			)
		) { clickType: ClickType, player: Player, _: InventoryClickEvent ->

			when (clickType) {
				ClickType.LEFT -> if (planet.spaceWorldName == player.world.name) jumpAction(player, planet.name)
				ClickType.RIGHT -> waypointAction(player, planet.name)
				ClickType.SHIFT_LEFT -> {
					// TODO: Add planet info window opener here
				}

				ClickType.SHIFT_RIGHT -> {
					val spaceWorld = planet.spaceWorld ?: player.world
					dynmapLinkAction(planet.name, player, spaceWorld, planet.location.x, planet.location.z)
				}

				else -> {}
			}
		}

	private fun createStarCustomControlItem(star: CachedStar) =
		GuiItems.CustomControlItem(
			star.name, getPlanetItems(star.name),
			navigationInstructionComponents(
				star,
				star.spaceWorldName,
				star.location.x,
				star.location.y,
				star.location.z
			)
		) { clickType: ClickType, player: Player, _: InventoryClickEvent ->

			when (clickType) {
				ClickType.LEFT -> if (star.spaceWorldName == player.world.name) jumpAction(
					player,
					star.location.x,
					star.location.z
				)

				ClickType.RIGHT -> waypointAction(player, star.spaceWorldName, star.location.x, star.location.z)
				ClickType.SHIFT_LEFT -> {
					// TODO: Add star info window opener here
				}

				ClickType.SHIFT_RIGHT -> {
					val spaceWorld = star.spaceWorld ?: player.world
					dynmapLinkAction(star.name, player, spaceWorld, star.location.x, star.location.z)
				}

				else -> {}
			}
		}

	private fun locationComponent(world: String, x: Int, y: Int, z: Int): Component = template(
		"{0} @ [{1}, {2}, {3}]",
		color = NamedTextColor.DARK_GREEN,
		paramColor = NamedTextColor.GREEN,
		useQuotesAroundObjects = false,
		world, x, y, z
	).decoration(TextDecoration.ITALIC, false)

	private fun oreComponent(planet: CachedPlanet): Component {
		val planetWorld = planet.planetWorld
		if (planetWorld != null) {
			val ores = PlanetOreSettings[planetWorld]?.ores
			if (ores != null) {
				val oreComponents = ores.map { ore -> ofChildren(
					Component.text("${ore.ore.name.replace('_', ' ').lowercase().replaceFirstChar(Char::titlecase)}: ", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false),
					Component.text(repeatString("★", ore.stars), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false),
					Component.text(" | ", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false)
				) }
				return Component.textOfChildren(*oreComponents.toTypedArray())
			}
		}
		return Component.empty()
	}

	private fun initiateJumpComponent(): Component = template(
		"{0} to initiate hyperspace jump to this location",
		color = HE_LIGHT_GRAY,
		paramColor = NamedTextColor.AQUA,
		useQuotesAroundObjects = false,
		"Left Click"
	).decoration(TextDecoration.ITALIC, false)

	private fun setWaypointComponent(): Component = template(
		"{0} to set a route waypoint to this location",
		color = HE_LIGHT_GRAY,
		paramColor = NamedTextColor.AQUA,
		useQuotesAroundObjects = false,
		"Right Click"
	).decoration(TextDecoration.ITALIC, false)

	private fun displayInfoComponent(): Component = template(
		"{0} to display information about this location",
		color = HE_LIGHT_GRAY,
		paramColor = NamedTextColor.AQUA,
		useQuotesAroundObjects = false,
		"Shift Left Click"
	).decoration(TextDecoration.ITALIC, false)

	private fun nextSystemComponent(): Component = template(
		"{0} to display the destination system's map",
		color = HE_LIGHT_GRAY,
		paramColor = NamedTextColor.AQUA,
		useQuotesAroundObjects = false,
		"Shift Left Click"
	).decoration(TextDecoration.ITALIC, false)

	private fun seeDynmapComponent(): Component = template(
		"{0} to see this location on Dynmap",
		color = HE_LIGHT_GRAY,
		paramColor = NamedTextColor.AQUA,
		useQuotesAroundObjects = false,
		"Shift Right Click"
	).decoration(TextDecoration.ITALIC, false)

	private fun waypointComponents(): List<Component> {
		val paths = WaypointManager.playerPaths[player.uniqueId]
		if (paths.isNullOrEmpty()) {
			return listOf(Component.text("Current route is empty. Add waypoints to your route", NamedTextColor.GOLD)
				.decoration(TextDecoration.ITALIC, false))
		}

		return paths.map { path -> Component.text("${path.startVertex.name} to ${path.endVertex.name} (distance ${path.weight.toInt()})", NamedTextColor.AQUA)
			.decoration(TextDecoration.ITALIC, false) }
	}

	private fun needWaypointComponent() = Component.text("You need at least one route waypoint set", NamedTextColor.GOLD)
		.decoration(TextDecoration.ITALIC, false)

	private fun jumpAction(player: Player, destinationName: String) {
		if (ActiveStarships.findByPilot(player) != null) {
			MiscStarshipCommands.onJump(player, destinationName, null)
			player.closeInventory()
		} else {
			player.userError("You must be piloting a starship!")
		}
	}

	private fun jumpAction(player: Player, x: Int, z: Int) {
		if (ActiveStarships.findByPilot(player) != null) {
			MiscStarshipCommands.onJump(player, x.toString(), z.toString(), null)
			player.closeInventory()
		} else {
			player.userError("You must be piloting a starship!")
		}
	}

	private fun waypointAction(player: Player, waypointName: String) {
		if (WaypointManager.getLastWaypoint(player) != waypointName) {
			WaypointCommand.onSetWaypoint(player, waypointName)
		} else {
			WaypointCommand.onUndoWaypoint(player)
		}
		updateGuiRoute()
	}

	private fun waypointAction(player: Player, world: String, x: Int, z: Int) {
		WaypointCommand.onSetWaypoint(player, world, x.toString(), z.toString())
		updateGuiRoute()
	}

	private fun dynmapLinkAction(name: String, player: Player, spaceWorld: World, x: Int, z: Int) {
		val serverName = IonServer.configuration.serverName
		val hyperlink = ofChildren(
			Component.text("Click to open ", TextColor.color(Colors.INFORMATION)),
			Component.text("[", TextColor.color(Colors.INFORMATION)),
			formatLink("$name Dynmap", "https://$serverName.horizonsend.net/?worldname=${spaceWorld.name}&x=$x&z=$z"),
			Component.text("]", TextColor.color(Colors.INFORMATION)),
		)
		player.sendMessage(hyperlink)
	}
}
