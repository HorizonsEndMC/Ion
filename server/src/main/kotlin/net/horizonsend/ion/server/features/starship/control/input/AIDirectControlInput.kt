package net.horizonsend.ion.server.features.starship.control.input

import net.horizonsend.ion.common.utils.text.sendMessage
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import kotlin.math.PI
import kotlin.math.round

class AIDirectControlInput(override val controller: AIController
) : DirectControlInput, AIInput{
	override var isBoosting: Boolean = true
	override var selectedSpeed: Int = 1
	private var thrust = Vector()

	override fun create() {
		controller.sendMessage(text("Direct Control: ", GRAY), text("ON", NamedTextColor.RED))
	}

	override fun destroy() {
		controller.sendMessage(text("Direct Control: ", GRAY), text("OFF", NamedTextColor.RED))
		thrust = Vector()
	}


	override fun getData(): DirectControlInput.DirectControlData {

		val direction = starship.getTargetForward()

		val rotated = thrust.clone()//.multiply(-1.0)
		rotated.y *= 1.5 //stretch y a little so that ship can strafe up and down more easily
		when (direction) {
			BlockFace.NORTH -> rotated.rotateAroundX(PI /2)
			BlockFace.SOUTH -> rotated.rotateAroundX(-PI /2)
			BlockFace.WEST -> rotated.rotateAroundZ(-PI /2)
			BlockFace.EAST -> rotated.rotateAroundZ(PI /2)
			else ->rotated.rotateAroundX(0.0)
		}
		rotated.setY(0)
		//if (forwardX) rotated.rotateAroundY(PI/4) // Z -> X
		if (rotated.lengthSquared() < 1e-2) return DirectControlInput.DirectControlData(Vector(),selectedSpeed,isBoosting)
		rotated.normalize()

		rotated.x = round(rotated.x)
		rotated.z = round(rotated.z)


		return DirectControlInput.DirectControlData(rotated,selectedSpeed,isBoosting)
	}

	/** Takes in a DirectControlInput.DirectControlData to update the instructions for direct control for ai ships*/
	override fun updateInput(data: Any?) {
		//yes im cheating a little bit by reusing this class but it works
		if (data !is DirectControlInput.DirectControlData) return
		thrust = data.strafeVector
		isBoosting = data.isBoosting
		selectedSpeed = data.selectedSpeed
	}
}
