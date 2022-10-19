package net.starlegacy.feature.starship.movement

import io.papermc.paper.entity.RelativeTeleportFlag
import java.util.concurrent.CompletableFuture
import kotlin.math.max
import kotlin.math.min
import net.minecraft.world.level.block.state.BlockState
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.util.Tasks
import net.starlegacy.util.add
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent

class TranslateMovement(starship: ActiveStarship, val dx: Int, val dy: Int, val dz: Int, newWorld: World? = null) :
	StarshipMovement(starship, newWorld) {
	companion object {
		fun loadChunksAndMove(
			starship: ActiveStarship,
			dx: Int,
			dy: Int,
			dz: Int,
			newWorld: World? = null
		): CompletableFuture<Boolean> {
			val world = newWorld ?: starship.world

			val toLoad = this.getChunkLoadTasks(starship, world, dx, dz)

			return CompletableFuture.allOf(*toLoad.toTypedArray()).thenCompose {
				Tasks.checkMainThread()
				return@thenCompose starship.moveAsync(TranslateMovement(starship, dx, dy, dz, newWorld))
			}
		}

		private fun getChunkLoadTasks(
			starship: ActiveStarship,
			world: World,
			dx: Int,
			dz: Int
		): MutableSet<CompletableFuture<Chunk>> {
			val newMinChunkX = min(starship.min.x + dx, starship.max.x + dx) shr 4
			val newMaxChunkX = max(starship.min.x + dx, starship.max.x + dx) shr 4
			val cxRange = newMinChunkX..newMaxChunkX

			val newMinChunkZ = min(starship.min.z + dz, starship.max.z + dz) shr 4
			val newMaxChunkZ = max(starship.min.z + dz, starship.max.z + dz) shr 4
			val czRange = newMinChunkZ..newMaxChunkZ

			val toLoad = mutableSetOf<CompletableFuture<Chunk>>()

			for (cx in cxRange) {
				for (cz in czRange) {
					val chunkFuture = world.getChunkAtAsyncUrgently(cx, cz)
					toLoad.add(chunkFuture)
				}
			}

			return toLoad
		}
	}

	override fun blockDataTransform(blockData: BlockState): BlockState = blockData

	override fun displaceX(oldX: Int, oldZ: Int): Int = oldX + dx

	override fun displaceY(oldY: Int): Int = oldY + dy

	override fun displaceZ(oldZ: Int, oldX: Int): Int = oldZ + dz

	override fun displaceLocation(oldLocation: Location): Location {
		val newLocation = oldLocation.clone().add(dx, dy, dz)
		if (newWorld != null) {
			newLocation.world = newWorld
		}
		return newLocation
	}

	override fun movePassenger(passenger: Entity) {
		val location = passenger.location.clone()
		location.add(dx, dy, dz)
		if (newWorld != null) {
			location.world = newWorld
		}

		if (passenger is Player) {
			passenger.teleport(
				location,
				PlayerTeleportEvent.TeleportCause.PLUGIN,
				true,
				false,
				*RelativeTeleportFlag.values()
			)
		} else {
			passenger.teleport(location)
		}
	}

	override fun onComplete() {}
}
