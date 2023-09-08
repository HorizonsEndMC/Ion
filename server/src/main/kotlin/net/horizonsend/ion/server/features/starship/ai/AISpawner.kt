package net.horizonsend.ion.server.features.starship.ai

import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import org.bukkit.Location
import org.bukkit.World
import kotlin.random.Random

abstract class AISpawner(vararg val ships: AIStarshipTemplates.AIStarshipTemplate = AIStarshipTemplates.templates.toTypedArray()) {
	abstract fun findLocation(world: World): Location

	open fun spawn(location: Location): ActiveControlledStarship {
		val ship = ships.randomOrNull() ?: throw NoSuchElementException()

		val schematic = AIStarshipTemplates.loadedSchematics.getOrPut(ship.schematicFile) { ship.schematic() }
		val type = ship.type
		val name = ship.miniMessageName
		val createController = ship.createController

		lateinit var activeStarship: ActiveControlledStarship

		AIUtils.createFromClipboard(location, schematic, type, name, createController) {
			activeStarship = it
		}

		return activeStarship
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
