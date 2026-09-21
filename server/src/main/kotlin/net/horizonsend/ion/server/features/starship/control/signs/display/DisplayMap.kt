@file:Suppress("UnstableApiUsage")

package net.horizonsend.ion.server.features.starship.control.signs.display

import io.papermc.paper.datacomponent.DataComponentTypes
import net.horizonsend.ion.common.database.schema.misc.Bookmark
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.successAction
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.BOLD
import net.horizonsend.ion.common.utils.text.SPECIAL_FONT_KEY
import net.horizonsend.ion.common.utils.text.asShadowColor
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.client.display.HudIcons
import net.horizonsend.ion.server.features.client.display.HudIcons.PLANET_PREFIX
import net.horizonsend.ion.server.features.client.display.HudIcons.STAR_PREFIX
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItem.Companion.applyGuiModel
import net.horizonsend.ion.server.features.space.body.CachedStar
import net.horizonsend.ion.server.features.space.body.CelestialBody
import net.horizonsend.ion.server.features.space.body.NamedCelestialBody
import net.horizonsend.ion.server.features.space.body.planet.CachedPlanet
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.signs.display.features.DisplayButtonFeature
import net.horizonsend.ion.server.features.starship.control.signs.display.features.DisplayFeature
import net.horizonsend.ion.server.features.starship.control.signs.display.features.map.BeaconDisplayFeature
import net.horizonsend.ion.server.features.starship.control.signs.display.features.map.BookmarkDisplayFeature
import net.horizonsend.ion.server.features.starship.control.signs.display.features.map.CelestialBodyFeature
import net.horizonsend.ion.server.features.starship.control.signs.display.features.map.ShipDisplayFeature
import net.horizonsend.ion.server.features.starship.control.signs.display.features.map.SystemDisplayButton
import net.horizonsend.ion.server.features.starship.event.StarshipPilotedEvent
import net.horizonsend.ion.server.features.starship.fleet.Fleets
import net.horizonsend.ion.server.features.waypoint.WaypointManager
import net.horizonsend.ion.server.features.waypoint.command.WaypointCommand
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.filterIsInstance
import net.horizonsend.ion.server.miscellaneous.utils.updateData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Sign
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import org.joml.Vector3d
import kotlin.math.abs
import kotlin.math.absoluteValue

class DisplayMap(ship: Starship, location: Location, dir: Vector, sizeX: Double, sizeY: Double, offset: Vector3d) :
	Display(ship, location, dir, sizeX, sizeY, offset) {
	var state: MapState = MapState.LOCAL_MAP

	val absoluteMaxDistance = 20000.0
	val absoluteMinimumMaxDistance = 2000.0
	var maxDistance = 4000.0

	val shipsTracked = mutableMapOf<Starship, ShipDisplayFeature>()
	val celestialBodiesTracked = mutableMapOf<CelestialBody, CelestialBodyFeature>()
	val beaconsTracked = mutableMapOf<ServerConfiguration.HyperspaceBeacon, BeaconDisplayFeature>()
	val bookmarkTracked = mutableMapOf<Bookmark, BookmarkDisplayFeature>()

	var stateMap: DisplayFeature? = null
	var systemForSystemMap: World? = null

	override fun init() {
		super.init()
		val (shouldUse, state, size) = loadMapStateFromLocation(location)
		if (shouldUse) {
			this.state = state
			this.maxDistance = size
		}
		initializeMapBackgroundAndBorder()
		setupMapSideBarButtons()
		when (state) {
			MapState.LOCAL_MAP -> placeLocalMap()
			MapState.SYSTEMS_MAP -> if (!shouldUse) placeSystemsMap() else placeGalacticMap()
			else -> placeGalacticMap()
		}

		//Add entities to ship
		for (mapFeatures in commonFeatures) {
			mapFeatures.init()
			ship.entityPassengers.addAll(mapFeatures.entities)
		}
		for (mapButtonDisplay in commonButtons) {
			mapButtonDisplay.init()
			ship.entityPassengers.addAll(mapButtonDisplay.entities)
		}
	}

	override fun tick() {
		//We dont want too many of these maps going around
		if (ship.displays.filterIsInstance<DisplayMap>().size > 2) {
			return
		}

		super.tick()

		when (state) {
			MapState.LOCAL_MAP -> {
				try {
					val shipsInRange = shipsInRange(maxDistance, ship)
					val centerOfMass = ship.centerOfMass.toVector()
					val bodiesInRange = celestialBodiesInRange(maxDistance, centerOfMass, this.location.world)
					val beaconsInRange = beaconsInRange(maxDistance, centerOfMass, this.location.world)
					val bookmarksInRange = bookmarksInRange(this, maxDistance, centerOfMass, this.location.world)
					for (ship in shipsInRange) {
						if (shipsTracked.containsKey(ship)) continue
						else {
							shipsTracked[ship] = generateShipMapFeature(ship) ?: continue
						}
					}
					val shipsNotInRange = shipsTracked.filterNot { shipsInRange.contains(it.key) }

					shipsNotInRange.forEach {
						it.value.despawn()
						shipsTracked.remove(it.key)
					}

					for (body in bodiesInRange) {
						if (celestialBodiesTracked.containsKey(body)) continue
						else {
							celestialBodiesTracked[body] = generateCelestialBodyMapFeature(body) ?: continue
						}
					}

					for (beacon in beaconsInRange) {
						if (beaconsTracked.containsKey(beacon)) continue
						else {
							beaconsTracked[beacon] = generateBeaconMapFeature(beacon) ?: continue
						}
					}

					for (bookmark in bookmarksInRange) {
						if (bookmarkTracked.containsKey(bookmark)) continue
						else {
							bookmarkTracked[bookmark] = generateBookmarkMapFeature(bookmark) ?: continue
						}
					}

					(dynamicFeatures.find { it.identifier == "THIS_SHIP" }?.featureDisplay as TextDisplay).text(
						ofChildren(
							Component.text(ship.type.icon, NamedTextColor.DARK_GREEN).font(getSidebarKeyToUse(ship)),
						)
					)

				} catch (_: Exception) {
				}
			}

			MapState.SYSTEMS_MAP -> {}
			MapState.GALACTIC_MAP -> {
				if (ship.playerPilot != null) {
					val pilot = ship.playerPilot!!
					val target = pilot.getTargetEntity(5, false)
					val buttonLookedAt = this.dynamicFeatures.find { it.entities.contains(target) }
					if (buttonLookedAt != null && buttonLookedAt is SystemDisplayButton) {
						pilot.sendActionBar(Component.text(buttonLookedAt.identifier, NamedTextColor.DARK_GREEN))
					}
				}
			}
		}
	}

	override fun despawn() {
		super.despawn()

		saveMapStateToLocation(location, state, maxDistance)

		stateMap = null
		this.shipsTracked.clear()
		this.beaconsTracked.clear()
		this.bookmarkTracked.clear()
		this.celestialBodiesTracked.clear()

	}

	private fun initializeMapBackgroundAndBorder() {
		//GALACTIC MAP
		commonFeatures.add(
			DisplayFeature(
				"BACKGROUND", this, 16.0 / 32.0, 16.0 / 32.0, 1.0, 1.0,
				ItemStack(Material.PAPER).updateData(
					DataComponentTypes.ITEM_MODEL,
					NamespacedKeys.packKey("map/black")
				),
				null,
				0.0
			),
		)

		commonFeatures.add(
			DisplayFeature(
				"BORDER", this, 14.0 / 32.0, 0.0, 1.0, 1.0, null,
				MapTextIcon.BORDER_RIGHT_MISSING.component(),
				.25
			)
		)
	}

	private fun setupMapSideBarButtons() {
		//GALACTIC MAP
		commonButtons.add(
			DisplayButtonFeature(
				"MAP_BUTTON", this, 30.5 / 32.0, 28.5 / 32.0, 3.0 / 32.0, 3.0 / 32.0,
				ItemStack(Material.PAPER).updateData(
					DataComponentTypes.ITEM_MODEL,
					NamespacedKeys.packKey("achievement_icon/hyperspace")
				),
				null,
				1.0
			) button@{
				if (it !is DisplayMap) return@button
				when (it.state) {
					MapState.LOCAL_MAP -> {
						//if the world is not creative world
						if (ConfigurationFiles.serverConfiguration().serverName?.lowercase()
								?.contains("creative") != true
						) {
							it.state = MapState.GALACTIC_MAP
							(it.commonButtons.find { it.identifier == "MAP_BUTTON" }?.getDisplayEntities()
								?.first() as? ItemDisplay)?.setItemStack(
								ItemStack(Material.PAPER).applyGuiModel(GuiItem.GENERIC_STARSHIP)
							)
							it.placeGalacticMap()
						}
					}

					MapState.GALACTIC_MAP -> {
						it.state = MapState.LOCAL_MAP
						val button = it.commonButtons.find { it.identifier == "MAP_BUTTON" }
						(button?.getDisplayEntities()?.first() as? ItemDisplay)?.setItemStack(button.itemStack)
						it.placeLocalMap()
					}

					MapState.SYSTEMS_MAP -> {
						it.state = MapState.GALACTIC_MAP
						(it.commonButtons.find { it.identifier == "MAP_BUTTON" }?.getDisplayEntities()
							?.first() as? ItemDisplay)?.setItemStack(
							ItemStack(Material.PAPER).applyGuiModel(GuiItem.GENERIC_STARSHIP)
						)
						it.placeGalacticMap()
					}
				}
			}
		)

		//PLUS BUTTON
		commonButtons.add(
			DisplayButtonFeature(
				"PLUS_BUTTON",
				this,
				30.5 / 32.0,
				25.5 / 32.0,
				3.0 / 32.0,
				3.0 / 32.0,
				ItemStack(Material.PAPER).applyGuiModel(GuiItem.PLUS),
				null,
				1.0,
			) button@{
				if (it !is DisplayMap) return@button
				it.maxDistance -= 2000.0
				if (maxDistance <= absoluteMinimumMaxDistance - 2000.0) {
					maxDistance = absoluteMinimumMaxDistance
				}
				(dynamicFeatures.find { it.identifier == "MAX_DISTANCE" }?.entities?.first() as? TextDisplay)?.text(
					Component.text("Square Size: ${maxDistance / 4.0}"),
				)
				ship.successAction("Set radius to ${maxDistance / 2.0}m")
			}
		)

		//MINUS BUTTON
		commonButtons.add(
			DisplayButtonFeature(
				"MINUS_BUTTON",
				this,
				30.5 / 32.0,
				22.5 / 32.0,
				3.0 / 32.0,
				3.0 / 32.0,
				ItemStack(Material.PAPER).applyGuiModel(GuiItem.MINUS),
				null,
				1.0,
			) button@{
				if (it !is DisplayMap) return@button
				it.maxDistance += 2000.0
				if (maxDistance >= absoluteMaxDistance + 2000.0) {
					maxDistance = absoluteMaxDistance
				}
				(dynamicFeatures.find { it.identifier == "MAX_DISTANCE" }?.entities?.first() as? TextDisplay)?.text(
					Component.text("Square Size: ${maxDistance / 4.0}"),
				)
				ship.successAction("Set radius to ${it.maxDistance / 2.0}m")
			}
		)
	}

	private fun placeLocalMap() {
		this.state = MapState.LOCAL_MAP
		dynamicFeatures.forEach { it.despawn() }
		dynamicFeatures.clear()
		val backgroundMap = DisplayFeature(
			"LOCAL_MAP",
			this,
			15.0 / 32.0,
			16.0 / 32.0,
			28.0 / 32.0,
			28.0 / 32.0,
			ItemStack(Material.PAPER).updateData(
				DataComponentTypes.ITEM_MODEL,
				NamespacedKeys.packKey("map/grid_lines")
			),
			null,
			.2
		)

		stateMap = backgroundMap

		val centralShipIcon = DisplayFeature(
			"THIS_SHIP",
			this,
			15.0 / 32.0,
			16.0 / 32.0,
			.04,
			.04,
			null,
			ofChildren(
				Component.text(ship.type.icon, NamedTextColor.DARK_GREEN).font(getSidebarKeyToUse(ship)),
				Component.text('\ueBF2').font(SPECIAL_FONT_KEY),
			),
			1.3
		)

		val maxDistanceMap = DisplayFeature(
			"MAX_DISTANCE",
			this,
			15.0 / 32.0,
			2.0 / 32.0,
			.03,
			.03,
			null,
			Component.text("Square Size: ${maxDistance / 4.0}"),
			1.2
		)

		dynamicFeatures.add(maxDistanceMap)
		dynamicFeatures.add(centralShipIcon)
		dynamicFeatures.add(backgroundMap)

		maxDistanceMap.init()
		backgroundMap.init()
		centralShipIcon.init()

		val centerOfMass = ship.centerOfMass.toVector()
		val world = this.location.world

		//Add Ships
		shipsInRange(maxDistance, ship).forEach { generateShipMapFeature(it) }
		//Add CelestialBodies
		celestialBodiesInRange(maxDistance, centerOfMass, world).forEach { generateCelestialBodyMapFeature(it) }
		//Add Beacons
		beaconsInRange(maxDistance, centerOfMass, world).forEach { generateBeaconMapFeature(it) }
		//Add BookMarks
		bookmarksInRange(this, maxDistance, centerOfMass, world).forEach { generateBookmarkMapFeature(it) }

		for (state in dynamicFeatures) {
			ship.entityPassengers.addAll(state.entities)
		}
	}

	private fun generateShipMapFeature(other: Starship): ShipDisplayFeature? {
		var color = ship.getRelation(other).color
		if (other.playerPilot != null && ship.playerPilot != null) {
			if (Fleets.findByMember(ship.playerPilot!!)?.contains(other.playerPilot!!) == true) {
				color = NamedTextColor.BLUE
			}
		}
		val shipScale = shipScale(this)
		//Get the ships icon
		val icon = other.type.icon

		val source = systemForSystemMap?.worldBorder?.center?.toVector() ?: Vector()

		//check if the body is out of range
		val offset = when (state) {
			MapState.LOCAL_MAP -> (ship.centerOfMass.minus(other.centerOfMass).toVector().setY(0)
				.multiply(1.0 / maxDistance))

			MapState.SYSTEMS_MAP -> ((source.add(other.centerOfMass.toVector().multiply(-1))).setY(0)
				.multiply(1.0 / (systemForSystemMap?.worldBorder?.size ?: 10000.0)))

			else -> Vector()
		}

		if (offset.length() > .5) {
			return null
		}

		val smf = ShipDisplayFeature(
			ship.getDisplayName().plainText(),
			this,
			.5 + offset.x,
			.5 + offset.z,
			shipScale,
			shipScale,
			ofChildren(
				MapTextIcon.ONE_PIXEL.component(),
				Component.text(icon, color).font(getSidebarKeyToUse(ship)),
				MapTextIcon.ONE_PIXEL.component(),
			),
			1.5,
			this.stateMap,
			Component.text(""),
			Color.fromARGB(color.asShadowColor(255).value()),
			other
		) {
		}
		dynamicFeatures.add(smf)
		smf.init()
		shipsTracked[other] = smf
		return smf
	}

	private fun generateCelestialBodyMapFeature(body: CelestialBody): CelestialBodyFeature? {
		val bodyScale = celestialBodyLocalMapScale(body, this)
		val source = systemForSystemMap?.worldBorder?.center?.toVector() ?: Vector()

		val offset = when (state) {
			MapState.LOCAL_MAP -> (ship.centerOfMass.toVector().add(body.location.toVector().multiply(-1))).setY(0)
				.multiply(1.0 / maxDistance)

			MapState.SYSTEMS_MAP -> ((source.add(body.location.toVector().multiply(-1))).setY(0)
				.multiply(1.0 / (systemForSystemMap?.worldBorder?.size ?: 10000.0)))

			else -> Vector()
		}

		if ((offset.x.absoluteValue + bodyScale / 4.0) > .5 || (offset.z.absoluteValue + bodyScale / 4) > .5) {
			return null
		}

		val identifier =
			(body as? NamedCelestialBody)?.name?.replaceFirstChar { it.uppercase() } ?: "UNKNOWN" //should never happen
		val itemStack: ItemStack? = when (body) {
			is CachedPlanet -> HudIcons.getItemStack(PLANET_PREFIX.plus(identifier.lowercase()))
			is CachedStar -> HudIcons.getItemStack(STAR_PREFIX.plus(identifier.lowercase()))
			else -> null
		}
		val component = null

		val cbf = CelestialBodyFeature(
			identifier.uppercase(),
			this,
			.5 + offset.x,
			.5 + offset.z,
			bodyScale,
			bodyScale,
			component,
			itemStack,
			1.3,
			this.stateMap!!,
			Component.text(identifier, null, BOLD),
			Color.fromARGB(0, 0, 0, 0),
			body
		) {
			val vertex = WaypointManager.getVertex(
				WaypointManager.playerGraphs[ship.playerPilot?.uniqueId ?: return@CelestialBodyFeature]
					?: return@CelestialBodyFeature, identifier.replaceFirstChar { it.uppercase() })
				?: return@CelestialBodyFeature
			WaypointCommand.addVertexToRoute(ship.playerPilot ?: return@CelestialBodyFeature, vertex)
		}
		dynamicFeatures.add(cbf)
		celestialBodiesTracked[body] = cbf
		cbf.init()
		return cbf
	}

	private fun generateBeaconMapFeature(beacon: ServerConfiguration.HyperspaceBeacon): BeaconDisplayFeature? {
		val source = systemForSystemMap?.worldBorder?.center?.toVector() ?: Vector()

		val beaconSize = 0.08

		val offset = when (state) {
			MapState.LOCAL_MAP -> (ship.centerOfMass.toVector().add(beacon.spaceLocation.toVector().multiply(-1))).setY(
				0
			).multiply(1.0 / maxDistance)

			MapState.SYSTEMS_MAP -> ((source.add(beacon.spaceLocation.toVector().multiply(-1))).setY(0)
				.multiply(1.0 / (systemForSystemMap?.worldBorder?.size ?: 10000.0)))

			else -> Vector()
		}

		if ((offset.x.absoluteValue) > .5 || (offset.z.absoluteValue) > .5) {
			return null
		}

		val bmf = BeaconDisplayFeature(
			beacon.name,
			this,
			.5 + offset.x,
			.5 + offset.z,
			beaconSize,
			beaconSize,
			null,
			ItemStack(Material.PAPER).applyGuiModel(GuiItem.BEACON),
			1.4,
			this.stateMap!!,
			Component.text(beacon.name, NamedTextColor.WHITE, BOLD),
			Color.fromARGB(0, 255, 255, 255),
			beacon
		) {
			ship.playerPilot?.performCommand("route add ${beacon.name} ${beacon.spaceLocation.x} ${beacon.spaceLocation.z}")
		}
		dynamicFeatures.add(bmf)
		beaconsTracked[beacon] = bmf
		bmf.init()
		return bmf
	}

	private fun generateBookmarkMapFeature(bookmark: Bookmark): BookmarkDisplayFeature? {
		val beaconScale = .06

		val source = systemForSystemMap?.worldBorder?.center?.toVector() ?: Vector()

		//check if the body is out of range
		val offset = when (state) {
			MapState.LOCAL_MAP -> (ship.centerOfMass.toVector().add(bookmark.toVector().multiply(-1))).setY(0)
				.multiply(1.0 / maxDistance)

			MapState.SYSTEMS_MAP -> ((source.add(bookmark.toVector().multiply(-1))).setY(0)
				.multiply(1.0 / (systemForSystemMap?.worldBorder?.size ?: 10000.0)))

			else -> Vector()
		}

		if ((offset.x.absoluteValue) > .5 || (offset.z.absoluteValue) > .5) {
			return null
		}

		val bmf = BookmarkDisplayFeature(
			bookmark.name,
			this,
			.5 + offset.x,
			.5 + offset.z,
			beaconScale,
			beaconScale,
			null,
			ItemStack(Material.PAPER).applyGuiModel(GuiItem.BOOKMARK),
			1.25,
			this.stateMap!!,
			Component.text(bookmark.name, NamedTextColor.BLACK, BOLD),
			Color.fromARGB(255, 255, 255, 255),
			bookmark
		) {
			val vertex = WaypointManager.getVertex(
				WaypointManager.playerGraphs[ship.playerPilot?.uniqueId ?: return@BookmarkDisplayFeature]
					?: return@BookmarkDisplayFeature, bookmark.name.lowercase()
			) ?: return@BookmarkDisplayFeature
			WaypointCommand.addVertexToRoute(ship.playerPilot ?: return@BookmarkDisplayFeature, vertex)
		}
		dynamicFeatures.add(bmf)
		bookmarkTracked[bookmark] = bmf
		bmf.init()
		return bmf
	}

	/*
	Generates the galactic map, with all the buttons for each system. 4 hours of work btw to get the offsets.
	Then I realized the offsets were all upside down, and I had to do 1-offsetY to get the correct one
	 */
	private fun placeGalacticMap() {
		this.state = MapState.GALACTIC_MAP
		dynamicFeatures.forEach { it.despawn() }
		dynamicFeatures.clear()

		//generates the background map
		val backgroundMap = DisplayFeature(
			"GALACTIC_MAP",
			this,
			15.0 / 32.0,
			16.0 / 32.0,
			28.0 / 32.0,
			28.0 / 32.0,
			ItemStack(Material.PAPER).updateData(
				DataComponentTypes.ITEM_MODEL,
				NamespacedKeys.packKey("map/systems")
			),
			null,
			.3
		)

		val clearRoutes = DisplayButtonFeature(
			"CLEAR_ROUTE",
			this,
			15.0 / 32.0,
			2.0 / 32.0,
			.04, .04,
			null,
			Component.text(
				"[/Clear Route]", NamedTextColor.RED, BOLD
			),
			1.0,
			null,
		) {
			if (ship.playerPilot != null) {
				WaypointCommand.onClearWaypoint(ship.playerPilot!!)
			}
		}

		dynamicFeatures.add(backgroundMap)
		dynamicFeatures.add(clearRoutes)
		stateMap = backgroundMap

		//WARD
		//-asteri
		dynamicFeatures.add(
			SystemDisplayButton(
				"ASTERI",
				this,
				.1,
				1.0 - .167,
				0.12,
				0.12,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"TRAVERSE",
				this,
				.207,
				1.0 - .084,
				58.0 / 1024.0,
				58.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"RESERVE",
				this,
				.19433,
				1.0 - .26172,
				58.0 / 1024.0,
				58.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"VENTURE",
				this,
				.0664,
				1.0 - .2715,
				58.0 / 1024.0,
				58.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		//BREACH
		//-TRENCH
		dynamicFeatures.add(
			SystemDisplayButton(
				"TRENCH",
				this,
				.34765,
				1.0 - .23,
				154.0 / 1024.0,
				154.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)

		dynamicFeatures.add(
			SystemDisplayButton(
				"HORIZON",
				this,
				.322,
				1.0 - .07422,
				58.0 / 1024.0,
				58.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"D-1LA",
				this,
				.44824,
				1.0 - .0752,
				58.0 / 1024.0,
				58.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)

		//MONOLITH
		//-ILIOS
		dynamicFeatures.add(
			SystemDisplayButton(
				"ILIOS",
				this,
				.64648,
				1.0 - .225,
				152.0 / 1024.0,
				152.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)

		dynamicFeatures.add(
			SystemDisplayButton(
				"XN-81",
				this,
				.71582,
				1.0 - .067383,
				58.0 / 1024.0,
				58.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"VXM-11",
				this,
				.85156,
				1.0 - .06445,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"IX-Q3",
				this,
				.78027,
				1.0 - .11816,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"DN-4V",
				this,
				.88476,
				1.0 - .1171875,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"CONDUIT",
				this,
				.83594,
				1.0 - .16115,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"GRX-5",
				this,
				.94726,
				1.0 - .1631,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"NTH-3",
				this,
				.77734,
				1.0 - .2041,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"RELIQUARY",
				this,
				.84668,
				1.0 - .2051,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"PDN-2",
				this,
				.9473,
				1.0 - .20898,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"XRW-9",
				this,
				.8584,
				1.0 - .25195,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"OQ-04",
				this,
				.9463,
				1.0 - .25195,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"TH-89",
				this,
				.765625,
				1.0 - .291,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)

		//FRACTURE
		//-REGULUS
		dynamicFeatures.add(
			SystemDisplayButton(
				"REGULUS",
				this,
				.25586,
				1.0 - .51,
				167.0 / 1024.0,
				167.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)

		dynamicFeatures.add(
			SystemDisplayButton(
				"MERIDIAN",
				this,
				.068356,
				1.0 - .5498,
				50.0 / 1024.0,
				50.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"LOA-7",
				this,
				.07324,
				1.0 - .6133,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"BQ-5A",
				this,
				.1641,
				1.0 - .6123,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"TT-91",
				this,
				.25293,
				1.0 - .6133,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"CXK-3",
				this,
				.0723,
				1.0 - .67383,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"QIM-8",
				this,
				.16406,
				1.0 - .671875,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"FAULT",
				this,
				.2783,
				1.0 - .68066,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"F3L-I",
				this,
				.072265,
				1.0 - .72070,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"KRY-2",
				this,
				.2334,
				1.0 - .73926,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"SUNDER",
				this,
				.3916,
				1.0 - .73926,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)

		//SPINE
		//-SIRIUS
		dynamicFeatures.add(
			SystemDisplayButton(
				"SIRIUS",
				this,
				.6128,
				1.0 - .575,
				163.0 / 1024.0,
				163.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)

		dynamicFeatures.add(
			SystemDisplayButton(
				"URT-8",
				this,
				.7207,
				1.0 - .36816,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"VERTIGO",
				this,
				.7207,
				1.0 - .416015,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"LM-77",
				this,
				.83886,
				1.0 - .4502,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"TNS-44",
				this,
				.76465,
				1.0 - .487305,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"HL-81",
				this,
				.76465,
				1.0 - .5303,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"AXA-2",
				this,
				.76465,
				1.0 - .57,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"KTR-18",
				this,
				.76465,
				1.0 - .609375,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"ANCHOR",
				this,
				.76465,
				1.0 - .651376,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"AXIS",
				this,
				.8779,
				1.0 - .506836,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"JCT-3",
				this,
				.90625,
				1.0 - .56641,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"PRM-16",
				this,
				.911133,
				1.0 - .647461,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)
		dynamicFeatures.add(
			SystemDisplayButton(
				"STR-29",
				this,
				.83789,
				1.0 - .706055,
				44.0 / 1024.0,
				44.0 / 1024.0,
				null,
				null,
				0.0,
				backgroundMap
			) {}
		)

		for (state in dynamicFeatures) {
			state.init()
			ship.entityPassengers.addAll(state.entities)
		}
		clearRoutes.interaction.interactionWidth = (.2f * this.sizeX).toFloat()

	}

	fun placeSystemsMap() {
		this.state = MapState.SYSTEMS_MAP
		dynamicFeatures.forEach { it.despawn() }
		dynamicFeatures.clear()
		if (systemForSystemMap == null) {
			ship.serverError("ERROR: World not found for system map, please alert staff!")
			return
		}

		val backgroundMap = DisplayFeature(
			"LOCAL_MAP",
			this,
			15.0 / 32.0,
			16.0 / 32.0,
			28.0 / 32.0,
			28.0 / 32.0,
			ItemStack(Material.PAPER).updateData(
				DataComponentTypes.ITEM_MODEL,
				NamespacedKeys.packKey("map/grid_lines")
			),
			null,
			.2
		)
		val maxDistanceMap = DisplayFeature(
			"WORLD_BORDER",
			this,
			15.0 / 32.0,
			2 / 32.0,
			.03,
			.03,
			null,
			Component.text("System Size: ${systemForSystemMap!!.worldBorder.size.toInt()}m"),
			2.0
		)

		dynamicFeatures.add(maxDistanceMap)
		dynamicFeatures.add(backgroundMap)

		maxDistanceMap.init()
		backgroundMap.init()

		val world = systemForSystemMap ?: return
		val source = world.worldBorder.center.toVector()
		planetInRange(1_000_000.0, source, world).forEach {
			generateCelestialBodyMapFeature(it)
		}
		starsInRange(1_000_000.0, source, world).forEach {
			generateCelestialBodyMapFeature(it)
		}
		beaconsInRange(1_000_000.0, source, world).forEach {
			generateBeaconMapFeature(it)
		}

		bookmarksInRange(this, 1_000_000.0, source, world).forEach {
			println("x")
			generateBookmarkMapFeature(it)
		}


		for (state in dynamicFeatures) {
			ship.entityPassengers.addAll(state.entities)
		}
	}

	/**
	 * Casts a ray against this map's display plane and, if it lands within the
	 * visible map square, returns the real-world location that point represents.
	 * Only meaningful for LOCAL_MAP / SYSTEMS_MAP, since those are the only states
	 * whose (rx, ry) placement is derived from a real world offset / maxDistance.
	 */
	fun getWorldClickLocation(rayOrigin: Vector, rayDirection: Vector): Location? {
		if (state != MapState.LOCAL_MAP && state != MapState.SYSTEMS_MAP) return null

		val (right, up) = displayBasis()
		val normal = dir.clone().normalize()
		val planePoint = displayLocation().toVector()

		val denominator = rayDirection.dot(normal)
		if (abs(denominator) < 1e-6) return null // ray parallel to the plane

		val t = (planePoint.clone().subtract(rayOrigin)).dot(normal) / denominator
		if (t < 0) return null // plane is behind the player

		val hitPoint = rayOrigin.clone().add(rayDirection.clone().multiply(t))
		val delta = hitPoint.clone().subtract(planePoint)

		// Inverts locationAtRelativeCoordinates()'s isTextDisplay = false branch:
		// point = displayLocation() + right*(-rx*sizeX) + up*(sizeY*ry + sizeY)
		val rx = -(delta.dot(right)) / sizeX
		val ry = (delta.dot(up)) / sizeY - 1.0

		// Bounds of the LOCAL_MAP/SYSTEMS_MAP background square:
		// anchored at rx=15/32, ry=16/32, spanning 28/32 x 28/32
		val mapCenterX = 15.0 / 32.0
		val mapCenterY = 16.0 / 32.0
		val mapHalfExtent = 14.0 / 32.0

		if (rx < mapCenterX - mapHalfExtent || rx > mapCenterX + mapHalfExtent) return null
		if (ry < mapCenterY - mapHalfExtent || ry > mapCenterY + mapHalfExtent) return null

		// Inverts: offset = (shipCenterOfMass - target) / maxDistance, rx = .5+offset.x, ry = .5+offset.z
		val offsetX = rx - 0.5
		val offsetZ = ry - 0.5

		val center = when (state) {
			MapState.LOCAL_MAP -> ship.centerOfMass.toVector()
			MapState.SYSTEMS_MAP -> systemForSystemMap?.worldBorder?.center?.toVector() ?: Vector()
			else -> Vector()
		}
		val distance = when (state) {
			MapState.LOCAL_MAP -> maxDistance
			MapState.SYSTEMS_MAP -> (systemForSystemMap?.worldBorder?.size ?: 10000.0)
			else -> 1000.0
		}
		val worldX = center.x + offsetX * distance
		val worldZ = center.z - offsetZ * distance

		return Location(location.world, worldX, 0.0, worldZ)
	}


	fun saveMapStateToLocation(location: Location, mapState: MapState, size: Double) : Boolean{
		try {
			val block = location.world.getBlockAt(location)
			val state = block.state as? Sign ?: return false
			val pdc = state.persistentDataContainer
			pdc.set(NamespacedKeys.MAP_STATE, PersistentDataType.STRING, mapState.name)
			pdc.set(NamespacedKeys.MAP_SIZE, PersistentDataType.DOUBLE, size)
			return state.update()
		}catch (_: Exception){
		}

		return false
	}

	fun loadMapStateFromLocation(location: Location): Triple<Boolean, MapState, Double>{
		try {
			val block = location.world.getBlockAt(location)
			val state = block.state as? Sign ?: return Triple(false,MapState.LOCAL_MAP, 1.0)
			val pdc = state.persistentDataContainer
			val mapState = pdc.get(NamespacedKeys.MAP_STATE, PersistentDataType.STRING)
			val size = pdc.get(NamespacedKeys.MAP_SIZE, PersistentDataType.DOUBLE)
			if (mapState ==null || size == null) return Triple(false,MapState.LOCAL_MAP, 1.0)
			val enumMapState = MapState.valueOf(mapState)
			return Triple(true, enumMapState, size)
		}catch (_: Exception){
		}
		return Triple(false,MapState.LOCAL_MAP, 1.0)
	}

	companion object : SLEventListener() {


		@EventHandler
		private fun onPlayerLeftClickInteration(event: PlayerInteractEvent) {
			if (event.hand != org.bukkit.inventory.EquipmentSlot.HAND) return
			if (!event.action.isLeftClick) return

			handlePlayerLeftClick(event.player)
		}

		fun handlePlayerLeftClick(player: Player) {
			val ship = ActiveStarships.findByPassenger(player) ?: return
			val eyeLocation = player.eyeLocation
			val rayOrigin = eyeLocation.toVector()
			val rayDirection = eyeLocation.direction.normalize()
			val maps = ship.displays.filterIsInstance<DisplayMap>()

			for (map in maps) {
				if (map.state != MapState.LOCAL_MAP && map.state != MapState.SYSTEMS_MAP) continue

				val worldPoint = map.getWorldClickLocation(rayOrigin, rayDirection) ?: continue

				if (map.state == MapState.LOCAL_MAP || map.systemForSystemMap == map.location.world) {
					player.sendActionBar(
						Component.text(
							"Shift + Punch to jump to: ${worldPoint.blockX}, ${worldPoint.blockZ}",
							NamedTextColor.DARK_PURPLE
						)
					)
					if (player.isSneaking) {
						player.performCommand("jump ${worldPoint.blockX} ${worldPoint.blockZ}")
					}
					return
				} else if (map.state == MapState.SYSTEMS_MAP) {
					player.sendActionBar(
						Component.text(
							"Shift + Punch to route to: ${map.systemForSystemMap?.name} ${worldPoint.blockX}, ${worldPoint.blockZ}",
							NamedTextColor.DARK_PURPLE
						)
					)
					if (player.isSneaking) {
						player.performCommand("route add ${map.systemForSystemMap?.name} ${worldPoint.blockX} ${worldPoint.blockZ}")
					}
					return
				}
			}
		}

		@EventHandler
		private fun onStarshipPilot(event: StarshipPilotedEvent) {
			//Don't let the player have more than 2 of these maps
			if ((ActiveStarships.findByPilot(event.player)?.displays?.filterIsInstance<DisplayMap>()?.size ?: 0) > 2) {
				event.starship.userError("Error: No more then 2 Display Maps are allowed aboard the ship")
				return
			}
			ActiveStarships.findByPilot(event.player)?.displays?.forEach { map -> map.init() }
		}
	}
}
