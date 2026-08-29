package net.horizonsend.ion.server.features.starship.control.signs.map.features

import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.starship.control.signs.map.DisplayMap
import net.horizonsend.ion.server.features.starship.control.signs.map.DisplayMap.Companion.toVector3f
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3d
import org.joml.Vector3f

open class MapFeatureWithInfotext(
	identifier: String,
	map: DisplayMap,
	rx: Double,
	ry: Double,
	sizeX: Double,
	sizeY: Double,
	itemStack: ItemStack? = null,
	component: Component? = null,
	offset: Double,
	relativeFeature: MapFeature? = null,
	var info: Component,
	val color: Color,
	function: (it: DisplayMap) -> Unit
) : MapButtonDisplay(identifier, map, rx, ry, sizeX, sizeY, itemStack, component, offset, relativeFeature, function) {

	override fun init() {
		initInfoDisplay()
		super.init()
	}

	var infoDisplay: TextDisplay? = null

	open fun initInfoDisplay() {
		val textDisplay = this.location().world.spawnEntity(location().add(map.dir.clone().multiply(map.shiftPerLayer*offset+.1)), EntityType.TEXT_DISPLAY) as TextDisplay
		textDisplay.text(info)
		textDisplay.backgroundColor = color
		textDisplay.transformation = Transformation(
			Vector3f(0f,0f,0f),
			ClientDisplayEntities.rotateToFaceVector(map.dir.toVector3f()),
			Vector3d(
				(sizeX)*.5 * map.sizeX * (relativeFeature?.sizeX ?: 1.0),
				(sizeY)*.5 * map.sizeY * (relativeFeature?.sizeY ?: 1.0),
				0.001
			).toVector3f(),
			Quaternionf()
		)
		textDisplay.teleportDuration = 0
		textDisplay.interpolationDelay = 0
		textDisplay.isPersistent = false
		textDisplay.brightness = Display.Brightness(15, 0)
		entities.add(textDisplay)
		infoDisplay = textDisplay
	}

	override fun tick() {
		if(!map.ship.isMoving) {
			this.entities.forEach { it.teleport(location()) }
		}

		this.infoDisplay?.transformation = Transformation(
			Vector3f(0f, (-sizeY/64.0).toFloat(),0f),
			ClientDisplayEntities.rotateToFaceVector(map.dir.toVector3f()),
			Vector3d(
				(sizeX)*.75 * map.sizeX * (relativeFeature?.sizeX ?: 1.0),
				(sizeY)*.75 * map.sizeY * (relativeFeature?.sizeY ?: 1.0),
				0.001
			).toVector3f(),
			Quaternionf()
		)

		interaction.interactionWidth = (sizeX * map.sizeX * (relativeFeature?.sizeX ?: 1.0)).toFloat()
		interaction.interactionHeight = (sizeY * map.sizeY * (relativeFeature?.sizeY ?: 1.0)).toFloat()

		if (display is TextDisplay) {
			this.display?.transformation = Transformation(
				Vector3f(),
				ClientDisplayEntities.rotateToFaceVector(map.dir.toVector3f()),
				Vector3d(
					(5.0 * sizeX) * map.sizeX,
					(5.0 * sizeY) * map.sizeY,
					0.001
				).toVector3f(),
				Quaternionf()

			)
		}
		else if(display is ItemDisplay) {
			display?.transformation = Transformation(
				Vector3f(),
				ClientDisplayEntities.rotateToFaceVector(map.dir.toVector3f().mul(-1f)),
				Vector3d(
					sizeX * map.sizeX * (relativeFeature?.sizeX ?: 1.0),
					sizeY * map.sizeY * (relativeFeature?.sizeY ?: 1.0),
					0.001
				).toVector3f(),
				Quaternionf()
			)
		}
	}
}
