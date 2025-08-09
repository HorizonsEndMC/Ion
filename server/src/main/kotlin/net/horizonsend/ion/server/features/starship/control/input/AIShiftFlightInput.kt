package net.horizonsend.ion.server.features.starship.control.input

import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.vectorToPitchYaw
import org.bukkit.util.Vector

class AIShiftFlightInput(override val controller: AIController,
) : ShiftFlightInput,AIInput {
	override var pitch: Float = 0f
	override var yaw: Float = 0f
	override var isSneakFlying: Boolean = false
	private var previousState = false
	override var toggledSneak = false


	/** Update ship flight direction by taking in a Vector. Will stop moving if provided a null vector **/
	override fun updateInput(data: Any?) {
		if (data !is Vector?) return
		if (data == null) {
			isSneakFlying = false
			return
		}

		val (newPitch, newYaw) = vectorToPitchYaw(data)
		pitch = newPitch
		yaw = newYaw
		isSneakFlying = true
	}

	override fun getData(): ShiftFlightInput.ShiftFlightData {
		if (previousState != isSneakFlying) {
			toggledSneak = true
			previousState = isSneakFlying
		}

		val temp = toggledSneak
		toggledSneak = false
		return ShiftFlightInput.ShiftFlightData(pitch, yaw, isSneakFlying, temp)
	}
}
