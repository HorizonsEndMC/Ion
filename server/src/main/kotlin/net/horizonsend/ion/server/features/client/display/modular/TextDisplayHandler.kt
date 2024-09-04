package net.horizonsend.ion.server.features.client.display.modular

import net.horizonsend.ion.server.features.client.display.modular.display.Display
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

		DisplayHandlers.deRegisterHandler(this)
	}

	fun register(): TextDisplayHandler {
		displays.forEach {
			it.setParent(this)
			it.register()
		}

		DisplayHandlers.registerHandler(this)

		return this
	}
}
