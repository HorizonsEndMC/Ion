package net.horizonsend.ion.server.features.starship.control.input

import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.control.movement.StarshipControl
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.features.starship.movement.StarshipMovementException
import net.horizonsend.ion.server.features.starship.movement.TranslateMovement
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockTypeSafe
import net.horizonsend.ion.server.miscellaneous.utils.isTankPassable
import net.horizonsend.ion.server.miscellaneous.utils.vectorToPitchYaw
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.util.Vector
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

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
