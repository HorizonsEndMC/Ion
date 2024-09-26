package net.horizonsend.ion.server.features.transport

import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.block.BlockPlaceEvent

object GlobalNodeManager : SLEventListener() {
	@EventHandler
	fun onBlockBreak(event: BlockBreakEvent) {
		val world = event.block.world
		val key = toBlockKey(event.block.x, event.block.y, event.block.z)

		handleBlockRemoval(world, key)
	}

	@EventHandler
	fun onBlockPlace(event: BlockPlaceEvent) {
		handleBlockChange(event.block)
	}

	@EventHandler
	fun onPistonExtend(event: BlockPistonExtendEvent) {
		// Delay 1 tick
		Tasks.sync { event.blocks.forEach {
			handleBlockRemoval(it.world, toBlockKey(it.x, it.y, it.z))
			handleBlockChange(it)
		}}
	}

	@EventHandler
	fun onPistonRetract(event: BlockPistonRetractEvent) {
		// Delay 1 tick
		Tasks.sync { event.blocks.forEach {
			handleBlockRemoval(it.world, toBlockKey(it.x, it.y, it.z))
			handleBlockChange(it)
		}}
	}

	fun handleBlockChange(new: Block) {
		val chunkX = new.x.shr(4)
		val chunkZ = new.z.shr(4)

		val chunk = new.world.ion.getChunk(chunkX, chunkZ) ?: return
		chunk.transportNetwork.processBlockChange(new)
	}

	fun handleBlockChange(world: World, position: BlockKey, data: BlockData) {
		val chunkX = getX(position).shr(4)
		val chunkZ = getZ(position).shr(4)

		val chunk = world.ion.getChunk(chunkX, chunkZ) ?: return
		chunk.transportNetwork.processBlockChange(position, data)
	}

	fun handleBlockChanges(world: World, changeMap: Map<BlockKey, BlockData>) {
		val byChunk = changeMap.entries.groupBy {
			Chunk.getChunkKey(getX(it.key), getZ(it.key))
		}

		for ((chunkKey, changes) in byChunk) {
			val chunk = IonChunk[world, chunkKey] ?: continue

			for ((position, data) in changes) {
				chunk.transportNetwork.processBlockChange(position, data)
			}
		}
	}

	fun refreshBlock(world: World, position: BlockKey) {
		val chunkX = getX(position).shr(4)
		val chunkZ = getZ(position).shr(4)

		val chunk = world.ion.getChunk(chunkX, chunkZ) ?: return
		chunk.transportNetwork.refreshBlock(position)
	}

	fun handleBlockAdditions(newBlocks: Iterable<Block>) {
		for (new in newBlocks) {
			handleBlockChange(new)
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
