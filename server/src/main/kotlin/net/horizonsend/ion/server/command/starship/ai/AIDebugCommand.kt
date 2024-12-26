package net.horizonsend.ion.server.command.starship.ai

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
import net.horizonsend.ion.common.utils.configuration.Configuration
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.lineBreakWithCenterText
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.ai.AIControllerFactories
import net.horizonsend.ion.server.features.ai.AIControllerFactory
import net.horizonsend.ion.server.features.ai.configuration.AIStarshipTemplate
import net.horizonsend.ion.server.features.ai.module.positioning.AxisStandoffPositioningModule
import net.horizonsend.ion.server.features.ai.spawning.AISpawningManager
import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import net.horizonsend.ion.server.features.ai.spawning.spawner.AISpawner
import net.horizonsend.ion.server.features.ai.spawning.spawner.AISpawners
import net.horizonsend.ion.server.features.ai.spawning.spawner.scheduler.AISpawnerTicker
import net.horizonsend.ion.server.features.ai.spawning.spawner.scheduler.LocusScheduler
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.kyori.adventure.text.Component.text
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandPermission("ion.aidebug")
@CommandAlias("aidebug")
object AIDebugCommand : SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		// Spawners
		manager.commandCompletions.registerAsyncCompletion("aiSpawners") { _ ->
			AISpawners.getAllSpawners().map { it.identifier }
		}

		manager.commandCompletions.setDefaultCompletion("aiSpawners", AISpawner::class.java)


		manager.commandContexts.registerContext(AISpawner::class.java) { context ->
			val arg = context.popFirstArg()
			AISpawners.getAllSpawners().firstOrNull { it.identifier == arg } ?: throw InvalidCommandArgument("No such spawner: $arg")
		}

		// Templates
		manager.commandCompletions.registerAsyncCompletion("spawnerTemplates") { c ->
			val spawner = c.getContextValue(AISpawner::class.java)
			spawner.getAvailableShips().map { it.template.identifier }
		}

		manager.commandCompletions.setDefaultCompletion("spawnerTemplates", AISpawner::class.java)

		manager.commandContexts.registerContext(SpawnedShip::class.java) { c ->
			val arg = c.popFirstArg()
			val spawner = c.passedArgs["spawner"] as? AISpawner ?: throw InvalidCommandArgument("No spawner specified")
			spawner.getAvailableShips().firstOrNull { it.template.identifier == arg } ?: throw InvalidCommandArgument("No template $arg")
		}

		// Control factories
		manager.commandContexts.registerContext(AIControllerFactory::class.java) { AIControllerFactories[it.popFirstArg()] }

		manager.commandCompletions.registerAsyncCompletion("controllerFactories") { _ ->
			AIControllerFactories.presetControllers.keys
		}
	}

	@Suppress("Unused")
	@Subcommand("spawner trigger")
	@CommandCompletion("@aiSpawners")
	fun triggerSpawn(sender: Player, spawner: AISpawner) {
		sender.success("Triggered spawn for ${spawner.identifier}")
		spawner.trigger(log, AISpawningManager.context)
	}

	@Subcommand("ai")
	@CommandCompletion("@controllerFactories standoffDistance x y z manualSets autoSets @autoTurretTargets ")
	fun ai(
		sender: Player,
		controller: AIControllerFactory,
		standoffDistance: Double,
		@Optional manualSets: String?,
		@Optional autoSets: String?,
	) {
		val starship = getStarshipRiding(sender)

		val newController = controller(
			starship,
			text("Player Created AI Ship"),
			Configuration.parse<WeaponSetsCollection>(manualSets ?: "{}").sets,
			Configuration.parse<WeaponSetsCollection>(autoSets ?: "{}").sets,
		).apply {
			val positioningEngine = modules["positioning"]
			(positioningEngine as? AxisStandoffPositioningModule)?.let { it.standoffDistance = standoffDistance }
		}

		starship.setController(newController)

		starship.removePassenger(sender.uniqueId)
	}

	@Subcommand("spawner query")
	fun onQuery(sender: CommandSender) {
		sender.sendMessage(lineBreakWithCenterText(text("AI Spawners", HEColorScheme.HE_LIGHT_ORANGE)))

		for (spawner in AISpawners.getAllSpawners()) {
			val line = template(
				message = text("{0}: {1}", HEColorScheme.HE_MEDIUM_GRAY),
				paramColor = HEColorScheme.HE_LIGHT_GRAY,
				useQuotesAroundObjects = false,
				spawner.identifier,
				spawner.scheduler.getTickInfo()
			)

			sender.sendMessage(line)
		}

		sender.sendMessage(net.horizonsend.ion.common.utils.text.lineBreak(44))
	}

	@Subcommand("spawner points set")
	fun setPoints(sender: Player, spawner: AISpawner, value: Int) {
		val scheduler = spawner.scheduler as? AISpawnerTicker ?: fail { "Spawner is not ticked!" }
		scheduler.points = value
		sender.success("Set points of ${spawner.identifier} to ${scheduler.points}")
	}

	@Subcommand("spawner points add")
	fun addPoints(sender: Player, spawner: AISpawner, value: Int) {
		val scheduler = spawner.scheduler as? AISpawnerTicker ?: fail { "Spawner is not ticked!" }
		scheduler.points = (scheduler.points + value)
		sender.success("Set points of ${spawner.identifier} to ${scheduler.points}")
	}

	@Serializable
	data class WeaponSetsCollection(val sets: MutableSet<AIStarshipTemplate.WeaponSet> = mutableSetOf())

	@Subcommand("spawn")
	fun spawn(sender: Player, spawner: AISpawner, template: SpawnedShip) {
		template.spawn(log, sender.location)

		sender.success("Spawned ship")
	}

	@Suppress("Unused")
	@Subcommand("dump controller")
	@CommandCompletion("@autoTurretTargets")
	fun listController(sender: Player, shipIdentifier: String) {
		val formatted = if (shipIdentifier.contains(":".toRegex())) shipIdentifier.substringAfter(":") else shipIdentifier

		val ship = ActiveStarships[formatted] ?: fail { "$shipIdentifier is not a starship" }
		sender.information(ship.controller.toString())

		(ship.controller as? AIController)?.let { sender.userError(it.modules.entries.joinToString(separator = "\n") { mod ->
			"[${mod.key}] = ${mod.value}" })
		}
	}

	@Subcommand("trigger locus")
	@CommandCompletion("@aiSpawners")
	fun listController(sender: Player, spawner: AISpawner) {
		val scheduler = spawner.scheduler as? LocusScheduler ?: fail { "Spawner's scheduler is not a locus" }
		scheduler.start()
	}
}
