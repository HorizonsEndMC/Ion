package net.horizonsend.ion.server.features.client.display.modular

import net.horizonsend.ion.server.features.client.display.modular.display.DisplayModule
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import org.bukkit.Axis
import org.bukkit.Location
import org.bukkit.block.BlockFace

class TextDisplayHandler private constructor(
	val holder: DisplayHandlerHolder,

	var anchorBlockX: Int,
	var anchorBlockY: Int,
	var anchorBlockZ: Int,

	val offsetRight: Double,
	val offsetUp: Double,
	val offsetForward: Double,

	var facing: BlockFace,
) {
	var displayModules = listOf<DisplayModule>()

	fun update() {
		if (!holder.isAlive) {
			remove()
			return
		}

		displayModules.forEach {
			it.runUpdates()
		}
	}

	fun remove() {
		displayModules.forEach {
			it.remove()
			it.deRegister()
		}

		DisplayHandlers.deRegisterHandler(this)
	}

	fun register(): TextDisplayHandler {
		displayModules.forEach {
			it.register()
			it.runUpdates()
		}

		DisplayHandlers.registerHandler(this)

		return this
	}

	fun getLocation(): Location {
		val forwardFace = facing
		val rightFace = if (forwardFace.axis == Axis.Y) BlockFace.NORTH else forwardFace.rightFace

		// Start from a center location, should be the same for every block
		val location = Location(holder.handlerGetWorld(), anchorBlockX + 0.5, anchorBlockY + 0.5, anchorBlockZ + 0.5)

		// Offset from the center location
		location.add(
			rightFace.modX * offsetRight + forwardFace.modX * offsetForward,
			offsetUp,
			rightFace.modZ * offsetRight + forwardFace.modZ * offsetForward
		)

		return location
	}

	fun displace(movement: StarshipMovement) {
		facing = movement.displaceFace(facing)

		val newX = movement.displaceX(anchorBlockX, anchorBlockZ)
		val newY = movement.displaceY(anchorBlockY)
		val newZ = movement.displaceZ(anchorBlockZ, anchorBlockX)

		anchorBlockX = newX
		anchorBlockY = newY
		anchorBlockZ = newZ

		for (display in displayModules) display.resetPosition()
	}

	class Builder(val holder: DisplayHandlerHolder, val anchorBlockX: Int, val anchorBlockY: Int, val anchorBlockZ: Int) {
		private val displays = mutableSetOf<(TextDisplayHandler) -> DisplayModule>()

		private var offsetForward = 0.0
		private var offsetRight = 0.0
		private var offsetUp = 0.0
		private var direction: BlockFace = BlockFace.NORTH

		fun addDisplay(build: (TextDisplayHandler) -> DisplayModule): Builder {
			displays.add(build)
			return this
		}

		fun setOffset(offsetRight: Double, offsetUp: Double, offsetForward: Double): Builder {
			this.offsetForward = offsetForward
			this.offsetRight = offsetRight
			this.offsetUp = offsetUp
			return this
		}

		fun setDirection(face: BlockFace): Builder {
			this.direction = face
			return this
		}

		fun build(): TextDisplayHandler {
			val handler = TextDisplayHandler(
				holder = holder,
				anchorBlockX = anchorBlockX,
				anchorBlockY = anchorBlockY,
				anchorBlockZ = anchorBlockZ,
				offsetRight = offsetRight,
				offsetUp = offsetUp,
				offsetForward = offsetForward,
				facing = direction
			)

			handler.displayModules = displays.map { build -> build.invoke(handler) }
			handler.register()

			return handler
		}
	}

	companion object {
		fun builder(holder: DisplayHandlerHolder, anchorBlockX: Int, anchorBlockY: Int, anchorBlockZ: Int): Builder {
			return Builder(holder, anchorBlockX, anchorBlockY, anchorBlockZ)
		}
	}
}
