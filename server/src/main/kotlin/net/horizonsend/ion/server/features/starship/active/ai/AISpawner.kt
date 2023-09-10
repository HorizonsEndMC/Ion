package net.horizonsend.ion.server.features.starship.active.ai

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import org.bukkit.Location
import org.bukkit.World
import kotlin.random.Random

abstract class AISpawner(vararg val ships: AIStarshipTemplates.AIStarshipTemplate) {
	abstract fun findLocation(world: World): Location

	open fun spawn(location: Location): Deferred<ActiveControlledStarship> {
		val ship = ships.randomOrNull() ?: throw NoSuchElementException()
		val deferred = CompletableDeferred<ActiveControlledStarship>()

		val schematic = AIStarshipTemplates.loadedSchematics.getOrPut(ship.schematicFile) { ship.schematic() }
		val type = ship.type
		val name = ship.miniMessageName
		val createController = ship.createController

        AIUtils.createFromClipboard(location, schematic, type, name, createController) {
            deferred.complete(it)
        }

		return deferred
	}
}

class VestaSpawner : AISpawner(AIStarshipTemplates.VESTA) {
	override fun findLocation(world: World): Location {
		val x = Random.nextDouble(-200.0, 200.0)
		val y = 128.0
		val z = Random.nextDouble(-200.0, 200.0)

		return Location(world, x, y, z)
	}
}
