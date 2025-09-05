package net.horizonsend.ion.server.command.misc

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import kotlinx.coroutines.launch
import net.horizonsend.ion.common.extensions.hint
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.ai.AIControllerFactories
import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.ai.module.misc.DifficultyModule
import net.horizonsend.ion.server.features.ai.module.misc.ReinforcementSpawnerModule
import net.horizonsend.ion.server.features.ai.spawning.AISpawningManager
import net.horizonsend.ion.server.features.ai.spawning.createAIShipFromTemplate
import net.horizonsend.ion.server.features.ai.spawning.spawner.AISpawners
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.destruction.StarshipDestruction
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

@CommandAlias("aiopponent")
@CommandPermission("ion.command.aiopponent")
object AIOpponentCommand : SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandCompletions.registerAsyncCompletion("allTemplates") {
			AISpawners.getAllSpawners().flatMap { it.getAvailableShips() }.map { it.template.identifier }.distinct()
		}

		manager.commandContexts.registerContext(AITemplate::class.java) { context ->
			val arg = context.popFirstArg()
			AITemplateRegistry.all()[arg.uppercase()] ?: throw InvalidCommandArgument("Template $arg not found!")
		}

		manager.commandCompletions.setDefaultCompletion("allTemplates", AITemplate::class.java)

		manager.commandCompletions.registerAsyncCompletion("targetMode") { _ ->
			AITarget.TargetMode.entries.map { it.name }
		}
	}

	@Subcommand("summon")
	@CommandCompletion("@allTemplates @AIDifficulty @targetMode")
	fun summon(
		sender: Player,
		template: AITemplate,
		@Optional difficulty : DifficultyModule.Companion.AIDifficulty?,
		@Optional targetMode : String?
	) {
		val world = sender.world
		failIf(!world.ion.hasFlag(WorldFlag.AI_ARENA)) { "AI Opponents may only be spawned in arena worlds!" }

		sender.hint("Spawning ${template.starshipInfo.miniMessageName}")

		summonShip(sender, template, null, difficulty?.ordinal, targetMode?.let { AITarget.TargetMode.valueOf(it) } ?: AITarget.TargetMode.PLAYER_ONLY)
	}

	@Subcommand("summon")
	@CommandCompletion("@allTemplates x y z @AIDifficulty @targetMode")
	fun summon(
		sender: Player,
		template: AITemplate,
		x: Int, y: Int, z: Int,
		@Optional difficulty : DifficultyModule.Companion.AIDifficulty?,
		@Optional targetMode : String?
	) {
		val world = sender.world
		failIf(!world.ion.hasFlag(WorldFlag.AI_ARENA)) { "AI Opponents may only be spawned in arena worlds!" }

		sender.hint("Spawning ${template.starshipInfo.miniMessageName}")

		summonShip(sender, template, Vec3i(x, y, z), difficulty?.ordinal,
			targetMode?.let { AITarget.TargetMode.valueOf(it) } ?: AITarget.TargetMode.PLAYER_ONLY)
	}

	@Subcommand("summonunlimited")
	@CommandCompletion("@allTemplates @AIDifficulty @targetMode")
	@CommandPermission("ion.command.aiopponent.unlimited")
	fun summonUnlimited(
		sender: Player,
		template: AITemplate,
		@Optional difficulty : DifficultyModule.Companion.AIDifficulty?,
		@Optional targetMode : String?
	) {
		val world = sender.world
		failIf(!world.ion.hasFlag(WorldFlag.AI_ARENA)) { "AI Opponents may only be spawned in arena worlds!" }
		//failIf((difficulty != null) && (difficulty > 5 || difficulty < 0)) {"Difficulty must be b/w 0 and 5"}

		sender.hint("Spawning ${template.starshipInfo.miniMessageName}")

		summonShip(sender, template, null, difficulty?.ordinal,
			targetMode?.let { AITarget.TargetMode.valueOf(it) } ?: AITarget.TargetMode.PLAYER_ONLY, false)
	}

	@Subcommand("summonunlimited")
	@CommandCompletion("@allTemplates x y z @AIDifficulty @targetMode")
	@CommandPermission("ion.command.aiopponent.unlimited")
	fun summonUnlimited(
		sender: Player,
		template: AITemplate,
		x: Int, y: Int, z: Int,
		@Optional difficulty : DifficultyModule.Companion.AIDifficulty?,
		@Optional targetMode : String?
	) {
		val world = sender.world
		failIf(!world.ion.hasFlag(WorldFlag.AI_ARENA)) { "AI Opponents may only be spawned in arena worlds!" }

		sender.hint("Spawning ${template.starshipInfo.miniMessageName}")

		summonShip(sender, template, Vec3i(x, y, z), difficulty?.ordinal,
			targetMode?.let { AITarget.TargetMode.valueOf(it) } ?: AITarget.TargetMode.PLAYER_ONLY, false)
	}


	private fun summonShip(summoner: Player, template: AITemplate, vec: Vec3i?, difficulty: Int?, targetMode: AITarget.TargetMode, limitSpawns: Boolean = true) {
		val location = vec?.toLocation(summoner.world) ?: summoner.location.add(summoner.location.direction.multiply(500.0)).apply { y = 192.0 }

		Tasks.async {
			if (limitSpawns && getExisting(summoner).isNotEmpty()) return@async summoner.userError("You may only have one AI opponent active at once.")

			Tasks.sync {
				try {

				AISpawningManager.context.launch {
					createAIShipFromTemplate(
						logger = log,
						template = template,
						location = location,
						createController = { starship ->
							val factory = AIControllerFactories[template.behaviorInformation.controllerFactory]
							val controller = factory.invoke(
								starship,
								template.starshipInfo.componentName(),
								template.starshipInfo.autoWeaponSets,
								template.starshipInfo.manualWeaponSets,
								difficulty ?: template.difficulty.get(),
								targetMode
							)

							processController(summoner, controller)
							controller
						},
						suffix = "✦".repeat((difficulty ?: template.difficulty.get())+1),
						callback = { starship ->
							summoner.success("Summoned ${template.starshipInfo.miniMessageName}")

							val reinforcementModules = (starship.controller as? AIController)?.getAllModules()?.filterIsInstance<ReinforcementSpawnerModule>().orEmpty()

							reinforcementModules.forEach { module ->
								module.controllerModifiers.add { summonedController -> processController(summoner, summonedController) }
							}
						}
					)
				}
				} catch (e: Throwable) {
					summoner.serverError("There was an error spawning ${template.identifier}: ${e.message}")
				}
			}
		}
	}

	private fun processController(summoner: Player, controller: AIController) {
		controller.coreModules[OpponentTrackerModule::class] = OpponentTrackerModule(controller, summoner.uniqueId)
	}

	fun getExisting(player: Player): Collection<Starship> {
		return ActiveStarships.all().filter {
			val controller = it.controller
			if (controller !is AIController) return@filter false

			val trackers = controller.coreModules.values.filterIsInstance<OpponentTrackerModule>()
			if (trackers.isEmpty()) return@filter false

			trackers.any { tracker -> tracker.opponent == player.uniqueId }
		}
	}

	@Subcommand("despawn")
	fun deSpawn(sender: Player) {
		// Don't want to clog async command thread
		Tasks.async {
			val toRemove = getExisting(sender)

			Tasks.sync {
				toRemove.forEach {
					StarshipDestruction.vanish(starship = it, urgent = true)
					sender.success("Removed ${it.identifier}")
				}
			}
		}
	}

	class OpponentTrackerModule(controller: AIController, val opponent: UUID) : AIModule(controller) {
		override fun tick() {
			if (Bukkit.getPlayer(opponent) == null) StarshipDestruction.vanish(starship = starship, urgent = true)
		}
	}
}
