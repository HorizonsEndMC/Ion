package net.horizonsend.ion.server.command.starship

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.ai.module.targeting.TargetingModule
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.movement.StarshipTeleportation
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
	fun onReleaseAll(sender: Player) {
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
	fun release(sender: Player, identifier: String) {
		val formatted = if (identifier.contains(":".toRegex())) identifier.substringAfter(":") else identifier
		val starship = ActiveStarships[formatted] ?: fail { "Could not find target $identifier" }

		DeactivatedPlayerStarships.deactivateNow(starship as ActiveControlledStarship)
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
	@Subcommand("dumpcontroller")
	@CommandCompletion("@autoTurretTargets")
	fun listController(sender: Player, shipIdentifier: String) {
		val formatted = if (shipIdentifier.contains(":".toRegex())) shipIdentifier.substringAfter(":") else shipIdentifier

		val ship = ActiveStarships[formatted] ?: fail { "$shipIdentifier is not a starship" }
		sender.information(ship.controller.toString())

		(ship.controller as? AIController)?.let { sender.userError("Target: ${(it.modules["targeting"] as? TargetingModule)?.findTarget()}") }
	}
}
