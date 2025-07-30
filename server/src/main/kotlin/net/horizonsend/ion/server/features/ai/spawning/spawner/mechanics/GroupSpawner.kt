package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.ai.util.SpawnMessage
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.World
import java.util.function.Supplier

/**
 * Will always spawn the list of provided ships
 **/
class GroupSpawner(
	locationProvider: Supplier<Location?>,
	private val ships: MutableList<SpawnedShip>,
	groupMessage: Component?,
	individualSpawnMessage: SpawnMessage?,
	difficultySupplier: (World) -> Supplier<Int>,
) : MultiSpawner(
	locationProvider,
	groupMessage,
	individualSpawnMessage,
	difficultySupplier,
	{ AITarget.TargetMode.PLAYER_ONLY },
	{ null }
) {
	override fun getShips(): List<SpawnedShip> {
		return ships
	}

	override fun getAvailableShips(draw: Boolean): Collection<SpawnedShip> {
		return ships
	}
}
