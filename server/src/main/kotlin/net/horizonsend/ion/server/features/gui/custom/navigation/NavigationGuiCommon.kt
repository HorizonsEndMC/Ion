package net.horizonsend.ion.server.features.gui.custom.navigation

import net.horizonsend.ion.common.database.schema.misc.Bookmark
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.NavigationObject
import net.horizonsend.ion.common.utils.text.colors.Colors
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.formatLink
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.repeatString
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.server.command.starship.MiscStarshipCommands
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.TextInputMenu.Companion.searchEntires
import net.horizonsend.ion.server.features.ores.generation.PlanetOreSettings
import net.horizonsend.ion.server.features.sidebar.command.BookmarkCommand
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.space.body.CachedStar
import net.horizonsend.ion.server.features.space.body.planet.CachedPlanet
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.waypoint.WaypointManager
import net.horizonsend.ion.server.features.waypoint.command.WaypointCommand
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item

object NavigationGuiCommon {

    private const val MENU_ROW = 5

    fun openSearchMenu(player: Player, world: World, gui: Gui, backButtonHandler: () -> Unit) {
		player.searchEntires(
			entries = getSearchableNavigationItems(player),
			searchTermProvider = { listOf(it.name) },
			prompt = text("Search for destination"),
			description = text("Enter a destination name"),
			backButtonHandler = { backButtonHandler.invoke() },
			itemTransformer = { obj ->
				when (obj) {
					is CachedPlanet -> createPlanetDisplayItem(obj, player, world)
					is CachedStar -> createStarDisplayItem(obj, player, world)
					is ServerConfiguration.HyperspaceBeacon -> createBeaconDisplayItem(obj, player, world)
					is Bookmark -> createBookmarkDisplayItem(obj, player, world)
					else -> GuiItem.EMPTY.makeItem()
				}
			},
			componentTransformer = { it.name.toComponent() },
			handler = { clickType, obj ->
				when (obj) {
					is CachedPlanet -> handlePlanetClick(obj, player, gui, clickType, backButtonHandler)
					is CachedStar -> handleStarClick(obj, player, gui, clickType, backButtonHandler)
					is ServerConfiguration.HyperspaceBeacon -> handleBeaconClick(obj, player, gui, clickType)
					is Bookmark -> handleBookmarkClick(obj, player, gui, clickType)
					else -> GuiItem.EMPTY.makeItem()
				}
			}
		)
    }

	private fun getSearchableNavigationItems(player: Player): List<NavigationObject> {
		val list = mutableListOf<NavigationObject>()
		list.addAll(Space.getStars())
		list.addAll(Space.getAllPlanets())
		list.addAll(ConfigurationFiles.serverConfiguration().beacons)
		list.addAll(BookmarkCommand.getBookmarks(player))

		return list
	}

//    private fun getSearchResults(input: String, player: Player): List<NavigationObject> {
//
//
//        val celestialBodies: List<CelestialBody> = Space.getStars().plus(Space.getAllPlanets())
//            .filter { body -> input.split(' ').all { splitInput -> body.name.contains(splitInput, ignoreCase = true) } }
//        val beacons: List<ServerConfiguration.HyperspaceBeacon> = ConfigurationFiles.serverConfiguration().beacons
//            .filter { beacon -> input.split(' ').all { splitInput -> beacon.name.contains(splitInput, ignoreCase = true) } }
//        val bookmarks: List<Bookmark> = BookmarkCommand.getBookmarks(player)
//            .filter { bookmark -> input.split(' ').all { splitInput -> bookmark.name.contains(splitInput, ignoreCase = true) } }
//
//        return celestialBodies.plus(beacons).plus(bookmarks)
//    }

    /**
     * Updates the GUI for route-related components
     */
    fun updateGuiRoute(gui: Gui, player: Player) {
        gui.setItem(5, MENU_ROW, GuiItems.CustomControlItem(text("Current Route:")
            .decoration(TextDecoration.ITALIC, false), GuiItem.ROUTE_SEGMENT_2, waypointComponents(player)))

        if (WaypointManager.getNextWaypoint(player) != null) {
            gui.setItem(6, MENU_ROW, GuiItems.CustomControlItem(text("Cancel All Route Waypoints")
                .decoration(TextDecoration.ITALIC, false), GuiItem.ROUTE_CANCEL) {
                    _: ClickType, _: Player, _: InventoryClickEvent ->
                WaypointCommand.onClearWaypoint(player)
                updateGuiRoute(gui, player)
            })
            gui.setItem(7, MENU_ROW, GuiItems.CustomControlItem(text("Undo The Last Waypoint")
                .decoration(TextDecoration.ITALIC, false), GuiItem.ROUTE_UNDO) {
                    _: ClickType, _: Player, _: InventoryClickEvent ->
                WaypointCommand.onUndoWaypoint(player)
                updateGuiRoute(gui, player)
            })
            gui.setItem(8, MENU_ROW, GuiItems.CustomControlItem(text("Jump To The Next Waypoint")
                .decoration(TextDecoration.ITALIC, false), GuiItem.ROUTE_JUMP) {
                    _: ClickType, _: Player, _: InventoryClickEvent ->
                if (ActiveStarships.findByPilot(player) != null) {
                    MiscStarshipCommands.onJump(player, "auto", null)
                    player.closeInventory()
                } else {
                    player.userError("You must be piloting a starship!")
                }

            })
        } else {
            gui.setItem(6, MENU_ROW, GuiItems.CustomControlItem(text("Cancel All Route Waypoints")
                .decoration(TextDecoration.ITALIC, false), GuiItem.ROUTE_CANCEL_GRAY, listOf(needWaypointComponent())))
            gui.setItem(7, MENU_ROW, GuiItems.CustomControlItem(text("Undo The Last Waypoint")
                .decoration(TextDecoration.ITALIC, false), GuiItem.ROUTE_UNDO_GRAY, listOf(needWaypointComponent())))
            gui.setItem(8, MENU_ROW, GuiItems.CustomControlItem(text("Jump To The Next Waypoint")
                .decoration(TextDecoration.ITALIC, false), GuiItem.ROUTE_JUMP_GRAY, listOf(needWaypointComponent())))
        }
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
    private fun navigationInstructionComponents(obj: Any, worldName: String, x: Int, y: Int, z: Int, player: Player, world: World): List<Component> {
        val list = mutableListOf(
            locationComponent(worldName, x, y, z),
        )

        // Ore quantity and star
        if (obj is CachedPlanet) {
            list.add(oreComponent(obj))
        }

        list.add(text(repeatString("=", 30)).decorate(TextDecoration.STRIKETHROUGH).color(NamedTextColor.DARK_GRAY))
        if (world == player.world) list.add(initiateJumpComponent())
        list.add(setWaypointComponent())
        if (obj is CachedPlanet || obj is CachedStar) {
            list.add(displayInfoComponent())
        } else if (obj is ServerConfiguration.HyperspaceBeacon) {
            list.add(nextSystemComponent())
        }
        list.add(seeDynmapComponent())

        return list
    }

    private fun createBookmarkDisplayItem(bookmark: Bookmark, player: Player, world: World): ItemStack {
		return GuiItem.BOOKMARK
			.makeItem(text(bookmark.name))
			.updateLore(navigationInstructionComponents(
				bookmark,
				bookmark.worldName,
				bookmark.x,
				bookmark.y,
				bookmark.z,
				player,
				world
			))
	}

	private fun handleBookmarkClick(bookmark: Bookmark, player: Player, gui: Gui, clickType: ClickType) {
		when (clickType) {
			ClickType.LEFT -> if (bookmark.worldName == player.world.name) jumpAction(player, bookmark.name)
			ClickType.RIGHT -> waypointAction(player, bookmark.name, gui)
			ClickType.SHIFT_LEFT -> {}
			ClickType.SHIFT_RIGHT -> {
				val spaceWorld = Bukkit.getWorld(bookmark.worldName) ?: player.world
				dynmapLinkAction(bookmark.name, player, spaceWorld, bookmark.x, bookmark.z)
			}

			else -> {}
		}
	}

    fun createBookmarkCustomControlItem(bookmark: Bookmark, player: Player, world: World, gui: Gui): Item {
		return GuiItems.createButton(createBookmarkDisplayItem(bookmark, player, world)) { clickType, _, _ ->
			handleBookmarkClick(bookmark, player, gui, clickType)
		}
	}

	private fun createBeaconDisplayItem(beacon: ServerConfiguration.HyperspaceBeacon, player: Player, world: World): ItemStack {
		return GuiItem.BEACON
			.makeItem(text(beacon.name))
			.updateLore(navigationInstructionComponents(
				beacon,
				beacon.spaceLocation.world,
				beacon.spaceLocation.x,
				beacon.spaceLocation.y,
				beacon.spaceLocation.z,
				player,
				world
			))
	}

	private fun handleBeaconClick(beacon: ServerConfiguration.HyperspaceBeacon, player: Player, gui: Gui, clickType: ClickType) {
		when (clickType) {
			ClickType.LEFT -> if (beacon.spaceLocation.world == player.world.name) jumpAction(
				player,
				beacon.name.replace(' ', '_')
			)

			ClickType.RIGHT -> waypointAction(player, beacon.name.replace(' ', '_'), gui)
			ClickType.SHIFT_LEFT -> {
				NavigationSystemMapGui(player, beacon.destination.bukkitWorld()).openGui()
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

    fun createBeaconCustomControlItem(beacon: ServerConfiguration.HyperspaceBeacon, player: Player, world: World, gui: Gui): Item {
		return GuiItems.createButton(createBeaconDisplayItem(beacon, player, world)) { clickType, _, _ ->
			handleBeaconClick(beacon, player, gui, clickType)
		}
	}

    private fun createPlanetDisplayItem(planet: CachedPlanet, player: Player, world: World): ItemStack {
		return getPlanetItems(planet.name)
			.makeItem(text(planet.name).itemName)
			.updateLore(navigationInstructionComponents(
				planet,
				planet.spaceWorldName,
				planet.location.x,
				planet.location.y,
				planet.location.z,
				player,
				world
			))
	}

	private fun handlePlanetClick(planet: CachedPlanet, player: Player, gui: Gui, clickType: ClickType, backButtonHandler: () -> Unit) {
		when (clickType) {
			ClickType.LEFT -> if (planet.spaceWorldName == player.world.name) jumpAction(player, planet.name)
			ClickType.RIGHT -> waypointAction(player, planet.name, gui)
			ClickType.SHIFT_LEFT -> {
				NavigationInfoGui(player, planet.name, getPlanetItems(planet.name), oreComponent2(planet)) { backButtonHandler.invoke() }.openGui()
			}

			ClickType.SHIFT_RIGHT -> {
				val spaceWorld = planet.spaceWorld ?: player.world
				dynmapLinkAction(planet.name, player, spaceWorld, planet.location.x, planet.location.z)
			}

			else -> {}
		}
	}

    fun createPlanetCustomControlItem(planet: CachedPlanet, player: Player, world: World, gui: Gui, backButtonHandler: () -> Unit): Item {
		return GuiItems.createButton(createPlanetDisplayItem(planet, player, world)) { clickType, _, _ ->
			handlePlanetClick(planet, player, gui, clickType, backButtonHandler)
		}
	}

	private  fun createStarDisplayItem(star: CachedStar, player: Player, world: World): ItemStack {
		return getPlanetItems(star.name)
			.makeItem(text(star.name).itemName)
			.updateLore(navigationInstructionComponents(
				star,
				star.spaceWorldName,
				star.location.x,
				star.location.y,
				star.location.z,
				player,
				world
			))
	}

	private fun handleStarClick(star: CachedStar, player: Player, gui: Gui, clickType: ClickType, backButtonHandler: () -> Unit) {
		when (clickType) {
			ClickType.LEFT -> if (star.spaceWorldName == player.world.name) jumpAction(
				player,
				star.location.x,
				star.location.z
			)

			ClickType.RIGHT -> waypointAction(player, star.spaceWorldName, star.location.x, star.location.z, gui)
			ClickType.SHIFT_LEFT -> {
				NavigationInfoGui(player, star.name, getPlanetItems(star.name)) { backButtonHandler.invoke() }.openGui()
			}

			ClickType.SHIFT_RIGHT -> {
				val spaceWorld = star.spaceWorld ?: player.world
				dynmapLinkAction(star.name, player, spaceWorld, star.location.x, star.location.z)
			}

			else -> {}
		}
	}

    fun createStarCustomControlItem(star: CachedStar, player: Player, world: World, gui: Gui, backButtonHandler: () -> Unit): Item {
		return GuiItems.createButton(createStarDisplayItem(star, player, world)) { clickType, _, _ ->
			handleStarClick(star, player, gui, clickType, backButtonHandler)
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
        val planetWorld = planet.planetWorld ?: return empty()
        val ores = PlanetOreSettings[planetWorld]?.ores

        if (ores != null) {
            val oreComponents = ores.map { ore -> ofChildren(
                text("${ore.ore.name.replace('_', ' ').lowercase().replaceFirstChar(Char::titlecase)}: ", NamedTextColor.GOLD).decoration(
                    TextDecoration.ITALIC, false),
                text(repeatString("★", ore.stars), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false),
                text(" | ", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false)
            ) }
            return ofChildren(*oreComponents.toTypedArray())
        }

        return empty()
    }

    private fun oreComponent2(planet: CachedPlanet): Component {
        val planetWorld = planet.planetWorld ?: return empty()
        val ores = PlanetOreSettings[planetWorld]?.ores

        if (ores != null) {
            val oreComponents = ores.map { ore -> ofChildren(
                text("${ore.ore.name.replace('_', ' ').lowercase().replaceFirstChar(Char::titlecase)}: ", NamedTextColor.DARK_AQUA).decoration(
                    TextDecoration.ITALIC, false),
                when (ore.stars) {
                    1 -> text("Low", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)
                    2 -> text("Med", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false)
                    3 -> text("High", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)
                    else -> text("Unknown", NamedTextColor.DARK_RED).decoration(TextDecoration.ITALIC, false)
                },
                text(" | ", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false)
            ) }
            return ofChildren(*oreComponents.toTypedArray())
        }

        return empty()
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

    private fun waypointComponents(player: Player): List<Component> {
        val paths = WaypointManager.playerPaths[player.uniqueId]
        if (paths.isNullOrEmpty()) {
            return listOf(
                text("Current route is empty. Add waypoints to your route", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false))
        }

        return paths.map { path -> text("${path.startVertex.name} to ${path.endVertex.name} (distance ${path.weight.toInt()})", NamedTextColor.AQUA)
            .decoration(TextDecoration.ITALIC, false) }
    }

    private fun needWaypointComponent() = text("You need at least one route waypoint set", NamedTextColor.GOLD)
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

    private fun waypointAction(player: Player, waypointName: String, gui: Gui) {
        if (WaypointManager.getLastWaypoint(player) != waypointName) {
            WaypointCommand.onSetWaypoint(player, waypointName)
        } else {
            WaypointCommand.onUndoWaypoint(player)
        }
        updateGuiRoute(gui, player)
    }

    private fun waypointAction(player: Player, world: String, x: Int, z: Int, gui: Gui) {
        WaypointCommand.onSetWaypoint(player, world, x.toString(), z.toString())
        updateGuiRoute(gui, player)
    }

    private fun dynmapLinkAction(name: String, player: Player, spaceWorld: World, x: Int, z: Int) {
        val serverName = ConfigurationFiles.serverConfiguration().serverName
        val hyperlink = ofChildren(
            text("Click to open ", TextColor.color(Colors.INFORMATION)),
            text("[", TextColor.color(Colors.INFORMATION)),
            formatLink("$name Dynmap", "https://$serverName.horizonsend.net/?worldname=${spaceWorld.name}&x=$x&z=$z"),
            text("]", TextColor.color(Colors.INFORMATION)),
        )
        player.sendMessage(hyperlink)
    }
}
