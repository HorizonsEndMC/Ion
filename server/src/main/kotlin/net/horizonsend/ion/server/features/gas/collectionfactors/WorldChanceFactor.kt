package net.horizonsend.ion.server.features.gas.collectionfactors

import org.bukkit.Location
import kotlin.random.Random

class WorldChanceFactor(val worlds: Map<String, Double>) : CollectionFactor() {
    override fun factor(location: Location): Boolean {
		val sample = Random.nextFloat()

		return worlds.any { (world, chance) ->
			location.world.name.equals(world, ignoreCase = true) && sample >= chance
		}
    }

	override fun canBeFound(location: Location): Boolean = worlds.any {
		(world, _) -> location.world.name.equals(world, ignoreCase = true)
	}
}
