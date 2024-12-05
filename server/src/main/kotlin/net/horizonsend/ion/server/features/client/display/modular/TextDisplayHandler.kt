package net.horizonsend.ion.server.features.client.display.modular

import net.horizonsend.ion.server.features.client.display.modular.display.Display
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.starship.movement.TranslateMovement
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import org.bukkit.Axis
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.BlockFace

class TextDisplayHandler(
	val holder: DisplayHandlerHolder,
	var world: World,
	var anchorX: Double,
	var anchorY: Double,
	var anchorZ: Double,
	var offsetRight: Double,
	var offsetUp: Double,
	var offsetForward: Double,
	var facing: BlockFace,
	vararg display: Display
) {
	val displays = listOf(*display)

	fun update() {
		if (!holder.isAlive) {
			remove()
			return
		}

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
			it.initialize(this)
			it.register()
		}

		DisplayHandlers.registerHandler(this)

		return this
	}

	fun getLocation(): Location {
		val rightFace = if (facing.axis == Axis.Y) BlockFace.NORTH else facing.rightFace

		val offsetX = (rightFace.modX * offsetRight) + (facing.modX * offsetForward)
		val offsetY = offsetUp
		val offsetZ = (rightFace.modZ * offsetRight) + (facing.modZ * offsetForward)

		return Location(world, anchorX + offsetX, anchorY + offsetY, anchorZ + offsetZ)
	}

	fun displace(movement: StarshipMovement) {
		val oldFace = facing
		facing = movement.displaceFace(oldFace)

		if (movement is TranslateMovement) {
			anchorX += movement.dx
			anchorY += movement.dy
			anchorZ += movement.dz
		}

		for (display in displays) {
			display.resetPosition(this)
		}

		update()
	}
}
