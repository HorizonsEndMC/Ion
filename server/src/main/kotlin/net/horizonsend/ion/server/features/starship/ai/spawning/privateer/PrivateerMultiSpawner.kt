package net.horizonsend.ion.server.features.starship.ai.spawning.privateer

import net.horizonsend.ion.common.utils.text.HEColorScheme
import net.horizonsend.ion.common.utils.text.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.templateMiniMessage
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.features.starship.ai.spawning.template.BasicSpawner
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Location

class PrivateerMultiSpawner : BasicSpawner(
	"PRIVATEER_MULTI",
	IonServer.aiShipConfiguration.spawners::privateerMulti,
) {
	override fun findSpawnLocation(): Location? = findPrivateerSpawnLocation(configuration)

	override val spawnMessage: Component? = null

	private fun patrolTriggerMessage(location: Location) = ofChildren(
		text(location.world.name, HE_LIGHT_GRAY),
		text(" System Defense Forces ", PRIVATEER_LIGHT_TEAL),
		text("have started a patrol.", HE_LIGHT_GRAY)
	)

	override suspend fun triggerSpawn() {
		val loc = findSpawnLocation() ?: return
		val (x, y, z) = Vec3i(loc)

		if (!spawningConditionsMet(loc.world, x, y, z)) return

		val ships = getStarshipTemplates(loc.world)

		Notify.online(patrolTriggerMessage(loc))

		for ((template, pilotName) in ships) {
			val deferred = spawnAIStarship(template, loc, createController(template, pilotName))

			deferred.invokeOnCompletion {
				IonServer.server.sendMessage(
					templateMiniMessage(
					message = configuration.miniMessageSpawnMessage,
					paramColor = HE_LIGHT_GRAY,
					useQuotesAroundObjects = false,
					template.getName(),
					x,
					y,
					z,
					loc.world.name
				)
				)
			}
		}
	}

	companion object {
		val defaultConfiguration = AIShipConfiguration.AISpawnerConfiguration(
			miniMessageSpawnMessage = "<$PRIVATEER_LIGHT_TEAL>Privateer patrol <${HEColorScheme.HE_MEDIUM_GRAY}>operation vessel {0} spawned at {1}, {2}, {3}, in {4}",
			pointChance = 0.5,
			pointThreshold = 20 * 60 * 15,
			tiers = listOf(
				AIShipConfiguration.AISpawnerTier(
					identifier = "EASY",
					nameList = mapOf(
						"<${PRIVATEER_MEDIUM_TEAL}>System Defense <${PRIVATEER_LIGHT_TEAL}>Rookie" to 5,
						"<${PRIVATEER_MEDIUM_TEAL}>System Defense <${PRIVATEER_LIGHT_TEAL}>Trainee" to 2,
					),
					ships = mapOf(
						dagger.identifier to 2,
						protector.identifier to 2,
						furious.identifier to 2,
						inflict.identifier to 2
					)
				),
				AIShipConfiguration.AISpawnerTier(
					identifier = "NORMAL",
					nameList = mapOf(
						"<${PRIVATEER_MEDIUM_TEAL}>System Defense <${PRIVATEER_LIGHT_TEAL}>Rookie" to 2,
						"<${PRIVATEER_MEDIUM_TEAL}>System Defense <${PRIVATEER_LIGHT_TEAL}>Trainee" to 5,
					),
					ships = mapOf(
						protector.identifier to 2,
						furious.identifier to 2,
						inflict.identifier to 2,
						veteran.identifier to 1,
						patroller.identifier to 1
					)
				),
				AIShipConfiguration.AISpawnerTier(
					identifier = "MODERATE",
					nameList = mapOf(
						"<${PRIVATEER_MEDIUM_TEAL}>System Defense <${PRIVATEER_LIGHT_TEAL}>Pilot" to 5,
						"<${PRIVATEER_MEDIUM_TEAL}>System Defense <${PRIVATEER_LIGHT_TEAL}>Veteran" to 2,
					),
					ships = mapOf(
						contractor.identifier to 2,
						teneta.identifier to 2,
						veteran.identifier to 2,
						patroller.identifier to 2
					)
				),
				AIShipConfiguration.AISpawnerTier(
					identifier = "ADVANCED",
					nameList = mapOf(
						"<${PRIVATEER_MEDIUM_TEAL}>System Defense <${PRIVATEER_LIGHT_TEAL}>Veteran" to 5,
						"<${PRIVATEER_MEDIUM_TEAL}>System Defense <${PRIVATEER_LIGHT_TEAL}>Ace" to 2,
						"<${PRIVATEER_MEDIUM_TEAL}>System Defense <${PRIVATEER_LIGHT_TEAL}>Pilot" to 2
					),
					ships = mapOf(
						contractor.identifier to 4,
						daybreak.identifier to 2,
						veteran.identifier to 4,
						patroller.identifier to 2
					)
				),
				AIShipConfiguration.AISpawnerTier(
					identifier = "EXPERT",
					nameList = mapOf(
						"<${PRIVATEER_MEDIUM_TEAL}>Expert Privateer <${PRIVATEER_LIGHT_TEAL}>Ace" to 5,
						"<${PRIVATEER_MEDIUM_TEAL}>Expert Privateer <${PRIVATEER_LIGHT_TEAL}>Veteran" to 2
					),
					ships = mapOf(
						bulwark.identifier to 4,
						daybreak.identifier to 2,
						contractor.identifier to 1,
					)
				)
			),
			worldSettings = listOf(
				AIShipConfiguration.AIWorldSettings(world = "Asteri", rolls = 7, tiers = mapOf(
					"EASY" to 2,
					"NORMAL" to 2,
					"MODERATE" to 2
				)),
				AIShipConfiguration.AIWorldSettings(world = "Sirius", rolls = 10, tiers = mapOf(
					"EASY" to 2,
					"NORMAL" to 2,
					"MODERATE" to 2
				)),
				AIShipConfiguration.AIWorldSettings(world = "Regulus", rolls = 15, tiers = mapOf(
					"NORMAL" to 2,
					"MODERATE" to 2,
					"ADVANCED" to 2
				)),
				AIShipConfiguration.AIWorldSettings(world = "Ilios", rolls = 5, tiers = mapOf(
					"NORMAL" to 2,
					"MODERATE" to 2,
					"ADVANCED" to 2
				)),
				AIShipConfiguration.AIWorldSettings(world = "Horizon", rolls = 5, tiers = mapOf(
					"MODERATE" to 4,
					"ADVANCED" to 4,
					"EXPERT" to 2
				)),
				AIShipConfiguration.AIWorldSettings(world = "Trench", rolls = 2, tiers = mapOf(
					"MODERATE" to 2,
					"ADVANCED" to 2,
					"EXPERT" to 2
				)),
				AIShipConfiguration.AIWorldSettings(world = "AU-0821", rolls = 2, tiers = mapOf(
					"MODERATE" to 2,
					"ADVANCED" to 4,
					"EXPERT" to 4
				))
			)
		)
	}
}
