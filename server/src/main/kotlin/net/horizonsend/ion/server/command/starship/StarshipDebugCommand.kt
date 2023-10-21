package net.horizonsend.ion.server.command.starship

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.active.ai.spawning.AISpawner
import net.horizonsend.ion.server.features.starship.active.ai.spawning.AISpawningManager
import net.horizonsend.ion.server.features.starship.active.ai.util.NPCFakePilot
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
object StarshipDebugCommand : SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandContexts.registerContext(AISpawner::class.java) { context ->
			AISpawningManager.spawners.firstOrNull { it.identifier == context.popFirstArg() } ?: throw InvalidCommandArgument("No such spawner: ${context.popFirstArg()}")
		}

		manager.commandCompletions.registerAsyncCompletion("aiSpawners") { _ ->
			AISpawningManager.spawners.map { it.identifier }
		}
	}

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
	@CommandCompletion("@aiSpawners")
	fun triggerSpawn(sender: Player, spawner: AISpawner) {
		sender.success("Triggered spawn for ${spawner.identifier}")
		spawner.handleSpawn()
	}

	@Subcommand("ai")
	fun ai(sender: Player, controller: AI, aggressivenessLevel: AggressivenessLevel, destinationX: Double, destinationY: Double, destinationZ: Double) {
		val destination = Location(sender.world, destinationX, destinationY, destinationZ)
		val starship = getStarshipRiding(sender)

		starship.controller = controller.createController(starship, aggressivenessLevel, destination)
		NPCFakePilot.add(starship as ActiveControlledStarship, null)
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
				autoWeaponSets = mutableListOf(
					net.horizonsend.ion.server.configuration.AIShipConfiguration.AIStarshipTemplate.WeaponSet("TT", 0.0, 1000.0)
				),
				manualWeaponSets = mutableListOf(
					net.horizonsend.ion.server.configuration.AIShipConfiguration.AIStarshipTemplate.WeaponSet("TT", 100.0, 1000.0),
					net.horizonsend.ion.server.configuration.AIShipConfiguration.AIStarshipTemplate.WeaponSet("Phasers", 0.0, 100.0),
				)
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
		),

		AUTO_CRUISE_WITH_FRIGATE_FALLBACK(
			{ ship, aggressivenessLevel, location ->
				AutoCruiseAIController(
					ship,
					location,
					-1,
					aggressivenessLevel
				) { controller, nearbyShip ->
					FrigateCombatAIController(
						controller.starship,
						nearbyShip,
						controller.aggressivenessLevel,
						mutableListOf(),
						mutableListOf()
					)
				}
			}
		)
	}
}
