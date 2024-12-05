package net.horizonsend.ion.server.features.client.display.modular

import net.horizonsend.ion.server.features.client.display.modular.display.Display
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import org.bukkit.Axis
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.BlockFace

class TextDisplayHandler(
	val holder: DisplayHandlerHolder,
	var world: World,

	var blockX: Int,
	var blockY: Int,
	var blockZ: Int,

	val offsetRight: Double,
	val offsetUp: Double,
	val offsetForward: Double,

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
		val forwardFace = facing
		val rightFace = if (forwardFace.axis == Axis.Y) BlockFace.NORTH else forwardFace.rightFace

		// Start from a center location, should be the same for every block
		val centerLocation = Location(world, blockX + 0.5, blockY + 0.5, blockZ + 0.5)

		// Offset from the center location
		centerLocation.add(
			rightFace.modX * offsetRight + forwardFace.modX * offsetForward,
			offsetUp,
			rightFace.modZ * offsetRight + forwardFace.modZ * offsetForward
		)

		return centerLocation
	}

	fun displace(movement: StarshipMovement) {
		val oldFace = facing
		facing = movement.displaceFace(oldFace)

		val newX = movement.displaceX(blockX, blockZ)
		val newY = movement.displaceY(blockY)
		val newZ = movement.displaceZ(blockZ, blockX)

		blockX = newX
		blockY = newY
		blockZ = newZ

		for (display in displays) {
			display.resetPosition(this)
		}

		update()
	}
}
