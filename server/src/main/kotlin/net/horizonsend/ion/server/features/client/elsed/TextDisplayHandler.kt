package net.horizonsend.ion.server.features.client.elsed

import net.horizonsend.ion.server.features.client.elsed.display.Display
import org.bukkit.World

class TextDisplayHandler(val world: World, val x: Double, val y: Double, val z: Double, vararg display: Display) {
	private val displays = listOf(*display)

	fun update() {
		displays.forEach {
			it.update()
		}
	}

	fun remove() {
		displays.forEach {
			it.remove()
			it.deRegister()
		}
	}

	fun register(): TextDisplayHandler {
		displays.forEach {
			it.setParent(this)
			it.register()
		}

		// TODO schedule updates

		return this
	}
}
