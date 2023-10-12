package net.horizonsend.ion.server.command.starship

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.active.ai.spawning.AISpawningManager.handleSpawn
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.combat.FrigateCombatAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.combat.StarfighterCombatAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.combat.TemporaryStarfighterCombatAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.navigation.AutoCruiseAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.utils.AggressivenessLevel
import net.horizonsend.ion.server.features.starship.movement.StarshipTeleportation
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
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

	@Subcommand("ai")
	fun ai(sender: Player, controller: AI, aggressivenessLevel: AggressivenessLevel, destinationX: Double, destinationY: Double, destinationZ: Double) {
		val destination = Location(sender.world, destinationX, destinationY, destinationZ)
		val starship = getStarshipRiding(sender)

		starship.controller = controller.createController(starship, aggressivenessLevel, destination)
		starship.removePassenger(sender.uniqueId)
	}

	enum class AI(val createController: (ActiveStarship, AggressivenessLevel, Location) -> AIController) {
		STARFIGHTER({ ship, aggressivenessLevel, _ ->
			StarfighterCombatAIController(
				starship = ship,
				target = null,
				aggressivenessLevel = aggressivenessLevel
			)
		}),

		FRIGATE({ ship, aggressivenessLevel, _ ->
			FrigateCombatAIController(
				starship = ship,
				target = null,
				aggressivenessLevel = aggressivenessLevel,
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
					TemporaryStarfighterCombatAIController(
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
