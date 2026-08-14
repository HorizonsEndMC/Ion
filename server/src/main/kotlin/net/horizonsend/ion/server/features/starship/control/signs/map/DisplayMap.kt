@file:Suppress("UnstableApiUsage")

package net.horizonsend.ion.server.features.starship.control.signs.map

import io.papermc.paper.datacomponent.DataComponentTypes
import net.horizonsend.ion.common.utils.text.SPECIAL_FONT_KEY
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItem.Companion.applyGuiModel
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.updateData
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.TextDisplay
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

	//Map features
	val mapFeatures: MutableList<MapFeature> = mutableListOf()
	val mapButtonDisplays = mutableListOf<MapButtonDisplay>()

	init {
		initializeBackgroundAndBorder()
		setupSideBarButtons()
		placeMap()

		//DO LAST

		//Add entities to ship
		ship.entityPassengers.add(this.border)
		for(mapFeatures in mapFeatures) {
			mapFeatures.init()
			ship.entityPassengers.add(mapFeatures.itemDisplay ?: continue)
		}
		for(mapButtonDisplay in mapButtonDisplays) {
			mapButtonDisplay.init()
			ship.entityPassengers.add(mapButtonDisplay.itemDisplay ?: continue)
			ship.entityPassengers.add(mapButtonDisplay.interaction)
		}
	}

	private fun initializeBackgroundAndBorder() {
		//GALACTIC MAP
		mapFeatures.add(
			MapFeature(
				"BACKGROUND", this, 16f/32f,16f/32f, 1f, 1f,
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
	}

	private fun setupSideBarButtons() {
		//GALACTIC MAP
		mapButtonDisplays.add(
			MapButtonDisplay(
				"MAP_BUTTON", this, 30.5f / 32.0f, 28.5f / 32.0f, 3f/32, 3f/32,
				ItemStack(Material.PAPER).updateData(
					DataComponentTypes.ITEM_MODEL,
					NamespacedKeys.packKey("achievement_icon/hyperspace")
				),
				4f
			)
		)

		//PLUS BUTTON
		mapButtonDisplays.add(
			MapButtonDisplay(
				"PLUS_BUTTON",
				this,
				30.5f / 32.0f,
				25.5f / 32.0f,
				3f/32,
				3f/32,
				ItemStack(Material.PAPER).applyGuiModel(GuiItem.PLUS),
				4f
			),
		)

		//MINUS BUTTON
		mapButtonDisplays.add(
			MapButtonDisplay(
				"MINUS_BUTTON",
				this,
				30.5f / 32.0f,
				22.5f / 32.0f,
				3/32f,
				3f/32,
				ItemStack(Material.PAPER).applyGuiModel(GuiItem.MINUS),
				4f
			)
		)
	}


	private fun placeMap() {
		mapFeatures.add(
			MapFeature(
			"GALACTIC_MAP",
			this,
			15f / 32.0f,
			16f / 32.0f,
			28f/32f,
			28f/32,
				ItemStack(Material.PAPER).updateData(
					DataComponentTypes.ITEM_MODEL,
					NamespacedKeys.packKey("map/systems")
				),
				8f
			)
		)
	}

	/**
	 * This function generates a location to teleport entities. Given they are a textDisplay on the background.
	 * Values for x and y must be between 0 and 1
	 *
	 * @rx: relative x
	 * @ry: relative y
	 * @return the location to set as the basis of the button.
	 */
	fun locationAtRelativeCoordinates(rx: Float, ry: Float, isTextDisplay: Boolean): Location {
		val isTextDisplay = isTextDisplay
		return displayLocation.clone().add(
			Vector(
				rx * sizeX * -dz,
				(sizeY * ry) + sizeY - 0.05 * sizeY * (if (isTextDisplay) 1.0 else 0.0),
				-rx * sizeX * -dx
			)
		)
	}

	companion object {
		private fun Transformation.clone(): Transformation =
			Transformation(this.translation, this.leftRotation, this.scale, this.rightRotation)
	}
}
