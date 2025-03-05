package net.horizonsend.ion.server.features.starship.control.input


import com.destroystokyo.paper.event.player.PlayerJumpEvent
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.util.Vector


interface InputHandler {
	val controller: Controller
	val starship get() = controller.starship

	fun create() {}
	fun destroy() {}

	fun getData() : Any
}

interface ShiftFlightInput : InputHandler {
	var pitch: Float
	var yaw: Float
	var isSneakFlying: Boolean
	var toggledSneak: Boolean
	data class ShiftFlightData(val pitch: Float, val yaw: Float, val isSneakFlying: Boolean, val toggledSneak: Boolean)
	override fun getData() : ShiftFlightData
}

interface DirectControlInput : InputHandler {
	var isBoosting: Boolean
	var selectedSpeed : Int
	data class DirectControlData(val strafeVector: Vector, val selectedSpeed : Int, val isBoosting: Boolean)
	override fun getData(): DirectControlData
}

interface DirecterControlInput : InputHandler {
	var lastDelta : Vec3i
	override fun getData(): Vec3i
}

interface PlayerInput {
	val player : Player
	fun handleMove(event: PlayerMoveEvent) {}
	fun handleSneak(event: PlayerToggleSneakEvent) {}
	fun handleJump(event: PlayerJumpEvent) {}
	fun handlePlayerHoldItem(event: PlayerItemHeldEvent) {}
}

interface AIInput {

	fun updateInput(data: Any?)

}
