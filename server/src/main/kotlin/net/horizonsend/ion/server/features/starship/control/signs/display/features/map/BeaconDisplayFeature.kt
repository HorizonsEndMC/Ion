package net.horizonsend.ion.server.features.starship.control.signs.display.features.map

import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.starship.control.signs.display.Display
import net.horizonsend.ion.server.features.starship.control.signs.display.DisplayMap
import net.horizonsend.ion.server.features.starship.control.signs.display.MapState
import net.horizonsend.ion.server.features.starship.control.signs.display.features.DisplayButtonFeatureWithInfotext
import net.horizonsend.ion.server.features.starship.control.signs.display.features.DisplayFeature
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import kotlin.collections.forEach
import kotlin.math.absoluteValue

class BeaconDisplayFeature(
	identifier: String,
	map: Display,
	rx: Double,
	ry: Double,
	sizeX: Double,
	sizeY: Double,
	component: Component? = null,
	itemStack: ItemStack? = null,
	offset: Double,
	relativeFeature: DisplayFeature? = null,
	info: Component,
	color: Color,
	val beacon: ServerConfiguration.HyperspaceBeacon,
	function: (it: Display) -> Unit,
	) : DisplayButtonFeatureWithInfotext(identifier, map, rx, ry, sizeX, sizeY, itemStack, component, offset, relativeFeature, info, color, function) {
	override fun tick() {
		if (display !is DisplayMap) return
		val source = display.systemForSystemMap?.worldBorder?.center?.toVector() ?: Vector()
		//check if the routeable thing is out of range
		val offset = when(display.state){
			MapState.LOCAL_MAP ->(display.ship.centerOfMass.toVector().add(beacon.spaceLocation.toVector().multiply(-1))).setY(0).multiply(1.0 / display.maxDistance)
			MapState.SYSTEMS_MAP-> ((source.add(beacon.spaceLocation.toVector().multiply(-1))).setY(0)
				.multiply(1.0 / (display.systemForSystemMap?.worldBorder?.size ?: 10000.0)))
			else -> Vector()
		}
		if(offset.x.absoluteValue > .5  || offset.z.absoluteValue > .5){
			display.dynamicFeatures.remove(this)
			display.beaconsTracked.remove(beacon)
			this.despawn()
		}

		val beaconScale = .08

		this.rx = .5-offset.x
		this.ry = .5+offset.z
		if(!display.ship.isMoving) {
			this.entities.forEach { it.teleport(location()) }
		}

		this.sizeX = beaconScale
		this.sizeY = beaconScale

		super.tick()
	}
}
