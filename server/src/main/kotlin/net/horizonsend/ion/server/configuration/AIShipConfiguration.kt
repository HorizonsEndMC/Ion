package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.miscellaneous.utils.WeightedRandomList
import org.bukkit.World

/** Registration and spawning parameters of AI ships **/
@Serializable
data class AIShipConfiguration(
	val spawnRate: Long = 20 * 60 * 15,
	val worldSettings: Map<String, AIWorldSettings> = mutableMapOf("world" to AIWorldSettings())
) {
	/**
	 * Each world has a number of rolls for selection when a ship spawns
	 *
	 * @param spawners contains a list of defined AI ship templates, and their number of rolls when this world is selected
	 *
	 * @see AISpawner
	 **/
	@Serializable
	data class AIWorldSettings(
		val rolls: Int = 1,
		val spawners: Map<String, Int> = mapOf("VESTA" to 1)
	) {
		fun templateWeightedList(): WeightedRandomList<String> = WeightedRandomList(spawners)
	}

	fun worldWeightedList(): WeightedRandomList<String> = WeightedRandomList(worldSettings.map { it.key to it.value.rolls }.toMap())

	fun getWorld(world: World): AIWorldSettings? = worldSettings[world.name]
}
