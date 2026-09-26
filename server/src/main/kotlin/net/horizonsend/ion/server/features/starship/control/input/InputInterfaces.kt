package net.horizonsend.ion.server.features.starship.control.input


import com.destroystokyo.paper.event.player.PlayerJumpEvent
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.server.command.admin.debugBanner
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSettingOrThrow
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.control.movement.CruiseData
import net.horizonsend.ion.server.features.starship.fleet.Fleets
import net.horizonsend.ion.server.features.starship.fleet.toFleetMember
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.util.Vector
import org.litote.kmongo.mul
import kotlin.collections.indexOf
import kotlin.collections.set


interface InputHandler {
	val controller: Controller
	val starship get() = controller.starship

	fun create() {}
	fun destroy() {}

	fun getData() : Any
}

interface ShiftFlightInput : InputHandler {
	val pitch: Float
	val yaw: Float
	val isSneakFlying: Boolean
	val toggledSneak: Boolean
	data class ShiftFlightData(val pitch: Float, val yaw: Float, val isSneakFlying: Boolean, val toggledSneak: Boolean)
	override fun getData() : ShiftFlightData
}

interface DirectControlInput : InputHandler {
	val isBoosting: Boolean
	val selectedSpeed : Double

	data class DirectControlData(val strafeVector: Vector, val selectedSpeed : Double, val isBoosting: Boolean)
	override fun getData(): DirectControlData
}

interface DirecterControlInput : InputHandler {
	var lastDelta : Vec3i
	override fun getData(): Vec3i
}

interface DirectCruiseControlInput : InputHandler {
	override fun getData(): CruiseData
}

interface PlayerInput {
	val player : Player
	fun handleMove(event: PlayerMoveEvent) {}
	fun handleSneak(event: PlayerToggleSneakEvent) {}
	fun handleJump(event: PlayerJumpEvent) {}
	fun handlePlayerHoldItem(event: PlayerItemHeldEvent) {}
	fun handleTertiaryInput(starship: Starship) {
		when(TertiaryButtonControl.entries[player.getSettingOrThrow(PlayerSettings::tertiaryButtonControl)]) {
			TertiaryButtonControl.NOTHING->{}
			TertiaryButtonControl.DISRUPT-> {
				//Disrupts the ship the player is looking at
				player.debugBanner("INTERACT EVENT DISRUPT TARGETING START")
				val targetShip = ActiveStarships.getInWorld(player.world).filter {
					it.centerOfMass.toCenterVector().distanceSquared(player.location.toVector()) <=
						starship.balancing.interdictionRange * starship.balancing.interdictionRange &&
						it != ActiveStarships.findByPassenger(player)
				}.minByOrNull {
					it.centerOfMass.toCenterVector().subtract(player.location.toVector())
						.angle(player.eyeLocation.direction)
				}

				if (targetShip == starship) return //should prevent setting to yourself
				if (targetShip == null) {
					starship.userErrorAction("Disruptor Target not found")
					return
				}
				//ally or nation member
				if (starship.getRelation(targetShip).ordinal >= 5){
					starship.userErrorAction("Disruptor Target is an ally!")
					return
				}
				val thisPilot = starship.playerPilot
				val targetPilot = targetShip.playerPilot
				if ((thisPilot!= null && targetPilot != null) && Fleets.findByMember(thisPilot)?.contains(targetPilot) == true) {
					starship.userErrorAction("Disruptor Target is a member of your fleet!")
					return
				}
				starship.setIsDisrupting(starship)
				player.debugBanner("INTERACT EVENT DISRUPT TARGETING END")
			}
			TertiaryButtonControl.CHANGE_WEAPON_SET ->{
				val setOfWeaponSets = starship.weaponSets.keys().toSet()
				val currentWeaponSet = 	starship.weaponSetSelections[player.uniqueId] ?: ""

				val indexOf = setOfWeaponSets.indexOf(currentWeaponSet)+1

				//if the player hasn't selected a weapon set, or has reached the last weapon set, give them the first one in the index.
				if(indexOf == -1 || indexOf == setOfWeaponSets.size) {
					starship.weaponSetSelections[player.uniqueId] = setOfWeaponSets.first()
				}
				else{
					starship.weaponSetSelections[player.uniqueId] = setOfWeaponSets.elementAt(indexOf)
				}
				player.success("Took control of weaponset ${starship.weaponSetSelections[player.uniqueId]}")
			}
		}
	}

	enum class TertiaryButtonControl{
		NOTHING,
		DISRUPT,
		CHANGE_WEAPON_SET,
	}
}

interface AIInput {

	fun updateInput(data: Any?)

}
