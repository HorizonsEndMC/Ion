package net.horizonsend.ion.server.features.starship.control.signs.map.features

import net.horizonsend.ion.common.database.schema.misc.Bookmark
import net.horizonsend.ion.server.features.starship.control.signs.map.DisplayMap
import net.horizonsend.ion.server.features.starship.control.signs.map.MapState
import net.horizonsend.ion.server.features.starship.control.signs.map.toVector
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class BookmarkMapFeature(
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
	color: Color,
	val bookmark: Bookmark,
	function: (it: DisplayMap) -> Unit,
	) : MapFeatureWithInfotext(identifier, map, rx, ry, sizeX, sizeY, itemStack, component, offset, relativeFeature, info, color, function) {
	override fun tick() {
		val source = map.systemForSystemMap?.worldBorder?.center?.toVector() ?: Vector()
		//check if the routeable thing is out of range
		val offset = when(map.state){
			MapState.LOCAL_MAP ->(map.ship.centerOfMass.toVector().add(bookmark.toVector().multiply(-1))).setY(0).multiply(1.0 / map.maxDistance)
			MapState.SYSTEMS_MAP-> ((source.add(bookmark.toVector().multiply(-1))).setY(0)
				.multiply(1.0 / (map.systemForSystemMap?.worldBorder?.size ?: 10000.0)))
			else -> Vector()
		}
		if(offset.length() > .5){
			map.mapStateFeatures.remove(this)
			map.bookmarkTracked.remove(bookmark)
			this.despawn()
		}

		val beaconScale = .06

		this.rx = .5-offset.x
		this.ry = .5+offset.z
		if(!map.ship.isMoving) {
			this.entities.forEach { it.teleport(location()) }
		}

		this.sizeX = beaconScale
		this.sizeY = beaconScale

		super.tick()
	}
}
