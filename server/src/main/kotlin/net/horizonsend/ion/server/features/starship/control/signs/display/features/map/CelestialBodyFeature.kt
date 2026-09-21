package net.horizonsend.ion.server.features.starship.control.signs.display.features.map

import net.horizonsend.ion.server.features.space.body.CachedStar
import net.horizonsend.ion.server.features.space.body.CelestialBody
import net.horizonsend.ion.server.features.space.body.planet.CachedPlanet
import net.horizonsend.ion.server.features.starship.control.signs.display.Display
import net.horizonsend.ion.server.features.starship.control.signs.display.DisplayMap
import net.horizonsend.ion.server.features.starship.control.signs.display.MapState
import net.horizonsend.ion.server.features.starship.control.signs.display.celestialBodyLocalMapScale
import net.horizonsend.ion.server.features.starship.control.signs.display.features.DisplayButtonFeatureWithInfotext
import net.horizonsend.ion.server.features.starship.control.signs.display.features.DisplayFeature
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import kotlin.math.absoluteValue

class CelestialBodyFeature(
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
	val body: CelestialBody,
	function: (it: Display) -> Unit
) : DisplayButtonFeatureWithInfotext(identifier, map, rx, ry, sizeX, sizeY, itemStack, component, offset, relativeFeature, info, color, function) {
	override fun init() {
		super.init()
		if (body is CachedStar) interaction.remove()
	}
	override fun tick() {
		if (display !is DisplayMap) return
		val source = display.systemForSystemMap?.worldBorder?.center?.toVector() ?: Vector()

		//check if the body is out of range
		val offset = when(display.state){
			MapState.LOCAL_MAP ->(display.ship.centerOfMass.toVector().add(body.location.toVector().multiply(-1))).setY(0).multiply(1.0 / display.maxDistance)
			MapState.SYSTEMS_MAP-> ((source.add(body.location.toVector().multiply(-1))).setY(0).multiply(1.0 / (display.systemForSystemMap?.worldBorder?.size ?: 10000.0)))
			else -> Vector()
		}

		val bodyScale = when(this.display.state){
			MapState.LOCAL_MAP -> celestialBodyLocalMapScale(body, display)
			else -> when(this.body){
				is CachedStar -> 0.12
				is CachedPlanet -> 0.08
				else -> 0.04
			}
		}

		if((offset.x.absoluteValue + bodyScale/4.0) > .5  || (offset.z.absoluteValue + bodyScale/4) > .5){
			display.dynamicFeatures.remove(this)
			display.celestialBodiesTracked.remove(body)
			this.despawn()
		}

		this.rx = .5-offset.x
		this.ry = .5+offset.z

		this.sizeX = bodyScale
		this.sizeY = bodyScale

		super.tick()
	}
}
