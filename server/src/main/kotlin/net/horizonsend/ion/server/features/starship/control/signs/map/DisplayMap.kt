package net.horizonsend.ion.server.features.starship.control.signs.map

import net.horizonsend.ion.common.utils.text.SPECIAL_FONT_KEY
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.block.Sign
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.Interaction
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f

class DisplayMap(val ship: Starship, val sign: Sign) {
	val shiftPerLayer = 0.0025
	val size = sign.lines[1].split(" ")
	val sizeX = size[0].toFloat()
	val sizeY = size[1].toFloat()
	val signDirection = sign.getFacing().direction

	val dir = sign.getFacing().oppositeFace.direction
	val dx = dir.x
	val dz = dir.z

	val location = sign.location.add(//the following vector maths, centers the displayEntity onto the center of the block
		Vector(
			0.5 - 0.25 * dx + 0.5 * sizeX * dz,
			-sizeY.toDouble(),
			0.5 - 0.25 * dz - 0.5 * sizeX * dx
		)
	)

	val background = ship.world.spawnEntity(
		location.clone(), EntityType.TEXT_DISPLAY
	) as TextDisplay
	val border = ship.world.spawnEntity(
		location.clone().add(
			sign.getFacing().direction.clone().multiply(shiftPerLayer * 9)
		),
		EntityType.TEXT_DISPLAY
	) as TextDisplay

	//Map features
	val mapFeatures: MutableList<MapFeature> = mutableListOf()
	val mapButtons = mutableListOf<MapButton>()

	init {
		initializeBackgroundAndBorder()
		initializeInteractionEntityOfTheMap()
		//DO LAST

		//Add entities to ship
		ship.entityPassengers.addAll(listOf(border, background))
		mapFeatures.forEach { ship.entityPassengers.add(it.itemDisplay) }
	}

	private fun initializeInteractionEntityOfTheMap() {
		//size of the interaction should be the size of the background shown through the border. In other words 39*size
		val loc = sign.location.add(Vector(0.0, sizeY/2.0, 0.0))
		val interaction = sign.world.spawnEntity(location, EntityType.INTERACTION) as Interaction
		interaction.interactionWidth = sizeX
		interaction.interactionHeight =sizeY
		interaction.interactionWidth
		interaction.isResponsive = true

		val button = MapButton(background, interaction)
		mapButtons.add(button)
	}

	private fun initializeBackgroundAndBorder() {
		background.backgroundColor = Color.fromARGB(0, 0, 0, 0)
		border.backgroundColor = Color.fromARGB(0, 0, 0, 0)
		background.brightness = Display.Brightness(15,0)
		border.brightness = Display.Brightness(15,0)

		val singleCharacterTextDisplayTransformation = Transformation(
			Vector3f(),
			ClientDisplayEntities.rotateToFaceVector(signDirection.toVector3f()),
			Vector3f(40f * sizeX, 40 * sizeY, 0f),
			Quaternionf()
		)
		border.transformation = singleCharacterTextDisplayTransformation.clone()
		background.transformation = singleCharacterTextDisplayTransformation.clone()

		background.teleportDuration = 0
		border.teleportDuration = 0

		background.text(Component.text('\uEBF2').font(SPECIAL_FONT_KEY))
		border.text(Component.text('\uEBF1').font(SPECIAL_FONT_KEY))
	}

	fun tick() {

	}

	fun kill(){}


	companion object {
		private fun Transformation.clone(): Transformation =
			Transformation(this.translation, this.leftRotation, this.scale, this.rightRotation)
	}
}
