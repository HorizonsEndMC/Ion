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
import net.minecraft.world.entity.Interaction
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
import org.joml.Vector3f

class DisplayMap(val ship: Starship, val location: Location, val dir: Vector, val sizeX: Float, val sizeY: Float) {
	val shiftPerLayer = 0.0025

	val oppositeDir = dir.clone().multiply(-1)
	val dx = oppositeDir.x
	val dz = oppositeDir.z

	var state: MapState = MapState.LOCAL_MAP
	var zoom: Float = 1.0f

	val displayLocation =
		location.add(//the following vector maths, centers the displayEntity onto the center of the block
			Vector(
				0.5 - 0.25 * dx + 0.5 * sizeX * dz,
				-sizeY.toDouble(),
				0.5 - 0.25 * dz - 0.5 * sizeX * dx
			)
		)

	//Only works for text Displays 1 px wide & tall
	val singleCharacterTextDisplayTransformation = Transformation(
		Vector3f(),
		ClientDisplayEntities.rotateToFaceVector(dir.toVector3f()),
		Vector3f(40f * sizeX, 40 * sizeY, 0f),
		Quaternionf()
	)

	val border = ship.world.spawnEntity(
		displayLocation.clone().add(
			dir.clone().multiply(shiftPerLayer * 1)
		),
		EntityType.TEXT_DISPLAY
	) as TextDisplay

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
		ship.entityPassengers.add(this.border)
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
				"BACKGROUND", this, 16f / 32f, 16f / 32f, 1f, 1f,
				ItemStack(Material.PAPER).updateData(
					DataComponentTypes.ITEM_MODEL,
					NamespacedKeys.packKey("map/black")
				),
				0f
			),
		)

		border.backgroundColor = Color.fromARGB(0, 0, 0, 0)
		border.brightness = Display.Brightness(15, 0)
		border.transformation = singleCharacterTextDisplayTransformation.clone()
		border.displayHeight
		border.teleportDuration = 0
		border.text(Component.text('\uEBF1').font(SPECIAL_FONT_KEY))
		ship.playerPilot?.give(ItemStack(Material.PAPER).updateData(
			DataComponentTypes.ITEM_MODEL,
			NamespacedKeys.packKey("map/black")
		))
	}

	private fun setupSideBarButtons() {
		//GALACTIC MAP
		commonButtons.add(
			MapButtonDisplay(
				"MAP_BUTTON", this, 30.5f / 32.0f, 28.5f / 32.0f, 3f / 32, 3f / 32,
				ItemStack(Material.PAPER).updateData(
					DataComponentTypes.ITEM_MODEL,
					NamespacedKeys.packKey("achievement_icon/hyperspace")
				),
				4f
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
				30.5f / 32.0f,
				25.5f / 32.0f,
				3f / 32,
				3f / 32,
				ItemStack(Material.PAPER).applyGuiModel(GuiItem.PLUS),
				4f
			) {
				it.zoom += 1f;
			}
		)

		//MINUS BUTTON
		commonButtons.add(
			MapButtonDisplay(
				"MINUS_BUTTON",
				this,
				30.5f / 32.0f,
				22.5f / 32.0f,
				3 / 32f,
				3f / 32,
				ItemStack(Material.PAPER).applyGuiModel(GuiItem.MINUS),
				4f
			) {
				it.zoom -= 1f;
			}
		)
	}

	//Generates the galactic map, with all the buttons for each system
	private fun placeGalacticMap() {
		mapStateFeatures.forEach { it.despawn() }
		mapStateFeatures.clear()

		//generates the background map
		val backgroundMap = MapFeature(
			"GALACTIC_MAP",
			this,
			15f / 32.0f,
			16f / 32.0f,
			28f / 32f,
			28f / 32f,
			ItemStack(Material.PAPER).updateData(
				DataComponentTypes.ITEM_MODEL,
				NamespacedKeys.packKey("map/systems")
			),
			8f
		)
		mapStateFeatures.add(backgroundMap)

		//WARD
		//-asteri
		mapStateFeatures.add(
			MapButtonDisplay(
				"ASTERI",
				this,
				.1f,
				1f-.1465f,
				0.12f,
				0.12f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"TRAVERSE",
				this,
				.207f,
				1f-.084f,
				58f / 1024f,
				58f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"RESERVE",
				this,
				.19433f,
				1f-.26172f,
				58f / 1024f,
				58f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"VENTURE",
				this,
				.0664f,
				1f-.2715f,
				58f / 1024f,
				58f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		//BREACH
		//-TRENCH
		mapStateFeatures.add(
			MapButtonDisplay(
				"TRENCH",
				this,
				.34765f,
				1f-.2109375f,
				154f / 1024f,
				154f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)

		mapStateFeatures.add(
			MapButtonDisplay(
				"HORIZON",
				this,
				.322f,
				1f-.07422f,
				58f / 1024f,
				58f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"D-1LA",
				this,
				.44824f,
				1f-.0752f,
				58f / 1024f,
				58f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)

		//MONOLITH
		//-ILIOS
		mapStateFeatures.add(
			MapButtonDisplay(
				"ILIOS",
				this,
				.64648f,
				1f-.19043f,
				152f / 1024f,
				152f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)

		mapStateFeatures.add(
			MapButtonDisplay(
				"XN-81",
				this,
				.71582f,
				1f-.067383f,
				58f / 1024f,
				58f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"VXM-11",
				this,
				.85156f,
				1f-.06445f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"IX-Q3",
				this,
				.78027f,
				1f-.11816f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"DN-4V",
				this,
				.88476f,
				1f-.1171875f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"CONDUIT",
				this,
				.83594f,
				1f-.16115f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"GRX-5",
				this,
				.94726f,
				1f-.1631f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"NTH-3",
				this,
				.77734f,
				1f-.2041f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"RELIQUARY",
				this,
				.84668f,
				1f-.2051f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"PDN-2",
				this,
				.9473f,
				1f-.20898f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"XRW-9",
				this,
				.8584f,
				1f-.25195f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"0Q-04",
				this,
				.9463f,
				1f-.25195f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"TH-89",
				this,
				.765625f,
				1f-.291f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)

		//FRACTURE
		//-REGULUS
		mapStateFeatures.add(
			MapButtonDisplay(
				"REGULUS",
				this,
				.25586f,
				1f-.47168f,
				167f / 1024f,
				167f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)

		mapStateFeatures.add(
			MapButtonDisplay(
				"VXM-11",
				this,
				.068356f,
				1f-.5498f,
				50f / 1024f,
				50f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"LOA-7",
				this,
				.07324f,
				1f-.6133f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"BQ-5A",
				this,
				.1641f,
				1f-.6123f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"TT-91",
				this,
				.25293f,
				1f-.6133f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"CXK-3",
				this,
				.0723f,
				1f-.67383f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"QIM-8",
				this,
				.16406f,
				1f-.671875f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"VXM-11",
				this,
				.2783f,
				1f-.68066f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"F3L-1",
				this,
				.072265f,
				1f-.72070f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"KRY-2",
				this,
				.2334f,
				1f-.73926f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"SUNDER",
				this,
				.3916f,
				1f-.73926f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)

		//SPINE
		//-SIRIUS
		mapStateFeatures.add(
			MapButtonDisplay(
				"SIRIUS",
				this,
				.6128f,
				1f-.5298f,
				163f / 1024f,
				163f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)

		mapStateFeatures.add(
			MapButtonDisplay(
				"URT-8",
				this,
				.7207f,
				1f-.36816f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"VERTIGO",
				this,
				.7207f,
				1f-.416015f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"LM-77",
				this,
				.83886f,
				1f-.4502f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"TNS-44f",
				this,
				.76465f,
				1f-.487305f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"HL-81",
				this,
				.76465f,
				1f-.5303f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"KTR-18",
				this,
				.76465f,
				1f-.609375f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"ANCHOR",
				this,
				.76465f,
				1f-.651376f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"AXIS",
				this,
				.8779f,
				1f-.506836f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"JCT-3",
				this,
				.90625f,
				1f-.56641f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"PRM-16",
				this,
				.911133f,
				1f-.647461f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)
		mapStateFeatures.add(
			MapButtonDisplay(
				"STR-29",
				this,
				.83789f,
				1f-.706055f,
				44f / 1024f,
				44f / 1024f,
				ItemStack(Material.AIR),
				8f,
				backgroundMap
			) {}
		)

		for (state in mapStateFeatures) {
			state.init()
			ship.entityPassengers.add(state.itemDisplay ?: continue)
			ship.entityPassengers.add((state as? MapButtonDisplay)?.interaction ?: continue)
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
	fun locationAtRelativeCoordinates(rx: Float, ry: Float, isTextDisplay: Boolean): Location {
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
