package net.horizonsend.ion.server.features.starship.control.input

import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.control.movement.PlayerStarshipControl.isHoldingController
import net.horizonsend.ion.server.features.starship.control.movement.StarshipControl
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.features.starship.movement.StarshipMovementException
import net.horizonsend.ion.server.features.starship.movement.TranslateMovement
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockTypeSafe
import net.horizonsend.ion.server.miscellaneous.utils.isTankPassable
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

class PlayerShiftFlightInput(override val controller: PlayerController,
) : ShiftFlightInput,PlayerInput {
	override val player get() = controller.player
	override var pitch : Float
		get() = player.pitch
		set(value) {}
	override var yaw : Float
		get() = player.yaw
		set(value) {}
	override var isSneakFlying : Boolean
		get() = player.isSneaking && isHoldingController(player)
		set(value) {}
	override var toggledSneak = false

	override fun handleSneak(event: PlayerToggleSneakEvent) {
		toggledSneak = true
	}
	override fun getData(): ShiftFlightInput.ShiftFlightData {
		val temp = toggledSneak
		toggledSneak = false
		return ShiftFlightInput.ShiftFlightData(pitch, yaw, isSneakFlying, temp)
	}
}
