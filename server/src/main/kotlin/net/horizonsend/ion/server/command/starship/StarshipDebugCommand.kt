package net.horizonsend.ion.server.command.starship

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.active.ai.AISpawningManager.handleSpawn
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.combat.FrigateCombatController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.combat.StarfighterCombatController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.navigation.AutoCruiseAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.util.AggressivenessLevel
import net.horizonsend.ion.server.features.starship.movement.StarshipTeleportation
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.text
import net.horizonsend.ion.server.miscellaneous.utils.title
import org.bukkit.Location
import org.bukkit.entity.Player

@CommandPermission("starlegacy.starshipdebug")
@CommandAlias("starshipdebug|sbug")
object StarshipDebugCommand : net.horizonsend.ion.server.command.SLCommand() {
	@Suppress("Unused")
	@Subcommand("teleport")
	fun onTeleport(sender: Player, x: Int, y: Int, z: Int) {
		val riding = getStarshipRiding(sender)
		StarshipTeleportation.teleportStarship(riding, Location(sender.world, x.toDouble(), y.toDouble(), z.toDouble()))
	}

	@Suppress("Unused")
	@Subcommand("thrusters")
	fun onThrusters(sender: Player) {
		val starship = getStarshipRiding(sender)
		for (dir in CARDINAL_BLOCK_FACES) {
			sender.sendRichMessage(starship.thrusterMap[dir].toString())
		}
	}

	@Suppress("Unused")
	@Subcommand("releaseall")
	fun onReleaseAll() {
		ActiveStarships.allControlledStarships().forEach { DeactivatedPlayerStarships.deactivateNow(it) }
	}

	@Suppress("Unused")
	@Subcommand("triggerSpawn")
	fun triggerSpawn(sender: Player) {
		handleSpawn()
	}

	@Suppress("Unused")
	@Subcommand("debugReason")
	fun debugReason(sender: Player) {
		val (x, y, z) = Vec3i(sender.location)

		val ship = ActiveStarships.allControlledStarships().minBy { it.centerOfMass.distance(x, y, z) }
		val controller = ship.controller as? StarfighterCombatController

		val nodes = controller?.navigationEngine?.trackedSections

		val secX = x.shr(4)
		val secY = (y - sender.world.minHeight).shr(4)
		val secZ = z.shr(4)

		val currentNode = nodes?.firstOrNull { it.location == Vec3i(secX, secY, secZ) }

		sender.title(currentNode?.reason?.text() ?: "Node not tracked.".text(), "".text())
	}

	@Subcommand("ai")
	fun ai(sender: Player, controller: AI, aggressivenessLevel: AggressivenessLevel, destinationX: Double, destinationY: Double, destinationZ: Double) {
		val destination = Location(sender.world, destinationX, destinationY, destinationZ)
		val starship = getStarshipRiding(sender)

		starship.controller = controller.createController(starship, aggressivenessLevel, destination)
		starship.removePassenger(sender.uniqueId)
	}

	enum class AI(val createController: (ActiveStarship, AggressivenessLevel, Location, ) -> AIController) {
		STARFIGHTER({ ship, aggressivenessLevel, _ ->
			StarfighterCombatController(
				starship = ship,
				target = null,
				aggressivenessLevel = aggressivenessLevel,
				previousController = null
			)
		}),

		FRIGATE({ ship, aggressivenessLevel, _ ->
			FrigateCombatController(
				starship = ship,
				target = null,
				aggressivenessLevel = aggressivenessLevel,
				previousController = null,
				autoWeaponSets = mutableListOf(),
				manualWeaponSets = mutableListOf()
			)
		}),

		AUTO_CRUISE_WITH_SF_FALLBACK(
			{ ship, aggressivenessLevel, location ->
				AutoCruiseAIController(
					ship,
					location,
					-1,
					aggressivenessLevel
				) { controller, nearbyShip ->
					StarfighterCombatController(
						controller.starship,
						nearbyShip,
						controller.aggressivenessLevel,
						controller,
					)
				}
			}
		)
	}
}
