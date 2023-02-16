package net.horizonsend.ion.server.features.space.encounters

import net.minecraft.world.level.block.state.BlockState
import net.starlegacy.util.toNMSBlockData
import org.bukkit.Material

object Encounters {
	private val encounters: MutableMap<String, Encounter> = mutableMapOf()

	// TODO, test encounter. Will spawn enemies when you open the chest
	val ITS_A_TRAP = register(object : Encounter(
		identifier = "ITS_A_TRAP"
	) {
			override fun constructChestState(): BlockState {
				return Material.CHEST.toNMSBlockData()
			}

			override fun generate() {
				TODO("Not yet implemented")
			}
		}
	)

	private fun <T : Encounter> register(encounter: T): T {
		encounters[encounter.identifier] = encounter
		return encounter
	}

	fun getByIdentifier(identifier: String): Encounter? = encounters[identifier]
}

/**
 * A basic class controlling an encounter on a wreck.
 *
 * @property constructChestState Code used when generating the primary chest on the wreck.
 * 	This is executed when it places the chest block.
 * @property onChestInteract Code executed when the primary chest is interacted with.
 * @property generate Additional instructions executed when generating the wreck.
 **/
abstract class Encounter(
	val identifier: String
) {
	abstract fun constructChestState(): BlockState

	open fun onChestInteract() {}

	open fun generate() {}
}
