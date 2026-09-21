package net.horizonsend.ion.server.features.starship.control.signs.display.features.map

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.sidebar.tasks.ContactsSidebar
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.control.signs.display.Display
import net.horizonsend.ion.server.features.starship.control.signs.display.DisplayMap
import net.horizonsend.ion.server.features.starship.control.signs.display.MapTextIcon
import net.horizonsend.ion.server.features.starship.control.signs.display.features.DisplayButtonFeatureWithInfotext
import net.horizonsend.ion.server.features.starship.control.signs.display.features.DisplayFeature
import net.horizonsend.ion.server.features.starship.control.signs.display.getSidebarKeyToUse
import net.horizonsend.ion.server.features.starship.control.signs.display.shipScale
import net.horizonsend.ion.server.features.starship.fleet.Fleets
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Color
import org.bukkit.entity.TextDisplay
import kotlin.math.absoluteValue

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
class ShipDisplayFeature(
	identifier: String,
	map: DisplayMap,
	rx: Double,
	ry: Double,
	sizeX: Double,
	sizeY: Double,
	component: Component? = null,
	offset: Double,
	relativeFeature: DisplayFeature? = null,
	info: Component,
	color: Color,
	val ship: Starship,
	function: (it: Display) -> Unit
	) : DisplayButtonFeatureWithInfotext(identifier, map, rx, ry, sizeX, sizeY, null,component, offset, relativeFeature, info, color, function) {

	override fun init() {
		this.info = generateDistanceText()
		super.init()
	}

	override fun tick(){
		if (display !is DisplayMap) return
		//check if the ship is out of range
		val offset = (display.ship.centerOfMass.minus(ship.centerOfMass).toVector().setY(0).multiply(1.0/display.maxDistance))

		if(offset.x.absoluteValue > .5  || offset.z.absoluteValue > .5){
			display.dynamicFeatures.remove(this)
			display.shipsTracked.remove(ship)
			this.despawn()
		}

		this.rx = .5-offset.x
		this.ry = .5+offset.z

		val shipScale = shipScale(display)

		this.sizeX = shipScale
		this.sizeY = shipScale

		this.infoDisplay?.text(generateDistanceText())

		val icon = ship.type.icon

		var color = ship.getRelation(ship).color
		if (ship.playerPilot != null && ship.playerPilot != null) {
			if (Fleets.findByMember(ship.playerPilot!!)?.contains(ship.playerPilot!!) == true) {
				color = NamedTextColor.BLUE
			}
		}

		(this.featureDisplay as? TextDisplay)?.text(
            ofChildren(
                MapTextIcon.ONE_PIXEL.component(),
                Component.text(icon, color).font(getSidebarKeyToUse(ship)),
            )
        )

		super.tick()
	}

	fun generateDistanceText() : Component {
		val distance = ship.centerOfMass.distance(display.ship.centerOfMass).toInt()
		return ofChildren(
            Component.text(this.ship.identifier, NamedTextColor.WHITE),
            Component.text(" ${distance}m", ContactsSidebar.distanceColor(distance)),
            Component.text("\nX: ${ship.centerOfMass.x}, Y: ${ship.centerOfMass.z}", NamedTextColor.WHITE),
        )
	}
}
