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
import net.horizonsend.ion.server.features.ai.spawning.spawner.AISpawner
import net.kyori.adventure.text.Component.text
import org.bukkit.command.CommandSender
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

		manager.commandContexts.registerContext(AIControllerFactory::class.java) { AIControllerFactories[it.popFirstArg()] }
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
	@Suppress("unused")
	fun onQuery(sender: CommandSender) {
		sender.sendMessage(lineBreakWithCenterText(text("AI Spawners", HEColorScheme.HE_LIGHT_ORANGE)))

		for (spawner in AISpawningManager.spawners) {
			val line = template(
				message = text("{0}: {1} points, {2} threshold", HEColorScheme.HE_MEDIUM_GRAY),
				paramColor = HEColorScheme.HE_LIGHT_GRAY,
				useQuotesAroundObjects = false,
				spawner.identifier,
				spawner.points,
				spawner.pointThreshold
			)

			sender.sendMessage(line)
		}

		sender.sendMessage(net.horizonsend.ion.common.utils.text.lineBreak(44))
	}

	@Subcommand("spawner points set")
	@Suppress("unused")
	fun setPoints(sender: Player, spawner: AISpawner, value: Int) {
		spawner.points = value
		sender.success("Set points of ${spawner.identifier} to ${spawner.points}")
	}

	@Subcommand("spawner points add")
	@Suppress("unused")
	fun addPoints(sender: Player, spawner: AISpawner, value: Int) {
		spawner.points = (spawner.points + value)
		sender.success("Set points of ${spawner.identifier} to ${spawner.points}")
	}

	@Serializable
	data class WeaponSetsCollection(val sets: MutableSet<AIStarshipTemplate.WeaponSet> = mutableSetOf())
}
