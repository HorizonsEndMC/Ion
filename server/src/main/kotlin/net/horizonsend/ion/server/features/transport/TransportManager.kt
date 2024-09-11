package net.horizonsend.ion.server.features.transport

import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot
import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot.Companion.snapshot
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent

object TransportManager : SLEventListener() {
	@EventHandler
	fun onBlockBreak(event: BlockBreakEvent) {
		val world = event.block.world
		val key = toBlockKey(event.block.x, event.block.y, event.block.z)

		handleBlockRemoval(world, key)
	}

	@EventHandler
	fun onBlockPlace(event: BlockPlaceEvent) {
		val world = event.block.world

		handleBlockAddition(world, event.block)
	}

	fun handleBlockAddition(world: World, new: Block) {
		val chunkX = new.x.shr(4)
		val chunkZ = new.z.shr(4)

		val chunk = world.ion.getChunk(chunkX, chunkZ) ?: return
		chunk.transportNetwork.processBlockAddition(new)
	}

	fun handleBlockAdditions(world: World, newBlocks: Iterable<Block>) {
		for (new in newBlocks) {
			handleBlockAddition(world, new)
		}
	}

	fun handleBlockRemoval(world: World, key: BlockKey) {
		val chunkX = getX(key).shr(4)
		val chunkZ = getZ(key).shr(4)

		val chunk = world.ion.getChunk(chunkX, chunkZ) ?: return

		chunk.transportNetwork.processBlockRemoval(key)
	}

	fun handleBlockRemovals(world: World, keys: Iterable<BlockKey>) {
		for (key in keys) {
			val chunkX = getX(key).shr(4)
			val chunkZ = getZ(key).shr(4)

			val chunk = world.ion.getChunk(chunkX, chunkZ) ?: return

			chunk.transportNetwork.processBlockRemoval(key)
		}
	}
}
