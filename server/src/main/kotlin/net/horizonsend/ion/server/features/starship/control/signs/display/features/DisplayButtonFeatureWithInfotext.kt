package net.horizonsend.ion.server.features.starship.control.signs.display.features

import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.starship.control.signs.display.toVector3f
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3d
import org.joml.Vector3f

open class DisplayButtonFeatureWithInfotext(
	identifier: String,
	map: net.horizonsend.ion.server.features.starship.control.signs.display.Display,
	rx: Double,
	ry: Double,
	sizeX: Double,
	sizeY: Double,
	itemStack: ItemStack? = null,
	component: Component? = null,
	offset: Double,
	relativeFeature: DisplayFeature? = null,
	var info: Component,
	val color: Color,
	function: (it: net.horizonsend.ion.server.features.starship.control.signs.display.Display) -> Unit
) : DisplayButtonFeature(identifier, map, rx, ry, sizeX, sizeY, itemStack, component, offset, relativeFeature, function) {

	override fun init() {
		initInfoDisplay()
		super.init()
	}

	var infoDisplay: TextDisplay? = null

	open fun initInfoDisplay() {
		val textDisplay = display.ship.world.spawnEntity(
			location().add(display.dir.clone().multiply(display.shiftPerLayer * offset * .1))
				.add(Vector(0.0, display.dir.y, 0.0).multiply(display.shiftPerLayer * offset * .1)),
			EntityType.TEXT_DISPLAY
		) as TextDisplay
		textDisplay.text(info)
		textDisplay.backgroundColor = color
		textDisplay.transformation = Transformation(
			Vector3f(),
			ClientDisplayEntities.rotateToFaceVector(display.dir.toVector3f()),
			Vector3d(
				(sizeX) * .5 * display.sizeX * (relativeFeature?.sizeX ?: 1.0),
				(sizeY) * .5 * display.sizeY * (relativeFeature?.sizeY ?: 1.0),
				0.01
			).toVector3f(),
			Quaternionf()
		)
		textDisplay.teleportDuration = 0
		textDisplay.interpolationDelay = 0
		textDisplay.isPersistent = false
		textDisplay.brightness = org.bukkit.entity.Display.Brightness(15, 0)
		entities.add(textDisplay)
		infoDisplay = textDisplay
	}

	override fun tick() {
		if (!display.ship.isMoving) {
			this.entities.forEach { it.teleport(location()) }
		}

		this.infoDisplay?.transformation = Transformation(
			Vector3f(0f, (-sizeY / 64.0).toFloat(), 0f).add(
				display.dir.clone().multiply(display.shiftPerLayer * offset).toVector3f()
			),
			ClientDisplayEntities.rotateToFaceVector(display.dir.toVector3f()),
			Vector3d(
				(sizeX) * .75 * display.sizeX * (relativeFeature?.sizeX ?: 1.0),
				(sizeY) * .75 * display.sizeY * (relativeFeature?.sizeY ?: 1.0),
				0.0001
			).toVector3f(),
			Quaternionf()
		)

		interaction.interactionWidth = (sizeX * display.sizeX * (relativeFeature?.sizeX ?: 1.0)).toFloat()
		interaction.interactionHeight = (sizeY * display.sizeY * (relativeFeature?.sizeY ?: 1.0)).toFloat()

		if (featureDisplay is TextDisplay) {
			this.featureDisplay?.transformation = Transformation(
				Vector3f(),
				ClientDisplayEntities.rotateToFaceVector(display.dir.toVector3f()),
				Vector3d(
					(5.0 * sizeX) * display.sizeX,
					(5.0 * sizeY) * display.sizeY,
					0.0001
				).toVector3f(),
				Quaternionf()

			)
		} else if (featureDisplay is ItemDisplay) {
			featureDisplay?.transformation = Transformation(
				Vector3f(),
				ClientDisplayEntities.rotateToFaceVector(display.dir.toVector3f().mul(-1f)),
				Vector3d(
					sizeX * display.sizeX * (relativeFeature?.sizeX ?: 1.0),
					sizeY * display.sizeY * (relativeFeature?.sizeY ?: 1.0),
					0.0001
				).toVector3f(),
				Quaternionf()
			)
		}
	}
}
