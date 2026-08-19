package net.horizonsend.ion.server.features.starship.control.signs.map.features

import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.common.utils.text.asShadowColor
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.sidebar.tasks.ContactsSidebar
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.control.signs.map.DisplayMap
import net.horizonsend.ion.server.features.starship.control.signs.map.DisplayMap.Companion.toVector3f
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Color
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3d
import org.joml.Vector3f

/**
 * Ship map feature is the encapsulation of the Map feature of a starship on the map + the textDisplay with the info, and the
 * good relative maths needed for it to work.
 *
 * @constructor
 * @property ship
 * @property color
 * @param identifier
 * @param map
 * @param rx
 * @param ry
 * @param sizeX the size x
 * @param sizeY the size y
 * @param component
 * @param offset
 * @param relativeFeature the relative feature
 */
class ShipMapFeature(
	identifier: String,
	map: DisplayMap,
	rx: Double,
	ry: Double,
	sizeX: Double,
	sizeY: Double,
	component: Component? = null,
	offset: Double,
	relativeFeature: MapFeature? = null,
	val ship: Starship,
	val color: NamedTextColor,
) : MapFeature(identifier, map, rx, ry, sizeX, sizeY, null, component, offset, relativeFeature) {

	override fun init() {
		super.init()
		initShipInfoDisplay()
	}

	private var shipInfoDisplay: TextDisplay? = null

	private fun initShipInfoDisplay() {
		println("z")
		val text = generateDistanceText()

		val textDisplay = this.location().world.spawnEntity(location(), EntityType.TEXT_DISPLAY) as TextDisplay
		textDisplay.text(text)
		textDisplay.backgroundColor = Color.fromARGB(color.asShadowColor(200).value())
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
		shipInfoDisplay = textDisplay
	}


	override fun tick(){
		//check if the ship is out of range
		val offset = (map.ship.centerOfMass.minus(ship.centerOfMass).toVector().setY(0).multiply(1.0/map.maxDistance))
		if(offset.length() > 1){
			map.mapStateFeatures.remove(this)
			map.shipsTracked.remove(ship)
			this.despawn()
		}

		this.rx = .5+offset.x
		this.ry = .5+offset.z
		println(location())
		println("${offset.x}, ${offset.y}, ${offset.z}")
		if(!ship.isMoving) {
			this.entities.forEach { it.teleport(location()) }
		}

		val shipScale = map.shipScale()

		this.sizeX = shipScale
		this.sizeY = shipScale

		this.shipInfoDisplay?.transformation = Transformation(
			Vector3f(0f,0f,0f),
			ClientDisplayEntities.rotateToFaceVector(map.dir.toVector3f()),
			Vector3d(
				(sizeX)*.5 * map.sizeX * (relativeFeature?.sizeX ?: 1.0),
				(sizeY)*.5 * map.sizeY * (relativeFeature?.sizeY ?: 1.0),
				0.001
			).toVector3f(),
			Quaternionf()
		)
		this.shipInfoDisplay?.text(generateDistanceText())

		if (display is TextDisplay) {
			println("y")
			this.display?.transformation = Transformation(
				Vector3f(),
				ClientDisplayEntities.rotateToFaceVector(map.dir.toVector3f().mul(1f)),
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

	fun generateDistanceText() : Component{
		val distance = ship.centerOfMass.distance(map.ship.centerOfMass).toInt()
		return ofChildren(
			Component.text(this.ship.identifier, NamedTextColor.WHITE),
			Component.text(" ${distance}m", ContactsSidebar.distanceColor(distance))
		)
	}
}
