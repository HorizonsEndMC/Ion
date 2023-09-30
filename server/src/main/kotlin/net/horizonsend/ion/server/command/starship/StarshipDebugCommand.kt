package net.horizonsend.ion.server.command.starship

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.active.ai.AISpawningManager.handleSpawn
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AutoCruiseAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.StarfighterCombatController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.util.AggressivenessLevel
import net.horizonsend.ion.server.features.starship.movement.StarshipTeleportation
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.helixAroundVector
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.text
import net.horizonsend.ion.server.miscellaneous.utils.title
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.World
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
	@Subcommand("dumpSubsystems")
	fun onDumpSubsystems(sender: Player) {
		val starship = getStarshipRiding(sender)

		sender.information(starship.subsystems.joinToString { it.javaClass.simpleName })
	}

	@Subcommand("testVector")
	fun onTestVector(sender: Player, particle: Particle, radius: Double, points: Int, step: Double, length: Double, wavelength: Double, offset: Double) {
		val dir = sender.location.direction
		val origin = sender.eyeLocation.clone().add(dir)

		val direction = dir.clone().normalize().multiply(length)

		helixAroundVector(origin, direction, radius, points, step = step, wavelength = wavelength, offsetRadians = offset).forEach {
			sender.world.spawnParticle(particle, it, 1, 0.0, 0.0, 0.0, 0.0, null)
		}
	}

	@Suppress("Unused")
	@Subcommand("loadAI")
	fun loadAI(sender: Player, name: String) {
		val (data, schematic) = StarshipDealers.schematicMap.filter { it.key.schematicName == name }.firstNotNullOfOrNull { it } ?: fail { "Sold ship $name not found!" }

		AIUtils.createFromClipboard(
			sender.location,
			schematic,
			data.shipType,
			data.displayName,
			{ ship -> AIControllers.dumbAI(ship) }
		)
	}

	@Suppress("Unused")
	@Subcommand("triggerSpawn")
	fun triggerSpawn(sender: Player) {
		handleSpawn()
	}

	@CommandCompletion("destinationX destinationY destinationZ")
	fun ai(sender: Player, controller: AI, aggressivenessLevel: AggressivenessLevel, destinationX: Double, destinationY: Double, destinationZ: Double) {
		val destination = Location(sender.world, destinationX, destinationY, destinationZ)
		val starship = getStarshipRiding(sender)

		starship.controller = controller.createController(starship, aggressivenessLevel, destination)
	}

	enum class AI(val createController: (ActiveStarship, AggressivenessLevel, Location, ) -> AIController) {
		ONLY_ONE_FOR_NOW(
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
						controller,
						controller.aggressivenessLevel
					)
				}
			}
		)
	}
}
