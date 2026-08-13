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
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f

class DisplayMap(val ship: Starship, val location: Location, val dir: Vector, val sizeX: Float, val sizeY: Float) {
	val shiftPerLayer = 0.0025
	val buttonLayerOffset = shiftPerLayer * 4

	val oppositeDir = dir.multiply(-1)
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
	val singleCharacterTextDisplayTransformation = Transformation(
		Vector3f(),
		ClientDisplayEntities.rotateToFaceVector(dir.toVector3f()),
		Vector3f(40f * sizeX, 40 * sizeY, 0f),
		Quaternionf()
	)

	val background = ship.world.spawnEntity(
		locationAtRelativeCoordinates(16.0 / 32, 16.0 / 32, false).clone(), EntityType.ITEM_DISPLAY
	) as ItemDisplay
	val border = ship.world.spawnEntity(
		displayLocation.clone().add(
			dir.clone().multiply(shiftPerLayer * 1)
		),
		EntityType.TEXT_DISPLAY
	) as TextDisplay

	var map: ItemDisplay? = null

	//Map features
	val mapFeatures: MutableList<MapFeature> = mutableListOf()
	val mapButtons = mutableListOf<MapButton>()

	init {
		initializeBackgroundAndBorder()
		initializeButtons()
		//placeMap()
		//DO LAST

		//Add entities to ship
		ship.entityPassengers.addAll(listOf(border, background))
		mapButtons.forEach { ship.entityPassengers.add(it.itemDisplay); ship.entityPassengers.add(it.interaction) }
	}

	private fun initializeButtons() {
		//GALACTIC MAP
		mapButtons.add(
			MapButton(
				"GALACTIC_MAP", this, 30.5 / 32.0, 28.5 / 32.0, 3f, 3f,
				ItemStack(Material.PAPER).updateData(
					DataComponentTypes.ITEM_MODEL,
					NamespacedKeys.packKey("achievement_icon/hyperspace")
				)
			)
		)

		//PLUS BUTTON
		mapButtons.add(
			MapButton(
				"PLUS_BUTTON",
				this,
				30.5 / 32.0,
				25.5 / 32.0,
				3f,
				3f,
				ItemStack(Material.PAPER).applyGuiModel(GuiItem.PLUS)
			)
		)

		//MINUS BUTTON

		mapButtons.add(
			MapButton(
				"MINUS_BUTTON",
				this,
				30.5 / 32.0,
				22.5 / 32.0,
				3f,
				3f,
				ItemStack(Material.PAPER).applyGuiModel(GuiItem.MINUS)
			)
		)
	}

	private fun initializeBackgroundAndBorder() {
		border.backgroundColor = Color.fromARGB(0, 0, 0, 0)

		background.brightness = Display.Brightness(15, 0)
		border.brightness = Display.Brightness(15, 0)

		border.transformation = singleCharacterTextDisplayTransformation.clone()
		border.displayHeight
		background.transformation = Transformation(
			Vector3f(), ClientDisplayEntities.rotateToFaceVector(dir.clone().multiply(-1).toVector3f()),
			Vector3f(sizeX, sizeY, 0.001f), Quaternionf()
		)

		background.teleportDuration = 0
		border.teleportDuration = 0


		background.setItemStack(
			ItemStack(Material.PAPER).updateData(
				DataComponentTypes.ITEM_MODEL,
				NamespacedKeys.packKey("map/black")
			)
		)
		border.text(Component.text('\uEBF1').font(SPECIAL_FONT_KEY))
	}

	private fun placeMap() {


		map = ship.world.spawnEntity(
			locationAtRelativeCoordinates(
				15.0 / 32f,
				15.0 / 32.0,
				false
			).add(dir.clone().multiply(shiftPerLayer * 8)), EntityType.ITEM_DISPLAY
		) as ItemDisplay
		map!!.transformation = Transformation(
			Vector3f(), ClientDisplayEntities.rotateToFaceVector(dir.clone().multiply(-1).toVector3f()),
			Vector3f((sizeX * 28.0f) / 32.0f, (sizeY * 28.0f) / 32.0f, 0.001f), Quaternionf()
		)
		map!!.brightness = Display.Brightness(15, 0)
		map!!.teleportDuration = 0
		map!!.setItemStack(
			ItemStack(Material.PAPER).updateData(
				DataComponentTypes.ITEM_MODEL,
				NamespacedKeys.packKey("map/systems")
			)
		)
		ship.entityPassengers.add(map!!)
	}

	/**
	 * This function generates a location to teleport entities. Given they are a textDisplay on the background.
	 * Values for x and y must be between 0 and 1
	 *
	 * @rx: relative x
	 * @ry: relative y
	 * @return the location to set as the basis of the button.
	 */
	fun locationAtRelativeCoordinates(rx: Double, ry: Double, isTextDisplay: Boolean): Location {
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
