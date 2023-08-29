package net.horizonsend.ion.server.features.starship.controllers.ai

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.movement.AIControlUtils
import net.horizonsend.ion.server.features.starship.control.weaponry.StarshipWeaponry
import net.horizonsend.ion.server.miscellaneous.utils.keysSortedByValue
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.entity.Player

class DummyAIController(starship: Starship) : AIController(starship, "AI") {
	override val pilotName: Component = text("AI Pilot Matrix", NamedTextColor.RED, TextDecoration.BOLD)

	override var yaw: Float = 0.0F
	override var pitch: Float = 0.0F

	override var selectedDirectControlSpeed: Int = 0

	override var isShiftFlying: Boolean = false

	override fun canDestroyBlock(block: Block): Boolean {
		return false
	}

	override fun canPlaceBlock(block: Block, newState: BlockState, placedAgainst: Block): Boolean {
		return false
	}

	override fun tick() {
		val location = starship.centerOfMass.toLocation(starship.world)
		val nearestPlayer = getNearestPlayer(location)

		AIControlUtils.shiftFlyTowardsPlayer(this, nearestPlayer)
		setWeaponsToNearest(location, nearestPlayer)
	}

	private fun getNearestPlayer(location: Location) = IonServer.server.onlinePlayers
		.filter { it.world == starship.world }
		.associateWith { it.location.distance(location) }
		.filter { it.value <= 5000 }
		.filter { it.value >= 50 }
		.keysSortedByValue()
		.firstOrNull()

	private fun setWeaponsToNearest(location: Location, nearest: Player?) {
		if (starship !is ActiveStarship) return

		if (nearest == null) {
			starship.autoTurretTargets.clear()
			return
		}

		val nearestShip = ActiveStarships.findByPilot(nearest) ?: return
		val projectedPosition = nearestShip.centerOfMass.toVector().add(nearestShip.velocity)
		val dir = projectedPosition.subtract(location.toVector()).normalize()

//		for ((name, weapons) in starship.weaponSets.asMap()) {
//			if (weapons.all { it !is AutoWeaponSubsystem }) continue
//
//			starship.autoTurretTargets[name] = nearest.uniqueId
//		}

		StarshipWeaponry.manualFire(
			this,
			starship,
			true,
			starship.forward,
			dir,
			projectedPosition,
			null
		)
	}
}
