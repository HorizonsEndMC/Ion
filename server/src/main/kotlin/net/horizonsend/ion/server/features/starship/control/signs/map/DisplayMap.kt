	@file:Suppress("UnstableApiUsage")

	package net.horizonsend.ion.server.features.starship.control.signs.map

	import io.papermc.paper.datacomponent.DataComponentTypes
	import net.horizonsend.ion.common.extensions.successAction
	import net.horizonsend.ion.common.utils.miscellaneous.d
	import net.horizonsend.ion.common.utils.text.BOLD
	import net.horizonsend.ion.common.utils.text.SPECIAL_FONT_KEY
	import net.horizonsend.ion.common.utils.text.asShadowColor
	import net.horizonsend.ion.common.utils.text.ofChildren
	import net.horizonsend.ion.common.utils.text.plainText
	import net.horizonsend.ion.server.configuration.ServerConfiguration
	import net.horizonsend.ion.server.features.gui.GuiItem
	import net.horizonsend.ion.server.features.gui.GuiItem.Companion.applyGuiModel
	import net.horizonsend.ion.server.features.sidebar.Sidebar
	import net.horizonsend.ion.server.features.space.body.CachedStar
	import net.horizonsend.ion.server.features.space.body.CelestialBody
	import net.horizonsend.ion.server.features.space.body.NamedCelestialBody
	import net.horizonsend.ion.server.features.space.body.planet.CachedPlanet
	import net.horizonsend.ion.server.features.starship.Starship
	import net.horizonsend.ion.server.features.starship.active.ActiveStarships
	import net.horizonsend.ion.server.features.starship.control.signs.map.features.BeaconMapFeature
	import net.horizonsend.ion.server.features.starship.control.signs.map.features.CelestialBodyFeature
	import net.horizonsend.ion.server.features.starship.control.signs.map.features.MapButtonDisplay
	import net.horizonsend.ion.server.features.starship.control.signs.map.features.MapFeature
	import net.horizonsend.ion.server.features.starship.control.signs.map.features.ShipMapFeature
	import net.horizonsend.ion.server.features.starship.control.signs.map.features.SystemMapFeature
	import net.horizonsend.ion.server.features.starship.event.StarshipPilotedEvent
	import net.horizonsend.ion.server.features.starship.event.StarshipReleaseEvent
	import net.horizonsend.ion.server.features.starship.event.StarshipUnpilotEvent
	import net.horizonsend.ion.server.features.starship.event.movement.StarshipMoveEvent
	import net.horizonsend.ion.server.features.starship.event.movement.StarshipRotateEvent
	import net.horizonsend.ion.server.features.starship.event.movement.StarshipTranslateEvent
	import net.horizonsend.ion.server.features.starship.fleet.Fleets
	import net.horizonsend.ion.server.features.waypoint.WaypointManager
	import net.horizonsend.ion.server.features.waypoint.command.WaypointCommand
	import net.horizonsend.ion.server.listener.SLEventListener
	import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
	import net.horizonsend.ion.server.miscellaneous.utils.Tasks
	import net.horizonsend.ion.server.miscellaneous.utils.updateData
	import net.kyori.adventure.text.Component
	import net.kyori.adventure.text.format.NamedTextColor
	import org.bukkit.Color
	import org.bukkit.Location
	import org.bukkit.Material
	import org.bukkit.entity.EntityType
	import org.bukkit.entity.ItemDisplay
	import org.bukkit.entity.TextDisplay
	import org.bukkit.event.EventHandler
	import org.bukkit.event.EventPriority
	import org.bukkit.event.player.PlayerInteractEntityEvent
	import org.bukkit.inventory.ItemStack
	import org.bukkit.util.Vector
	import org.joml.Vector3d
	import org.joml.Vector3f
	import kotlin.math.abs

	class DisplayMap(val ship: Starship, var location: Location, var dir: Vector, val sizeX: Double, val sizeY: Double, val offset: Vector3d) {
		val shiftPerLayer = .005

		var state: MapState = MapState.LOCAL_MAP
		val absoluteMaxDistance = 10000.0
		val absoluteMinimumMaxDistance = 1000.0
		var maxDistance = 1000.0

		val shipsTracked = mutableMapOf<Starship, ShipMapFeature>()
		val celestialBodiesTracked = mutableMapOf<CelestialBody, CelestialBodyFeature>()
		val beaconTracked = mutableMapOf<ServerConfiguration.HyperspaceBeacon, BeaconMapFeature>()


		var stateMap: MapFeature? = null
		//Map features that are common to all the map states
		val commonFeatures: MutableList<MapFeature> = mutableListOf()
		val commonButtons = mutableListOf<MapButtonDisplay>()

		//buttons pertaining to the current state
		val mapStateFeatures = mutableListOf<MapFeature>()

		fun init() {
			initializeBackgroundAndBorder()
			setupSideBarButtons()
			placeLocalMap()
			//DO LAST

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

		fun tick() {
			if (state == MapState.LOCAL_MAP) {
				val shipsInRange = shipsInRange(maxDistance, ship)
				val centerOfMass = ship.centerOfMass.toVector()
				val bodiesInRange = celestialBodiesInRange(this, maxDistance, centerOfMass)
				for (ship in shipsInRange) {
					if (shipsTracked.containsKey(ship)) continue
					else {
						shipsTracked[ship] = generateShipMapFeature(ship)
					}
				}

				for(body in bodiesInRange) {
					if (celestialBodiesTracked.containsKey(body)) continue
					else{
						celestialBodiesTracked[body] = generateCelestialBodyMapFeature(body)
					}
				}

				mapStateFeatures.toList().forEach {
					it.tick()
				}
			}
		}

		private fun despawn() {
			mapStateFeatures.forEach { it.despawn() }
			commonFeatures.forEach { it.despawn() }
			commonButtons.forEach { it.despawn() }

			mapStateFeatures.clear()
			commonFeatures.clear()
			commonButtons.clear()
			this.shipsTracked.clear()
		}

		private fun initializeBackgroundAndBorder() {
			//GALACTIC MAP
			commonFeatures.add(
				MapFeature(
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
				MapFeature(
					"BORDER", this, 14.0/32.0,0.0,1.0,1.0, null,
					MapTextIcon.BORDER_RIGHT_MISSING.component(),
					1.0
				)
			)
		}

		private fun setupSideBarButtons() {
			//GALACTIC MAP
			commonButtons.add(
				MapButtonDisplay(
					"MAP_BUTTON", this, 30.5 / 32.0, 28.5 / 32.0, 3.0 / 32.0, 3.0 / 32.0,
					ItemStack(Material.PAPER).updateData(
						DataComponentTypes.ITEM_MODEL,
						NamespacedKeys.packKey("achievement_icon/hyperspace")
					),
					null,
					10.0
				) {
					when (it.state) {
						MapState.LOCAL_MAP -> {
							it.state = MapState.GALACTIC_MAP
							(it.commonButtons.find { it.identifier == "MAP_BUTTON" }?.getDisplayEntities()
								?.first() as? ItemDisplay)?.setItemStack(
								ItemStack(Material.PAPER).applyGuiModel(GuiItem.GENERIC_STARSHIP)
							)
							it.placeGalacticMap()
						}

						MapState.GALACTIC_MAP -> {
							it.state = MapState.LOCAL_MAP
							val button = it.commonButtons.find { it.identifier == "MAP_BUTTON" }
							(button?.getDisplayEntities()?.first() as? ItemDisplay)?.setItemStack(button.itemStack)
							it.placeLocalMap()
						}
					}
				}
			)

			//PLUS BUTTON
			commonButtons.add(
				MapButtonDisplay(
					"PLUS_BUTTON",
					this,
					30.5 / 32.0,
					25.5 / 32.0,
					3.0 / 32.0,
					3.0 / 32.0,
					ItemStack(Material.PAPER).applyGuiModel(GuiItem.PLUS),
					null,
					10.0,
				) {
					it.maxDistance += 1000.0
					if (maxDistance >= absoluteMaxDistance+1000.0) {
						maxDistance = absoluteMaxDistance
					}
					(mapStateFeatures.find { it.identifier == "MAX_DISTANCE"}?.entities?.first() as? TextDisplay)?.text(
						Component.text("Max Distance: ${maxDistance/2.0}")
					)
					ship.successAction("Set max distance to ${it.maxDistance/2.0}m")
				}
			)

			//MINUS BUTTON
			commonButtons.add(
				MapButtonDisplay(
					"MINUS_BUTTON",
					this,
					30.5 / 32.0,
					22.5 / 32.0,
					3.0 / 32.0,
					3.0 / 32.0,
					ItemStack(Material.PAPER).applyGuiModel(GuiItem.MINUS),
					null,
					10.0,
				) {
					it.maxDistance -= 1000.0
					if (maxDistance <= absoluteMinimumMaxDistance-1000.0) {
						maxDistance = absoluteMinimumMaxDistance
					}
					(mapStateFeatures.find { it.identifier == "MAX_DISTANCE"}?.entities?.first() as? TextDisplay)?.text(
						Component.text("Max Distance: ${maxDistance/2.0}")
					)
					ship.successAction("Set max distance to ${maxDistance/2.0}m")
				}
			)
		}

		private fun placeLocalMap() {
			mapStateFeatures.forEach { it.despawn() }
			mapStateFeatures.clear()

			val backgroundMap = MapFeature(
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
				1.2
			)

			stateMap = backgroundMap

			val centralShipIcon = MapFeature(
				"THIS_SHIP",
				this,
				15.0 / 32.0,
				16.0 / 32.0,
				.04,
				.04,
				null,
				ofChildren(
					Component.text(ship.type.icon, NamedTextColor.DARK_GREEN).font(Sidebar.fontKey),
					Component.text('\ueBF2').font(SPECIAL_FONT_KEY),
				),
				10.0
			)

			val maxDistanceMap = MapFeature(
				"MAX_DISTANCE",
				this,
				15.0/32.0,
				1.0/32.0,
				.03,
				.03,
				null,
				Component.text("Max Distance: $maxDistance"),
				5.1
			)

			mapStateFeatures.add(maxDistanceMap)
			mapStateFeatures.add(centralShipIcon)
			mapStateFeatures.add(backgroundMap)

			maxDistanceMap.init()
			backgroundMap.init()
			centralShipIcon.init()

			val centerOfMass = ship.centerOfMass.toVector()

			//Add Ships
			shipsInRange(maxDistance, ship).forEach { generateShipMapFeature(it) }
			//Add CelestialBodies
			celestialBodiesInRange(this, maxDistance, centerOfMass).forEach { generateCelestialBodyMapFeature(it) }
			//Add Beacons
			beaconsInRange(this, maxDistance,centerOfMass).forEach { generateBeaconMapFeature(it) }

			for (state in mapStateFeatures) {
				ship.entityPassengers.addAll(state.entities)
			}
		}

		private fun generateShipMapFeature(other: Starship): ShipMapFeature {
			var color = ship.getRelation(other).color
			if (other.playerPilot != null && ship.playerPilot != null) {
				if (Fleets.findByMember(ship.playerPilot!!)?.contains(other.playerPilot!!) == true) {
					color = NamedTextColor.BLUE
				}
			}
			val shipScale = shipScale(this)
			//Get the ships icon
			val icon = other.type.icon

			//find the offset of this ship from our ship
			val offset = (ship.centerOfMass.minus(other.centerOfMass).toVector().setY(0).multiply(1.0 / maxDistance))

			val smf = ShipMapFeature(
				ship.getDisplayName().plainText(),
				this,
				.5 + offset.x,
				.5 + offset.z,
				shipScale,
				shipScale,
				ofChildren(
					//\ueBF2 is a 1x1px character of literally nothing. Used for padding to avoid stretching below
					MapTextIcon.ONE_PIXEL.component(),
					Component.text(icon, color).font(Sidebar.fontKey),
					MapTextIcon.ONE_PIXEL.component(),
				),
				10.0,
				this.stateMap,
				Component.text(""),
				Color.fromARGB(color.asShadowColor(255).value()),
				other
			){
				ship.playerPilot?.performCommand("/jump ${location.x} ${location.z}")
			}
			mapStateFeatures.add(smf)
			smf.init()
			shipsTracked[other] = smf
			return smf
		}

		private fun generateCelestialBodyMapFeature(body: CelestialBody) : CelestialBodyFeature {
			val starScale = celestialBodyMapScale(body, this)
			val offset = (ship.centerOfMass.minus(body.location)).toVector().setY(0).multiply(1.0 / maxDistance)
			val identifier = (body as? NamedCelestialBody)?.name?.replaceFirstChar { it.uppercase() } ?: "UNKNOWN" //should never happen
			val itemStack: ItemStack? =
				when (body) {
					is CachedPlanet -> body.planetIconFactory.construct()
					is CachedStar -> body.starIconFactory.construct()
					else -> null
				}
			val component = null

			val cbf = CelestialBodyFeature(
				identifier.uppercase(),
				this,
				.5 + offset.x,
				.5 + offset.z,
				starScale,
				starScale,
				component,
				itemStack,
				9.9,
				this.stateMap!!,
				Component.text(identifier, null, BOLD),
				Color.fromARGB(0,0,0,0),
				body
			){
				val vertex = WaypointManager.getVertex(WaypointManager.playerGraphs[ship.playerPilot?.uniqueId?: return@CelestialBodyFeature] ?: return@CelestialBodyFeature, identifier.replaceFirstChar { it.uppercase() }) ?: return@CelestialBodyFeature
				WaypointCommand.addVertexToRoute(ship.playerPilot?: return@CelestialBodyFeature, vertex)
			}
			mapStateFeatures.add(cbf)
			celestialBodiesTracked[body] = cbf
			cbf.init()
			return cbf
		}

		private fun generateBeaconMapFeature(beacon: ServerConfiguration.HyperspaceBeacon) : BeaconMapFeature{
			val beaconScale = 100.0/maxDistance
			val offset = (ship.centerOfMass.toVector().add(beacon.spaceLocation.toVector().multiply(-1))).setY(0).multiply(1.0 / maxDistance)
			val bmf = BeaconMapFeature(
				beacon.name,
				this,
				.5 + offset.x,
				.5 + offset.z,
				beaconScale,
				beaconScale,
				null,
				ItemStack(Material.PAPER).applyGuiModel(GuiItem.BEACON),
				9.9,
				this.stateMap!!,
				Component.text(beacon.name, null, BOLD),
				Color.fromARGB(200, 255, 255, 255),
				beacon
			){
				val vertex = WaypointManager.getVertex(WaypointManager.playerGraphs[ship.playerPilot?.uniqueId?: return@BeaconMapFeature] ?: return@BeaconMapFeature, beacon.name.replaceFirstChar { it.uppercase() }) ?: return@BeaconMapFeature
				WaypointCommand.addVertexToRoute(ship.playerPilot?: return@BeaconMapFeature, vertex)
			}
			mapStateFeatures.add(bmf)
			beaconTracked[beacon] = bmf
			bmf.init()
			return bmf
		}

		/*
		Generates the galactic map, with all the buttons for each system. 4 hours of work btw to get the offsets.
		Then I realized the offsets were all upside down, and I had to do 1-offsetY to get the correct one
		 */
		private fun placeGalacticMap() {
			mapStateFeatures.forEach { it.despawn() }
			mapStateFeatures.clear()

			//generates the background map
			val backgroundMap = MapFeature(
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
				8.0
			)
			mapStateFeatures.add(backgroundMap)
			stateMap = backgroundMap

			//WARD
			//-asteri
			mapStateFeatures.add(
				SystemMapFeature(
					"ASTERI",
					this,
					.1,
					1.0 - .167,
					0.12,
					0.12,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"TRAVERSE",
					this,
					.207,
					1.0 - .084,
					58.0 / 1024.0,
					58.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"RESERVE",
					this,
					.19433,
					1.0 - .26172,
					58.0 / 1024.0,
					58.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"VENTURE",
					this,
					.0664,
					1.0 - .2715,
					58.0 / 1024.0,
					58.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			//BREACH
			//-TRENCH
			mapStateFeatures.add(
				SystemMapFeature(
					"TRENCH",
					this,
					.34765,
					1.0 - .23,
					154.0 / 1024.0,
					154.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)

			mapStateFeatures.add(
				SystemMapFeature(
					"HORIZON",
					this,
					.322,
					1.0 - .07422,
					58.0 / 1024.0,
					58.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"D-1LA",
					this,
					.44824,
					1.0 - .0752,
					58.0 / 1024.0,
					58.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)

			//MONOLITH
			//-ILIOS
			mapStateFeatures.add(
				SystemMapFeature(
					"ILIOS",
					this,
					.64648,
					1.0 - .225,
					152.0 / 1024.0,
					152.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)

			mapStateFeatures.add(
				SystemMapFeature(
					"XN-81",
					this,
					.71582,
					1.0 - .067383,
					58.0 / 1024.0,
					58.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"VXM-11",
					this,
					.85156,
					1.0 - .06445,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"IX-Q3",
					this,
					.78027,
					1.0 - .11816,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"DN-4V",
					this,
					.88476,
					1.0 - .1171875,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"CONDUIT",
					this,
					.83594,
					1.0 - .16115,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"GRX-5",
					this,
					.94726,
					1.0 - .1631,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"NTH-3",
					this,
					.77734,
					1.0 - .2041,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"RELIQUARY",
					this,
					.84668,
					1.0 - .2051,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"PDN-2",
					this,
					.9473,
					1.0 - .20898,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"XRW-9",
					this,
					.8584,
					1.0 - .25195,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"0Q-04",
					this,
					.9463,
					1.0 - .25195,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"TH-89",
					this,
					.765625,
					1.0 - .291,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)

			//FRACTURE
			//-REGULUS
			mapStateFeatures.add(
				SystemMapFeature(
					"REGULUS",
					this,
					.25586,
					1.0 - .51,
					167.0 / 1024.0,
					167.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)

			mapStateFeatures.add(
				SystemMapFeature(
					"VXM-11",
					this,
					.068356,
					1.0 - .5498,
					50.0 / 1024.0,
					50.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"LOA-7",
					this,
					.07324,
					1.0 - .6133,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"BQ-5A",
					this,
					.1641,
					1.0 - .6123,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"TT-91",
					this,
					.25293,
					1.0 - .6133,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"CXK-3",
					this,
					.0723,
					1.0 - .67383,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"QIM-8",
					this,
					.16406,
					1.0 - .671875,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"VXM-11",
					this,
					.2783,
					1.0 - .68066,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"F3L-1",
					this,
					.072265,
					1.0 - .72070,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"KRY-2",
					this,
					.2334,
					1.0 - .73926,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"SUNDER",
					this,
					.3916,
					1.0 - .73926,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)

			//SPINE
			//-SIRIUS
			mapStateFeatures.add(
				SystemMapFeature(
					"SIRIUS",
					this,
					.6128,
					1.0 - .575,
					163.0 / 1024.0,
					163.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)

			mapStateFeatures.add(
				SystemMapFeature(
					"URT-8",
					this,
					.7207,
					1.0 - .36816,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"VERTIGO",
					this,
					.7207,
					1.0 - .416015,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"LM-77",
					this,
					.83886,
					1.0 - .4502,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"TNS-44f",
					this,
					.76465,
					1.0 - .487305,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"HL-81",
					this,
					.76465,
					1.0 - .5303,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"KTR-18",
					this,
					.76465,
					1.0 - .609375,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"ANCHOR",
					this,
					.76465,
					1.0 - .651376,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"AXIS",
					this,
					.8779,
					1.0 - .506836,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"JCT-3",
					this,
					.90625,
					1.0 - .56641,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"PRM-16",
					this,
					.911133,
					1.0 - .647461,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)
			mapStateFeatures.add(
				SystemMapFeature(
					"STR-29",
					this,
					.83789,
					1.0 - .706055,
					44.0 / 1024.0,
					44.0 / 1024.0,
					null,
					null,
					8.0,
					backgroundMap
				) {}
			)

			for (state in mapStateFeatures) {
				state.init()
				ship.entityPassengers.addAll(state.entities)
			}
		}

		private val worldUpBasisVector = Vector(0.0, 1.0, 0.0)

		/*
		Builds the local (right, up) basis for the display plane from its facing direction `dir`.
		`right` is derived from dir × worldUp, so it stays level with the horizon (pure yaw).
		`up` is derived from right × dir, so it tilts correctly as dir gains a y-component from pitch,
		instead of always being assumed to equal (0,1,0) like before.
		*/
		private fun displayBasis(): Pair<Vector, Vector> {
			val forward = dir.clone().normalize()

			// Fallback axis for when dir is (near) straight up/down, where forward x worldUp
			// collapses to a zero vector and can't be normalized.
			val reference = if (abs(forward.y) > 0.999) Vector(0.0, 0.0, 1.0) else worldUpBasisVector

			val right = forward.clone().crossProduct(reference).normalize()
			val up = right.clone().crossProduct(forward).normalize()
			return right to up
		}

		/*
		The following maths serves to center a given displayEntity onto the center of the location given.
		The maths is most useful for a given text display of sizeX & sizeY, as it will center the center of the text display
		onto the center of the block. Now respects full 3D rotation (yaw + pitch), not just yaw.
		*/
		fun displayLocation(): Location {
			val (right, up) = displayBasis()
			val oppositeDir = dir.clone().multiply(-1)

			val shipRight = ship.forward.direction.normalize().crossProduct( if (abs(ship.forward.direction.normalize().y) > 0.999) Vector(0.0, 0.0, 1.0) else worldUpBasisVector)
			val shipOpposite = ship.forward.oppositeFace.direction

			return location.clone().add(
				// centers the displayEntity onto the center of the block
				Vector(0.5, 0.0, 0.5)
					.add(oppositeDir.clone().multiply(-0.25))
					.add(right.clone().multiply(0.5 * sizeX))
					.add(up.clone().multiply(-sizeY))
			).add(
				shipRight.clone().multiply(offset.x)
					.add(up.clone().multiply(offset.y))
					.add(shipOpposite.clone().multiply(offset.z))
			)
		}

		/**
		 * This function generates a location on the map, from the relative coordinates provided.
		 * Values for x and y must be between 0 and 1
		 * An example for its usage would be rx = .5, ry = .5. Which would be the center of the display.
		 *
		 * @rx: relative x
		 * @ry: relative y
		 * @isTextDisplay: shifts the returned location down by a pixel to account for the padding minecraft adds to text
		 * @return the location to set as the basis of the feature.
		 */
		fun locationAtRelativeCoordinates(rx: Double, ry: Double, isTextDisplay: Boolean): Location {
			val (right, up) = displayBasis()

			return displayLocation().clone().add(
				right.clone().multiply(-rx * sizeX)
					.add(up.clone().multiply(
						(sizeY * ry) + sizeY - 0.05 * sizeY * (if (isTextDisplay) 1.0 else 0.0)
					))
			)
		}

		fun processMovement(event: StarshipMoveEvent) {
			Tasks.sync {
				if (event is StarshipTranslateEvent) {
					this.location.add(Vector(event.x, event.y, event.z))
					this.location.world = event.ship.world
					this.tick()
				} else if (event is StarshipRotateEvent) {
					val rotation = (Math.PI / 2.0) * if (event.clockwise) -1.0 else 1.0
					this.dir = this.dir.clone().rotateAroundY(rotation)

					val oldX = this.location.x.toInt()
					val oldY = this.location.y.toInt()
					val oldZ = this.location.z.toInt()
					this.location = Location(
						event.ship.world,
						event.movement.displaceX(oldX, oldZ).d(),
						event.movement.displaceY(oldY).d(),
						event.movement.displaceZ(oldZ, oldX).d()
					)
					this.tick()
				}
			}
		}

		companion object : SLEventListener() {
			fun Vector3d.toVector3f() = Vector3f(this.x().toFloat(), this.y().toFloat(), this.z().toFloat())

			@EventHandler
			private fun onPlayerInteractWithInteraction(event: PlayerInteractEntityEvent) {
				val interaction = event.rightClicked
				if (interaction.type != EntityType.INTERACTION) return
				val player = event.player
				val ship = ActiveStarships.findByPassenger(player) ?: return
				//find the map that the interaction entity belongs too
				val map = ship.displayMaps.find {
					it.commonButtons.find { button -> button.interaction == interaction } != null || it.mapStateFeatures.filterIsInstance<MapButtonDisplay>()
						.find { button -> button.interaction == interaction } != null
				} ?: return
				//Find the button from the specified interaction entity inside the map
				val button = map.commonButtons.find { it.interaction == interaction }
					?: map.mapStateFeatures.filterIsInstance<MapButtonDisplay>()
						.find { it.interaction == interaction } ?: return
				button.onClick()
			}

			@EventHandler(priority = EventPriority.LOWEST)
			private fun onStarshipRelease(event: StarshipReleaseEvent) {
				event.starship.displayMaps.forEach { map -> map.despawn() }
			}

			@EventHandler(priority = EventPriority.LOWEST)
			private fun onStarshipUnpilot(event: StarshipUnpilotEvent) {
				event.starship.displayMaps.forEach { map -> map.despawn() }
			}

			@EventHandler
			private fun onStarshipPilot(event: StarshipPilotedEvent) {
				ActiveStarships.findByPilot(event.player)?.displayMaps?.forEach { map -> map.init() }
			}
		}
	}
