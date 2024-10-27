package net.horizonsend.ion.server.features.world

import kotlinx.serialization.Serializable
import net.horizonsend.ion.common.utils.miscellaneous.testRandom
import net.horizonsend.ion.server.configuration.IntegerAmount
import net.horizonsend.ion.server.configuration.StaticIntegerAmount
import net.horizonsend.ion.server.features.gas.type.WorldGasConfiguration
import net.horizonsend.ion.server.features.world.environment.Environment
import org.bukkit.entity.EntityType
import java.util.function.Supplier

@Serializable
data class WorldSettings(
	val flags: MutableSet<WorldFlag> = mutableSetOf(),
	val environments: MutableSet<Environment> = mutableSetOf(),
	val gasConfiguration: WorldGasConfiguration = WorldGasConfiguration(),
	val customMobSpawns: List<SpawnedMob> = listOf(),
) {
	@Serializable
	data class SpawnedMob(
		val function: SpawnFunction,
		val spawningWeight: Double,
		val type: String,
		val namePool: Map<String, Double> = mapOf(),
		val onHand: DroppedItem? = null,
		val offHand: DroppedItem? = null,
		val helmet: DroppedItem? = null,
		val chestPlate: DroppedItem? = null,
		val leggings: DroppedItem? = null,
		val boots: DroppedItem? = null,
	) {
		fun getEntityType(): EntityType = EntityType.valueOf(type)

		@Serializable
		data class ChanceReplace(val chance: Float): SpawnFunction {
			override fun get(): Boolean = testRandom(chance)
		}

		@Serializable
		data object AlwaysReplace: SpawnFunction {
			override fun get(): Boolean = true
		}

		@Serializable
		sealed interface SpawnFunction: Supplier<Boolean>
	}

	/**
	 * Uses bazaar strings for now
	 * Not the end of the world, but could be improved upon
	 **/
	@Serializable
	data class DroppedItem(
		val itemString: String,
		val amount: IntegerAmount = StaticIntegerAmount(1),
		val dropChance: Float,
	)
}
