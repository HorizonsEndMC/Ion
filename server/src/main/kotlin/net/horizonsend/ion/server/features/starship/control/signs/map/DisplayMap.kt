@file:Suppress("UnstableApiUsage")

package net.horizonsend.ion.server.features.starship.control.signs.map

import io.papermc.paper.datacomponent.DataComponentTypes
import net.horizonsend.ion.common.utils.text.SPECIAL_FONT_KEY
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItem.Companion.applyGuiModel
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.updateData
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.TextDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3d
import org.joml.Vector3f

class DisplayMap(val ship: Starship, val location: Location, val dir: Vector, val sizeX: Double, val sizeY: Double) {
	val shiftPerLayer = 0.0025

	val oppositeDir = dir.clone().multiply(-1)
	val dx = oppositeDir.x
	val dz = oppositeDir.z

	var state: MapState = MapState.LOCAL_MAP
	var zoom: Double = 1.0

	val displayLocation =
		location.add(//the following vector maths, centers the displayEntity onto the center of the block
			Vector(
				0.5 - 0.25 * dx + 0.5 * sizeX * dz,
				-sizeY,
				0.5 - 0.25 * dz - 0.5 * sizeX * dx
			)
		)

	//Only works for text Displays 1 px wide & tall
	val singleCharacterTextDisplayTransformation = Transformation(
		Vector3f(),
		ClientDisplayEntities.rotateToFaceVector(dir.toVector3f()),
		Vector3d(40.0 * sizeX, 40.0 * sizeY, 0.0).toVector3f(),
		Quaternionf()
	)

	//Map features that are common to all the map states
	val commonFeatures: MutableList<MapFeature> = mutableListOf()
	val commonButtons = mutableListOf<MapButtonDisplay>()

	//buttons pertaining to the current state
	val mapStateFeatures = mutableListOf<MapFeature>()

	init {
		initializeBackgroundAndBorder()
		setupSideBarButtons()
		//placeGalacticMap()

		//DO LAST

		//Add entities to ship
		for (mapFeatures in commonFeatures) {
			mapFeatures.init()
			ship.entityPassengers.add(mapFeatures.itemDisplay ?: continue)
		}
		for (mapButtonDisplay in commonButtons) {
			mapButtonDisplay.init()
			ship.entityPassengers.add(mapButtonDisplay.itemDisplay ?: continue)
			ship.entityPassengers.add(mapButtonDisplay.interaction)
		}
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
				0.0
			),
		)

		val border = ship.world.spawnEntity(
			displayLocation.clone().add(
				dir.clone().multiply(shiftPerLayer * 1.0)
			),
			EntityType.TEXT_DISPLAY
		) as TextDisplay

		border.backgroundColor = Color.fromARGB(0, 0, 0, 0)
		border.brightness = Display.Brightness(15, 0)
		border.transformation = singleCharacterTextDisplayTransformation.clone()
		border.displayHeight
		border.teleportDuration = 0
		border.text(Component.text('\uEBF1').font(SPECIAL_FONT_KEY))
		ship.playerPilot?.give(
			ItemStack(Material.PAPER).updateData(
				DataComponentTypes.ITEM_MODEL,
				NamespacedKeys.packKey("map/black")
			)
		)

		ship.entityPassengers.add(border)
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
				4.0
			) {
				when (it.state) {
					MapState.LOCAL_MAP -> {
						it.state = MapState.GALACTIC_MAP
						it.commonButtons.find { it.identifier == "MAP_BUTTON" }?.itemDisplay?.setItemStack(
							ItemStack(Material.PAPER).applyGuiModel(GuiItem.GENERIC_STARSHIP)
						)
						it.placeGalacticMap()
					}

					MapState.GALACTIC_MAP -> {
						it.state = MapState.LOCAL_MAP
						val button = it.commonButtons.find { it.identifier == "MAP_BUTTON" }
						button?.itemDisplay?.setItemStack(button.itemStack)
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
				4.0
			) {
				it.zoom += 1.0;
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
				4.0
			) {
				it.zoom -= 1.0;
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
				NamespacedKeys.packKey("map/black")
			),
			8.0
		)
		mapStateFeatures.add(backgroundMap)
	}

	//Generates the galactic map, with all the buttons for each system
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
			8.0
		)
		mapStateFeatures.add(backgroundMap)

		//WARD
		//-asteri
		mapStateFeatures.add(
			MapButtonDisplay(
				"ASTERI",
				this,
				.1,
				1.0 - .167,
				0.12,
				0.12,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"TRAVERSE",
				this,
				.207,
				1.0 - .084,
				58.0 / 1024.0,
				58.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"RESERVE",
				this,
				.19433,
				1.0 - .26172,
				58.0 / 1024.0,
				58.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"VENTURE",
				this,
				.0664,
				1.0 - .2715,
				58.0 / 1024.0,
				58.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		//BREACH
		//-TRENCH
		mapStateFeatures.add(
			MapButtonDisplay(
				"TRENCH",
				this,
				.34765,
				1.0 - .23,
				154.0 / 1024.0,
				154.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)

		mapStateFeatures.add(
			MapButtonDisplay(
				"HORIZON",
				this,
				.322,
				1.0 - .07422,
				58.0 / 1024.0,
				58.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"D-1LA",
				this,
				.44824,
				1.0 - .0752,
				58.0 / 1024.0,
				58.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)

		//MONOLITH
		//-ILIOS
		mapStateFeatures.add(
			MapButtonDisplay(
				"ILIOS",
				this,
				.64648,
				1.0 - .225,
				152.0 / 1024.0,
				152.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)

		mapStateFeatures.add(
			MapButtonDisplay(
				"XN-81",
				this,
				.71582,
				1.0 - .067383,
				58.0 / 1024.0,
				58.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"VXM-11",
				this,
				.85156,
				1.0 - .06445,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"IX-Q3",
				this,
				.78027,
				1.0 - .11816,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"DN-4V",
				this,
				.88476,
				1.0 - .1171875,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"CONDUIT",
				this,
				.83594,
				1.0 - .16115,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"GRX-5",
				this,
				.94726,
				1.0 - .1631,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"NTH-3",
				this,
				.77734,
				1.0 - .2041,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"RELIQUARY",
				this,
				.84668,
				1.0 - .2051,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"PDN-2",
				this,
				.9473,
				1.0 - .20898,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"XRW-9",
				this,
				.8584,
				1.0 - .25195,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"0Q-04",
				this,
				.9463,
				1.0 - .25195,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"TH-89",
				this,
				.765625,
				1.0 - .291,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)

		//FRACTURE
		//-REGULUS
		mapStateFeatures.add(
			MapButtonDisplay(
				"REGULUS",
				this,
				.25586,
				1.0 - .51,
				167.0 / 1024.0,
				167.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)

		mapStateFeatures.add(
			MapButtonDisplay(
				"VXM-11",
				this,
				.068356,
				1.0 - .5498,
				50.0 / 1024.0,
				50.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"LOA-7",
				this,
				.07324,
				1.0 - .6133,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"BQ-5A",
				this,
				.1641,
				1.0 - .6123,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"TT-91",
				this,
				.25293,
				1.0 - .6133,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"CXK-3",
				this,
				.0723,
				1.0 - .67383,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"QIM-8",
				this,
				.16406,
				1.0 - .671875,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"VXM-11",
				this,
				.2783,
				1.0 - .68066,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"F3L-1",
				this,
				.072265,
				1.0 - .72070,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"KRY-2",
				this,
				.2334,
				1.0 - .73926,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"SUNDER",
				this,
				.3916,
				1.0 - .73926,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)

		//SPINE
		//-SIRIUS
		mapStateFeatures.add(
			MapButtonDisplay(
				"SIRIUS",
				this,
				.6128,
				1.0 - .575,
				163.0 / 1024.0,
				163.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)

		mapStateFeatures.add(
			MapButtonDisplay(
				"URT-8",
				this,
				.7207,
				1.0 - .36816,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"VERTIGO",
				this,
				.7207,
				1.0 - .416015,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"LM-77",
				this,
				.83886,
				1.0 - .4502,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"TNS-44f",
				this,
				.76465,
				1.0 - .487305,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"HL-81",
				this,
				.76465,
				1.0 - .5303,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"KTR-18",
				this,
				.76465,
				1.0 - .609375,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"ANCHOR",
				this,
				.76465,
				1.0 - .651376,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"AXIS",
				this,
				.8779,
				1.0 - .506836,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"JCT-3",
				this,
				.90625,
				1.0 - .56641,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"PRM-16",
				this,
				.911133,
				1.0 - .647461,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"STR-29",
				this,
				.83789,
				1.0 - .706055,
				44.0 / 1024.0,
				44.0 / 1024.0,
				ItemStack(Material.AIR),
				8.0,
				backgroundMap
			) {}
		)

		for (state in mapStateFeatures) {
			state.init()
			if (state is MapButtonDisplay) ship.entityPassengers.add(state.interaction)
			ship.entityPassengers.add(state.itemDisplay ?: continue)
		}
	}

	/**
	 * This function generates a location on the map, from the relative coordinates provided.
	 * Values for x and y must be between 0 and 1
	 *
	 * @rx: relative x
	 * @ry: relative y
	 * @isTextDisplay: shifts the returned location down by a pixel to account for the padding minecraft adds to text
	 * @return the location to set as the basis of the button.
	 */
	fun locationAtRelativeCoordinates(rx: Double, ry: Double, isTextDisplay: Boolean): Location {
		val isTextDisplay = isTextDisplay
		Tasks.async { }
		return displayLocation.clone().add(
			Vector(
				rx * sizeX * -dz,
				(sizeY * ry) + sizeY - 0.05 * sizeY * (if (isTextDisplay) 1.0 else 0.0),
				-rx * sizeX * -dx
			)
		)
	}

	companion object : SLEventListener() {
		private fun Transformation.clone(): Transformation =
			Transformation(this.translation, this.leftRotation, this.scale, this.rightRotation)

		fun Vector3d.toVector3f() = Vector3f(this.x().toFloat(), this.y().toFloat(), this.z().toFloat())


		@EventHandler
		private fun onPlayerInteractWithInteraction(event: PlayerInteractEntityEvent) {
			val interaction = event.rightClicked
			if (interaction.type != EntityType.INTERACTION) return
			val player = event.player
			val ship = ActiveStarships.findByPassenger(player) ?: return
			val map = ship.displayMaps.find {
				it.commonButtons.find { it.interaction == interaction } != null || it.mapStateFeatures.filterIsInstance<MapButtonDisplay>()
					.find { it.interaction == interaction } != null
			} ?: return
			val button = map.commonButtons.find { it.interaction == interaction }
				?: map.mapStateFeatures.filterIsInstance<MapButtonDisplay>()
					.find { it.interaction == interaction } ?: return
			button.onClick()
		}
	}
}
