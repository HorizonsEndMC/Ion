package net.horizonsend.ion.server.command.starship

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.ai.module.targeting.TargetingModule
import net.horizonsend.ion.server.features.misc.UnusedSoldShipPurge
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.player.ActivePlayerController
import net.horizonsend.ion.server.features.starship.control.movement.DirecterControlHandler
import net.horizonsend.ion.server.features.starship.control.input.PlayerDirectControlInput
import net.horizonsend.ion.server.features.starship.control.input.PlayerDirecterControlInput
import net.horizonsend.ion.server.features.starship.control.input.PlayerShiftFlightInput
import net.horizonsend.ion.server.features.starship.control.movement.DirectControlHandler
import net.horizonsend.ion.server.features.starship.control.movement.ShiftFlightHandler
import net.horizonsend.ion.server.features.starship.movement.StarshipTeleportation
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.VariableVisualProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.VisualProjectile
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.helixAroundVector
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.Vector

@CommandPermission("starlegacy.starshipdebug")
@CommandAlias("starshipdebug|sbug")
object StarshipDebugCommand : SLCommand() {
	@Suppress("Unused")
	@Subcommand("teleport")
	fun onTeleport(sender: Player, world: World, x: Int, y: Int, z: Int) {
		val riding = getStarshipRiding(sender)
		StarshipTeleportation.teleportStarship(riding, Location(world, x.toDouble(), y.toDouble(), z.toDouble()))
	}

	@Suppress("Unused")
	@Subcommand("thrusters")
	fun onThrusters(sender: Player) {
		val starship = getStarshipRiding(sender)
		for (dir in CARDINAL_BLOCK_FACES) {
			sender.sendRichMessage(starship.thrusterMap[dir].toString())
		}
	}

	@Subcommand("query map")
	fun onQueryMap(sender: CommandSender) {
		sender.information("All world ships:")

		for (world in IonServer.server.worlds) {
			sender.information("${world.name}: ${ActiveStarships.getInWorld(world).joinToString { it.identifier }}")
		}
	}

	@Suppress("Unused")
	@Subcommand("releaseall")
	fun onReleaseAll(sender: CommandSender) {
		var released = 0
		ActiveStarships.allControlledStarships().forEach {
			DeactivatedPlayerStarships.deactivateNow(it)
			released++
		}

		sender.success("Released $released ships")
	}

	@Suppress("Unused")
	@Subcommand("release")
	@CommandCompletion("@autoTurretTargets")
	fun release(sender: CommandSender, identifier: String) {
		val formatted = if (identifier.contains(":".toRegex())) identifier.substringAfter(":") else identifier
		val starship = ActiveStarships[formatted] ?: fail { "Could not find target $identifier" }

		DeactivatedPlayerStarships.deactivateNow(starship)
		sender.success("Released $identifier")
	}

	@Suppress("Unused")
	@Subcommand("dumpSubsystems")
	fun onDumpSubsystems(sender: Player) {
		val starship = getStarshipRiding(sender)

		sender.information(starship.subsystems.joinToString { it.javaClass.simpleName })
	}

	@Subcommand("testVector")
	@Suppress("Unused")
	fun onTestVector(sender: Player, particle: Particle, radius: Double, points: Int, step: Double, length: Double, wavelength: Double, offset: Double) {
		val dir = sender.location.direction
		val origin = sender.eyeLocation.clone().add(dir)

		val direction = dir.clone().normalize().multiply(length)

		helixAroundVector(origin, direction, radius, points, step = step, wavelength = wavelength, offsetRadians = offset).forEach {
			sender.world.spawnParticle(particle, it, 1, 0.0, 0.0, 0.0, 0.0, null)
		}
	}

	@Subcommand("testVectorParticle")
	@Suppress("Unused")
	fun onTestVectorParticle(sender: Player, speed: Double, radius: Double, points: Int, step: Double, length: Double, wavelength: Double, offset: Double) {
		val dir = sender.location.direction
		val origin = sender.eyeLocation.clone().add(dir)

		VariableVisualProjectile(
			origin,
			dir,
			500.0,
			speed,
		) { loc, travel ->
			val vector = dir.clone().normalize().multiply(travel)

			helixAroundVector(loc, vector, radius, points, step = step, wavelength = wavelength, offsetRadians = offset) {
				loc.world.spawnParticle(
					Particle.WAX_OFF,
					it,
					0,
					0.0,
					0.0,
					0.0,
					0.0,
					null,
					true
				)
			}
		}.fire()
	}

	@Subcommand("ride")
	@Suppress("Unused")
	fun onRide(sender: Player) {
		val starships = ActiveStarships.getInWorld(sender.world)

		val (x, y, z) = Vec3i(sender.location).below(1)

		val starship = starships.firstOrNull { it.contains(x, y, z) } ?: return sender.userError("You're not standing on a starship!")

		starship.addPassenger(sender.uniqueId)
	}

	@Suppress("Unused")
	@Subcommand("visualProjectile")
	fun visualProjectile(
		sender: CommandSender,
		originWorld: World,
		originX: Double,
		originY: Double,
		originZ: Double,
		destinationX: Double,
		destinationY: Double,
		destinationZ: Double,
		range: Double,
		speed: Double,
		color: Int,
		particleThickness: Float,
		extraParticles: Int,
	) {
		val origin = Location(originWorld, originX, originY, originZ)
		val destination = Vector(destinationX, destinationY, destinationZ)

		val dir = destination.clone().subtract(origin.toVector())

		VisualProjectile(
			Location(originWorld, originX, originY, originZ),
			dir,
			range,
			speed,
			Color.fromRGB(color),
			particleThickness,
			extraParticles
		).fire()

		sender.success("Spawned projectile")
	}

	@Suppress("Unused")
	@Subcommand("dump controller")
	@CommandCompletion("@autoTurretTargets")
	fun listController(sender: Player, shipIdentifier: String) {
		val formatted = if (shipIdentifier.contains(":".toRegex())) shipIdentifier.substringAfter(":") else shipIdentifier

		val ship = ActiveStarships[formatted] ?: fail { "$shipIdentifier is not a starship" }
		sender.information(ship.controller.toString())

		(ship.controller as? AIController)?.let { sender.userError("Target: ${(it.coreModules[TargetingModule::class] as? TargetingModule)?.findTarget()}") }
	}

	@Subcommand("purge now")
	fun onPurge(sender: Player) {
		sender.information("purging")
		UnusedSoldShipPurge.purgeNoobShuttles()
	}

	@Subcommand("set movement")
	fun onSetMovement(sender: Player, type: MovementType) {
		val ship = getStarshipPiloting(sender)
		val controller = ship.controller as? ActivePlayerController ?: fail { "bruh" }
		type.apply(controller)
	}
	@Suppress("unused") // entrypoints
	enum class MovementType {
		SHIFT_FLIGHT {
			override fun apply(controller: ActivePlayerController) {
				controller.movementHandler = ShiftFlightHandler(controller, PlayerShiftFlightInput(controller))
			}
		},
		DIRECT_CONTROL {
			override fun apply(controller: ActivePlayerController) {
				controller.movementHandler = DirectControlHandler(controller, PlayerDirectControlInput(controller))
			}
		},
		DIRECTER_CONTROL {
			override fun apply(controller: ActivePlayerController) {
				controller.movementHandler = DirecterControlHandler(controller, PlayerDirecterControlInput(controller))
			}
		},

		;

		abstract fun apply(controller: ActivePlayerController)
	}

	@Suppress("Unused")
	@Subcommand("togglestats")
	@CommandCompletion("@autoTurretTargets")
	fun toggleStats(sender: Player, identifier: String) {
		val formatted = if (identifier.contains(":".toRegex())) identifier.substringAfter(":") else identifier
		val ship = ActiveStarships[formatted] ?: fail { "$identifier is not a starship" }
		ship.statsEnabled = !ship.statsEnabled
		sender.information("Toggled stats for $identifier to ${ship.statsEnabled}")
	}

	@Suppress("Unused")
	@Subcommand("toggleforecast")
	@CommandCompletion("@autoTurretTargets")
	fun toggleForecast(sender: Player, identifier: String) {
		val formatted = if (identifier.contains(":".toRegex())) identifier.substringAfter(":") else identifier
		val ship = ActiveStarships[formatted] ?: fail { "$identifier is not a starship" }
		ship.forecastEnabled = !ship.forecastEnabled
		sender.information("Toggled forecast for $identifier to ${ship.forecastEnabled}")
	}
}
