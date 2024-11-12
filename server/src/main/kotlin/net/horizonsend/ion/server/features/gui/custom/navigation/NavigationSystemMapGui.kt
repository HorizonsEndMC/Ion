package net.horizonsend.ion.server.features.gui.custom.navigation

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.SPACE_BACKGROUND_CHARACTER
import net.horizonsend.ion.common.utils.text.colors.Colors
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.repeatString
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.starship.MiscStarshipCommands
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItems
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.sidebar.command.BookmarkCommand
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.waypoint.WaypointManager
import net.horizonsend.ion.server.features.waypoint.command.WaypointCommand
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.window.Window

class NavigationSystemMapGui(val player: Player, val world: World) {

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
		_: ClickType, _: Player, _: InventoryClickEvent ->
		planetListShift--
		updateGui()
	}

	private val rightPlanetButton = GuiItems.CustomControlItem("Next Planet In This System", GuiItem.RIGHT) {
		_: ClickType, _: Player, _: InventoryClickEvent ->
		planetListShift++
		updateGui()
	}

	private val leftBeaconButton = GuiItems.CustomControlItem("Previous Beacon In This System", GuiItem.LEFT) {
		_: ClickType, _: Player, _: InventoryClickEvent ->
		beaconListShift--
		updateGui()
	}

	private val rightBeaconButton = GuiItems.CustomControlItem("Next Beacon In This System", GuiItem.RIGHT) {
		_: ClickType, _: Player, _: InventoryClickEvent ->
		beaconListShift++
		updateGui()
	}

	private val leftBookmarkButton = GuiItems.CustomControlItem("Previous Bookmark In This System", GuiItem.LEFT) {
		_: ClickType, _: Player, _: InventoryClickEvent ->
		bookmarkListShift--
		updateGui()
	}

	private val rightBookmarkButton = GuiItems.CustomControlItem("Next Bookmark In This System", GuiItem.RIGHT) {
		_: ClickType, _: Player, _: InventoryClickEvent ->
		bookmarkListShift++
		updateGui()
	}

	companion object {
		val ionWorldCache: LoadingCache<WorldFlag, Collection<World>> = CacheBuilder.newBuilder().build(
			CacheLoader.from { worldFlag: WorldFlag ->
				return@from Bukkit.getWorlds().filter { world -> world.ion.hasFlag(worldFlag) }
			}
		)

		fun openWindow(player: Player, world: World) {
			val gui = NavigationSystemMapGui(player, world)

			Window.single()
				.setViewer(player)
				.setGui(gui.createGui())
				.setTitle(AdventureComponentWrapper(gui.createText()))
				.build()
				.open()
		}

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
			.map { planet -> GuiItems.CustomItemControlItem(getPlanetItems(planet.name),
				navigationInstructionComponents(
					planet.spaceWorldName,
					planet.x,
					192,
					planet.z)
			) { clickType: ClickType, player: Player, _: InventoryClickEvent ->

				when (clickType) {
					ClickType.LEFT -> jumpAction(player, planet.name)
					ClickType.RIGHT -> waypointAction(player, planet.name)
					ClickType.SHIFT_LEFT -> {
						// TODO: Add planet info window opener here
					}
					ClickType.SHIFT_RIGHT -> {
						val serverName = IonServer.configuration.serverName
						val hyperlink = ofChildren(
							Component.text("Click to open ", TextColor.color(Colors.INFORMATION)),
							Component.text("[", TextColor.color(Colors.INFORMATION)),
							Component.text("${planet.planetWorldName} Dynmap", NamedTextColor.AQUA)
								.decorate(TextDecoration.UNDERLINED)
								.clickEvent(ClickEvent.clickEvent(
									ClickEvent.Action.OPEN_URL,
									"https://$serverName.horizonsend.net/?worldname=${planet.spaceWorldName}&x=${planet.location.x}&z=${planet.location.z}",
								)),
							Component.text("]", TextColor.color(Colors.INFORMATION)),
						)
						player.sendMessage(hyperlink)
					}
					else -> {}
				}
			} }
		)
		planetsInWorld = planetItems.size

		beaconItems.addAll(IonServer.configuration.beacons
			.filter { beacon -> beacon.spaceLocation.bukkitWorld() == world }
			.sortedBy { beacon -> beacon.name }
			.map { beacon -> GuiItems.CustomControlItem(beacon.name, GuiItem.BEACON, navigationInstructionComponents(
				beacon.spaceLocation.world,
				beacon.spaceLocation.x,
				beacon.spaceLocation.y,
				beacon.spaceLocation.z)
			) { clickType: ClickType, _: Player, _: InventoryClickEvent ->

				when (clickType) {
					ClickType.LEFT -> jumpAction(player, beacon.name.replace(' ', '_'))
					ClickType.RIGHT -> waypointAction(player, beacon.name.replace(' ', '_'))
					ClickType.SHIFT_LEFT -> {
						openWindow(player, beacon.destination.bukkitWorld())
					}
					ClickType.SHIFT_RIGHT -> {
						val serverName = IonServer.configuration.serverName
						val hyperlink = ofChildren(
							Component.text("Click to open ", TextColor.color(Colors.INFORMATION)),
							Component.text("[", TextColor.color(Colors.INFORMATION)),
							Component.text("${beacon.name} Dynmap", NamedTextColor.AQUA)
								.decorate(TextDecoration.UNDERLINED)
								.clickEvent(ClickEvent.clickEvent(
									ClickEvent.Action.OPEN_URL,
									"https://$serverName.horizonsend.net/?worldname=${beacon.spaceLocation.world}&x=${beacon.spaceLocation.x}&z=${beacon.spaceLocation.z}",
								)),
							Component.text("]", TextColor.color(Colors.INFORMATION)),
						)
						player.sendMessage(hyperlink)
					}
					else -> {}
				}
			} }
		)
		beaconsInWorld = beaconItems.size

		bookmarkItems.addAll(BookmarkCommand.getBookmarks(player)
			.filter { bookmark -> bookmark.worldName == world.name }
			.sortedBy { bookmark -> bookmark.name }
			.map { bookmark -> GuiItems.CustomControlItem(bookmark.name, GuiItem.BOOKMARK,
				navigationInstructionComponents(
					bookmark.worldName,
					bookmark.x,
					bookmark.y,
					bookmark.z))
			{ clickType: ClickType, _: Player, _: InventoryClickEvent ->

				when (clickType) {
					ClickType.LEFT -> jumpAction(player, bookmark.name)
					ClickType.RIGHT -> waypointAction(player, bookmark.name)
					ClickType.SHIFT_LEFT -> {}
					ClickType.SHIFT_RIGHT -> {
						val serverName = IonServer.configuration.serverName
						val hyperlink = ofChildren(
							Component.text("Click to open ", TextColor.color(Colors.INFORMATION)),
							Component.text("[", TextColor.color(Colors.INFORMATION)),
							Component.text("${bookmark.name} Dynmap", NamedTextColor.AQUA)
								.decorate(TextDecoration.UNDERLINED)
								.clickEvent(ClickEvent.clickEvent(
									ClickEvent.Action.OPEN_URL,
									"https://$serverName.horizonsend.net/?worldname=${bookmark.worldName}&x=${bookmark.x}&z=${bookmark.z}",
								)),
							Component.text("]", TextColor.color(Colors.INFORMATION)),
						)
						player.sendMessage(hyperlink)
					}
					else -> {}
				}

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

	private fun navigationInstructionComponents(worldName: String, x: Int, y: Int, z: Int): List<Component> {
		val list = mutableListOf(
			locationComponent(worldName, x, y, z),
			Component.text(repeatString("=", 30)).decorate(TextDecoration.STRIKETHROUGH).color(NamedTextColor.DARK_GRAY),
		)
		if (world == player.world) list.add(initiateJumpComponent())
		list.add(setWaypointComponent())
		list.add(displayInfoComponent())
		list.add(seeDynmapComponent())

		return list
	}

	private fun locationComponent(world: String, x: Int, y: Int, z: Int): Component = template(
		"{0} @ [{1}, {2}, {3}]",
		color = NamedTextColor.DARK_GREEN,
		paramColor = NamedTextColor.GREEN,
		useQuotesAroundObjects = false,
		world, x, y, z
	)

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

	private fun seeDynmapComponent(): Component = template(
		"{0} to see this location on Dynmap",
		color = HE_LIGHT_GRAY,
		paramColor = NamedTextColor.AQUA,
		useQuotesAroundObjects = false,
		"Shift Right Click"
	).decoration(TextDecoration.ITALIC, false)

	private fun jumpAction(
		player: Player,
		destinationName: String
	) {
		if (ActiveStarships.findByPilot(player) != null) {
			MiscStarshipCommands.onJump(player, destinationName, null)
		} else {
			player.userError("You must be piloting a starship!")
		}
	}

	private fun waypointAction(
		player: Player,
		waypointName: String
	) {
		if (WaypointManager.getLastWaypoint(player) != waypointName) {
			WaypointCommand.onSetWaypoint(player, waypointName)
		} else {
			WaypointCommand.onUndoWaypoint(player)
		}
	}
}
