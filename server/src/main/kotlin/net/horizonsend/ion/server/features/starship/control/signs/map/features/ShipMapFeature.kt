package net.horizonsend.ion.server.features.starship.control.signs.map.features

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.sidebar.tasks.ContactsSidebar
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.control.signs.map.DisplayMap
import net.horizonsend.ion.server.features.starship.control.signs.map.shipScale
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Color

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
	info: Component,
	color: Color,
	val ship: Starship,
	function: (it: DisplayMap) -> Unit
	) : MapFeatureWithInfotext(identifier, map, rx, ry, sizeX, sizeY, null,component, offset, relativeFeature, info, color, function) {

	override fun init() {
		this.info = generateDistanceText()
		super.init()
	}

	override fun tick(){
		//check if the ship is out of range
		val offset = (map.ship.centerOfMass.minus(ship.centerOfMass).toVector().setY(0).multiply(1.0/map.maxDistance))
		if(offset.length() > .5){
			map.mapStateFeatures.remove(this)
			map.shipsTracked.remove(ship)
			this.despawn()
		}

		this.rx = .5+offset.x
		this.ry = .5+offset.z

		val shipScale = shipScale(map)

		this.sizeX = shipScale
		this.sizeY = shipScale

		this.infoDisplay?.text(generateDistanceText())

		super.tick()
	}

	fun generateDistanceText() : Component{
		val distance = ship.centerOfMass.distance(map.ship.centerOfMass).toInt()
		return ofChildren(
			Component.text(this.ship.identifier, NamedTextColor.WHITE),
			Component.text(" ${distance}m", ContactsSidebar.distanceColor(distance)),
			Component.text("\nX: ${ship.centerOfMass.x}, Y: ${ship.centerOfMass.z}", NamedTextColor.WHITE),
		)
	}
}
