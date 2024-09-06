package net.horizonsend.ion.server.features.client.display.modular

import net.horizonsend.ion.server.features.client.display.modular.display.Display
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class TextDisplayHandler(
	var world: World,
	var anchorX: Double,
	var anchorY: Double,
	var anchorZ: Double,
	var offset: Vector,
	var facing: BlockFace,
	vararg display: Display
) {
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

	fun getLocation() = Location(world, anchorX, anchorY, anchorZ).add(offset)

	fun displace(movement: StarshipMovement) {
		offset = movement.displaceVector(offset)
		facing = movement.displaceFace(facing)

		for (display in displays) {
			display.resetPosition(this)
		}

		update()
	}
}
