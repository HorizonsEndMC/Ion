package net.horizonsend.ion.server.features.starship.destruction

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.event.StarshipExplodeEvent
import net.horizonsend.ion.server.features.starship.event.StarshipSunkEvent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.blockplacement.BlockPlacement
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Material
import java.util.function.Consumer

object StarshipDestruction {
	const val MAX_SAFE_HULL_INTEGRITY = 0.8

	/**
	 * 1984 the ship
	 *
	 * If urgent, all will be done on the main thread.
	 * @param ephemeral Whether to skip saving the deactivated ship data to the cache.
	 **/
	fun vanish(starship: Starship, ephemeral: Boolean = false, urgent: Boolean = false, successConsumer: Consumer<Boolean> = Consumer {}) {
		if (starship.isExploding) {
			successConsumer.accept(false)
			return
		}

		starship.isExploding = true

		starship.multiblockManager.clearData()
		starship.transportManager.clearData()

		if (urgent) {
			return Tasks.syncBlocking {
				DeactivatedPlayerStarships.deactivateNow(starship = starship, ephemeral = ephemeral)
				DeactivatedPlayerStarships.destroy(starship.data)

				vanishShip(starship, successConsumer)
			}
		}

		DeactivatedPlayerStarships.deactivateAsync(starship = starship, ephemeral = ephemeral) {
			DeactivatedPlayerStarships.destroyAsync(starship.data) {
				vanishShip(starship, successConsumer)
			}
		}
	}

	private fun vanishShip(starship: ActiveStarship, successConsumer: Consumer<Boolean> = Consumer {}) {
		val air = Material.AIR.createBlockData().nms
		val queue = Long2ObjectOpenHashMap<BlockState>(starship.initialBlockCount)
		starship.blocks.associateWithTo(queue) { air }
		BlockPlacement.placeImmediate(starship.world, queue) { successConsumer.accept(true) }
	}

	fun destroy(starship: ActiveStarship) {
		if (starship.isExploding) {
			return
		}

		if (!StarshipExplodeEvent(starship).callEvent()) {
			return
		}

		starship.isExploding = true

		starship.multiblockManager.clearData()
		starship.transportManager.clearData()

		val previousController = starship.controller

		DeactivatedPlayerStarships.deactivateAsync(starship) {
			DeactivatedPlayerStarships.destroyAsync(starship.data) {
				StarshipSunkEvent(starship, previousController).callEvent()
				destroyShip(starship)
			}
		}
	}

	private fun destroyShip(starship: ActiveStarship) {
		starship.type.sinkProvider.getSinkProvider(starship).execute()
	}
}
