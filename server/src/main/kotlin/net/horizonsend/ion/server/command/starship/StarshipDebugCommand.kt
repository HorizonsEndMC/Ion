package net.horizonsend.ion.server.command.starship

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import kotlinx.serialization.Serializable
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.Configuration
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.active.ai.AIControllerFactories
import net.horizonsend.ion.server.features.starship.active.ai.engine.misc.targeting.TargetingEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.positioning.AxisStandoffPositioningEngine
import net.horizonsend.ion.server.features.starship.active.ai.spawning.AISpawner
import net.horizonsend.ion.server.features.starship.active.ai.spawning.AISpawningManager
import net.horizonsend.ion.server.features.starship.active.ai.util.NPCFakePilot
import net.horizonsend.ion.server.features.starship.active.ai.util.PlayerTarget
import net.horizonsend.ion.server.features.starship.active.ai.util.StarshipTarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.movement.StarshipTeleportation
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.VisualProjectile
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.helixAroundVector
import net.kyori.adventure.text.Component.text
import org.bukkit.Bukkit
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
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandContexts.registerContext(AISpawner::class.java) { context ->
			AISpawningManager.spawners.firstOrNull { it.identifier == context.popFirstArg() } ?: throw InvalidCommandArgument("No such spawner: ${context.popFirstArg()}")
		}

		manager.commandCompletions.registerAsyncCompletion("aiSpawners") { _ ->
			AISpawningManager.spawners.map { it.identifier }
		}

		manager.commandCompletions.registerAsyncCompletion("controllerFactories") { _ ->
			AIControllerFactories.presetControllers.keys
		}

		manager.commandContexts.registerContext(AIControllerFactories.AIControllerFactory::class.java) { AIControllerFactories[it.popFirstArg()] }
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
	@Subcommand("triggerSpawn")
	@CommandCompletion("@aiSpawners")
	fun triggerSpawn(sender: Player, spawner: AISpawner) {
		sender.success("Triggered spawn for ${spawner.identifier}")
		spawner.trigger(AISpawningManager.context)
	}

	@Suppress("Unused")
	@Subcommand("dumpcontroller")
	@CommandCompletion("@autoTurretTargets")
	fun listController(sender: Player, shipIdentifier: String) {
		val formatted = if (shipIdentifier.contains(":".toRegex())) shipIdentifier.substringAfter(":") else shipIdentifier

		val ship = ActiveStarships[formatted] ?: fail { "$shipIdentifier is not a starship" }
		sender.information(ship.controller.toString())

		(ship.controller as? AIController)?.let { sender.userError("Target: ${(it.engines["targeting"] as? TargetingEngine)?.findTarget()}") }
	}

	@Subcommand("ai")
	@CommandCompletion("@controllerFactories standoffDistance x y z manualSets autoSets @autoTurretTargets ")
	fun ai(
		sender: Player,
		controller: AIControllerFactories.AIControllerFactory,
		standoffDistance: Double,
		@Optional destinationX: Double?,
		@Optional destinationY: Double?,
		@Optional destinationZ: Double?,
		@Optional manualSets: String?,
		@Optional autoSets: String?,
		@Optional target: String?,
	) {
		val destination = if (destinationX != null && destinationY != null && destinationZ != null) Location(sender.world, destinationX, destinationY, destinationZ) else null
		val starship = getStarshipRiding(sender)

		val aTarget = target?.let {
			val formatted = if (target.contains(":".toRegex())) target.substringAfter(":") else target

			Bukkit.getPlayer(formatted)?.let { PlayerTarget(it) } ?:
			ActiveStarships[formatted]?.let { StarshipTarget(it) }
		}

		starship.controller = controller.createController(
			starship,
			text("Player Created AI Ship"),
			aTarget,
			destination,
			Configuration.parse<WeaponSetsCollection>(manualSets ?: "{}").sets,
			Configuration.parse<WeaponSetsCollection>(autoSets ?: "{}").sets,
			null
		).apply {
			val positioningEngine = engines["positioning"]
			(positioningEngine as? AxisStandoffPositioningEngine)?.let { it.standoffDistance = standoffDistance }
		}

		NPCFakePilot.add(starship as ActiveControlledStarship, null)
		starship.removePassenger(sender.uniqueId)
	}

	@Serializable
	data class WeaponSetsCollection(val sets: MutableSet<AIShipConfiguration.AIStarshipTemplate.WeaponSet> = mutableSetOf())
}
