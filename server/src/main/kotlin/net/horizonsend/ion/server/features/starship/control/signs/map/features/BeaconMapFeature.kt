package net.horizonsend.ion.server.features.starship.control.signs.map.features

import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.starship.control.signs.map.DisplayMap
import net.horizonsend.ion.server.features.starship.control.signs.map.DisplayMap.Companion.toVector3f
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3d
import org.joml.Vector3f

class BeaconMapFeature(
	identifier: String,
	map: DisplayMap,
	rx: Double,
	ry: Double,
	sizeX: Double,
	sizeY: Double,
	component: Component? = null,
	itemStack: ItemStack? = null,
	offset: Double,
	relativeFeature: MapFeature? = null,
	info: Component,
	color: NamedTextColor,
	val beacon: ServerConfiguration.HyperspaceBeacon,
) : MapFeatureWithInfotext(identifier, map, rx, ry, sizeX, sizeY, itemStack, component, offset, relativeFeature, info, color) {
	override fun tick() {
		//check if the beacon is out of range
		val offset = (map.ship.centerOfMass.toVector().add(beacon.spaceLocation.toVector().multiply(-1))).setY(0).multiply(1.0 / map.maxDistance)
		if(offset.length() > .5){
			map.mapStateFeatures.remove(this)
			map.beaconTracked.remove(beacon)
			this.despawn()
		}
		val beaconScale = 100.0/map.maxDistance

		this.rx = -.5+offset.x
		this.ry = .5+offset.z
		if(!map.ship.isMoving) {
			this.entities.forEach { it.teleport(location()) }
		}

		this.sizeX = beaconScale
		this.sizeY = beaconScale

		super.tick()
	}
}
