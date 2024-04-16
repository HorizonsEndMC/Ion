package net.horizonsend.ion.server.features.starship.destruction

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.event.StarshipExplodeEvent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.blockplacement.BlockPlacement
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Material

object StarshipDestruction {
	const val MAX_SAFE_HULL_INTEGRITY = 0.8

	/**
	 * 1984 the ship
	 *
	 * If urgent, all will be done on the main thread.
	 **/
	fun vanish(starship: ActiveStarship, urgent: Boolean = false) {
		if (starship.isExploding) {
			return
		}

		starship.isExploding = true

		if (urgent) {
			Tasks.syncBlocking {
				vanishShip(starship)
			}
			return
		}

		if (starship is ActiveControlledStarship) {
			DeactivatedPlayerStarships.deactivateAsync(starship) {
				DeactivatedPlayerStarships.destroyAsync(starship.data) {
					vanishShip(starship)
				}
			}
		} else {
			vanishShip(starship)
		}
	}

	private fun vanishShip(starship: ActiveStarship) {
		val air = Material.AIR.createBlockData().nms
		val queue = Long2ObjectOpenHashMap<BlockState>(starship.initialBlockCount)
		starship.blocks.associateWithTo(queue) { air }
		BlockPlacement.placeImmediate(starship.world, queue)
	}

	fun destroy(starship: ActiveStarship) {
		if (starship.isExploding) {
			return
		}
		if (!StarshipExplodeEvent(starship).callEvent()) {
			return
		}

		starship.isExploding = true

		if (starship is ActiveControlledStarship) {
			DeactivatedPlayerStarships.deactivateAsync(starship) {
				DeactivatedPlayerStarships.destroyAsync(starship.data) {
					destroyShip(starship)
				}
			}
		} else {
			destroyShip(starship)
		}
	}

	private fun destroyShip(starship: ActiveStarship) {
		starship.type.sinkProvider.getSinkProvider(starship).execute()
	}
}
