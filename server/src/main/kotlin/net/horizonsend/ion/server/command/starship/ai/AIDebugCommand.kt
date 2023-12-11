package net.horizonsend.ion.server.command.starship.ai

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import kotlinx.serialization.Serializable
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.Configuration
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.active.ai.AIControllerFactories
import net.horizonsend.ion.server.features.starship.active.ai.module.positioning.AxisStandoffPositioningModule
import net.horizonsend.ion.server.features.starship.active.ai.spawning.AISpawner
import net.horizonsend.ion.server.features.starship.active.ai.spawning.AISpawningManager
import net.horizonsend.ion.server.features.starship.active.ai.util.PlayerTarget
import net.horizonsend.ion.server.features.starship.active.ai.util.StarshipTarget
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player

@CommandPermission("ion.aidebug")
@CommandAlias("aidebug")
object AIDebugCommand : SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandContexts.registerContext(AISpawner::class.java) { context ->
			AISpawningManager.spawners.firstOrNull { it.identifier == context.firstArg } ?: throw InvalidCommandArgument("No such spawner: ${context.firstArg}")
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
	@Subcommand("triggerSpawn")
	@CommandCompletion("@aiSpawners")
	fun triggerSpawn(sender: Player, spawner: AISpawner) {
		sender.success("Triggered spawn for ${spawner.identifier}")
		spawner.trigger(AISpawningManager.context)
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
			Component.text("Player Created AI Ship"),
			aTarget,
			destination,
			Configuration.parse<WeaponSetsCollection>(manualSets ?: "{}").sets,
			Configuration.parse<WeaponSetsCollection>(autoSets ?: "{}").sets,
			null
		).apply {
			val positioningEngine = modules["positioning"]
			(positioningEngine as? AxisStandoffPositioningModule)?.let { it.standoffDistance = standoffDistance }
		}

//		NPCFakePilot.add(starship as ActiveControlledStarship, null)
		starship.removePassenger(sender.uniqueId)
	}

	@Serializable
	data class WeaponSetsCollection(val sets: MutableSet<AIShipConfiguration.AIStarshipTemplate.WeaponSet> = mutableSetOf())
}
