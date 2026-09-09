package net.horizonsend.ion.server.features.starship.control.signs.map.features

import net.horizonsend.ion.server.features.space.body.CachedStar
import net.horizonsend.ion.server.features.space.body.CelestialBody
import net.horizonsend.ion.server.features.space.body.planet.CachedPlanet
import net.horizonsend.ion.server.features.starship.control.signs.map.DisplayMap
import net.horizonsend.ion.server.features.starship.control.signs.map.MapState
import net.horizonsend.ion.server.features.starship.control.signs.map.celestialBodyLocalMapScale
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class CelestialBodyFeature(
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
	val body: CelestialBody,
	function: (it: DisplayMap) -> Unit
) : MapFeatureWithInfotext(identifier, map, rx, ry, sizeX, sizeY, itemStack, component, offset, relativeFeature, info, color, function) {
	override fun init() {
		super.init()
		if (body is CachedStar) interaction.remove()
	}
	override fun tick() {
		val source = map.systemForSystemMap?.worldBorder?.center?.toVector() ?: Vector()

		//check if the body is out of range
		val offset = when(map.state){
			MapState.LOCAL_MAP ->(map.ship.centerOfMass.toVector().add(body.location.toVector().multiply(-1))).setY(0).multiply(1.0 / map.maxDistance)
			MapState.SYSTEMS_MAP-> ((source.add(body.location.toVector().multiply(-1))).setY(0).multiply(1.0 / (map.systemForSystemMap?.worldBorder?.size ?: 10000.0)))
			else -> Vector()
		}
		if(offset.length() > .5){
			map.mapStateFeatures.remove(this)
			map.celestialBodiesTracked.remove(body)
			this.despawn()
		}
		val bodyScale = when(this.map.state){
			MapState.LOCAL_MAP -> celestialBodyLocalMapScale(body, map)
			else -> when(this.body){
				is CachedStar -> 0.12
				is CachedPlanet -> 0.08
				else -> 0.04
			}
		}

		this.rx = .5-offset.x
		this.ry = .5+offset.z

		this.sizeX = bodyScale
		this.sizeY = bodyScale

		super.tick()
	}
}
