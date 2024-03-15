package net.horizonsend.ion.server.features.world

import net.horizonsend.ion.server.features.multiblock.ChunkMultiblockManager
import net.horizonsend.ion.server.features.transport.ChunkTransportNetwork
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent

class IonChunk(val inner: Chunk) {
	val locationKey = inner.chunkKey

	val originX = inner.x.shl(4)
	val originZ = inner.z.shl(4)

	val x = inner.x
	val z = inner.z

	// TODO
	//  - Wires
	//  - Ore upgrader
	//  - Explosion Reversal

	val multiblockManager = ChunkMultiblockManager(this)
	val transportNetwork: ChunkTransportNetwork = ChunkTransportNetwork(this)

	/**
	 * Logic upon loading the chunk
	 **/
	fun onLoad() {

	}

	/**
	 * Logic upon unloading the chunk
	 **/
	fun onUnload() {
		save()
	}

	/**
	 * Logic upon world save
	 **/
	fun save() {
		multiblockManager.save()
	}

	/**
	 * Logic upon world tick
	 **/
	fun tick() {
		transportNetwork.tick()
		multiblockManager.tick()
	}

	companion object : SLEventListener() {
		@EventHandler
		fun onChunkLoad(event: ChunkLoadEvent) {
			registerChunk(event.chunk)
		}

		@EventHandler
		fun onChunkUnload(event: ChunkUnloadEvent) {
			unregisterChunk(event.chunk)
		}

		/**
		 * Handles the creation, registration and loading of the chunk in the IonWorld
		 **/
		private fun registerChunk(chunk: Chunk): IonChunk {
			val ionWorld = chunk.world.ion

			val ionChunk = IonChunk(chunk)

			ionWorld.addChunk(ionChunk)

			ionChunk.onLoad()

			return ionChunk
		}

		/**
		 * Handles the unloading of the chunk
		 **/
		private fun unregisterChunk(chunk: Chunk) {
			val ionWorld = chunk.world.ion

			val removed = ionWorld.removeChunk(chunk) ?: return

			removed.onUnload()
		}

		/**
		 * Returns the chunk at the specified coordinates in the world if it is loaded
		 **/
		operator fun get(world: World, x: Int, z: Int): IonChunk? {
			return world.ion.getChunk(x, z)
		}

		fun Chunk.ion(): IonChunk = this.world.ion.getChunk(chunkKey)!!
	}
}
