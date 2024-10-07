package net.horizonsend.ion.server.command.misc

import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.hint
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.ai.AIControllerFactories
import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.ai.spawning.createAIShipFromTemplate
import net.horizonsend.ion.server.features.ai.spawning.spawner.AISpawners
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.destruction.StarshipDestruction
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
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
			AITemplateRegistry.all()[context.popFirstArg()]
		}

		manager.commandCompletions.setDefaultCompletion("allTemplates", AITemplate::class.java)
	}

	@Subcommand("summon")
	fun summon(sender: Player, template: AITemplate) {
		val world = sender.world
		failIf(!world.ion.hasFlag(WorldFlag.AI_ARENA)) { "AI Opponents may only be spawned in arena worlds!" }

		sender.hint("Spawning ${template.starshipInfo.miniMessageName}")

		summonShip(sender, template, null)
	}

	@Subcommand("summon")
	fun summon(sender: Player, template: AITemplate, x: Int, y: Int, z: Int) {
		val world = sender.world
		failIf(!world.ion.hasFlag(WorldFlag.AI_ARENA)) { "AI Opponents may only be spawned in arena worlds!" }

		sender.hint("Spawning ${template.starshipInfo.miniMessageName}")

		summonShip(sender, template, Vec3i(x, y, z))
	}

	private fun summonShip(summoner: Player, template: AITemplate, vec: Vec3i?) {
		val location = vec?.toLocation(summoner.world) ?: summoner.location.add(summoner.location.direction.multiply(500.0))

		Tasks.async {
			if (getExisting(summoner).isNotEmpty()) return@async summoner.userError("You may only have one AI opponent active at once.")

			Tasks.sync {
				createAIShipFromTemplate(
					log,
					template,
					location,
					{ starship ->
						val factory = AIControllerFactories[template.behaviorInformation.controllerFactory]
						val controller = factory.invoke(starship, template.starshipInfo.componentName())
						processController(summoner, controller)
						controller
					}
				) {
					summoner.success("Summoned ${template.starshipInfo.miniMessageName}")
				}
			}
		}
	}

	private fun processController(summoner: Player, controller: AIController) {
		controller.modules["opponent"] = OpponentTrackerModule(controller, summoner.uniqueId)
	}

	fun getExisting(player: Player): Collection<Starship> {
		return ActiveStarships.all().filter {
			val controller = it.controller
			if (controller !is AIController) return@filter false

			val trackers = controller.modules.values.filterIsInstance<OpponentTrackerModule>()
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
					StarshipDestruction.vanish(it, true)
					sender.success("Removed ${it.identifier}")
				}
			}
		}
	}

	class OpponentTrackerModule(controller: AIController, val opponent: UUID) : AIModule(controller) {
		override fun tick() {
			if (Bukkit.getPlayer(opponent) == null) StarshipDestruction.vanish(starship, true)
		}
	}
}
