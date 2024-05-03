package net.horizonsend.ion.server.features.world

import net.horizonsend.ion.server.features.multiblock.ChunkMultiblockManager
import net.horizonsend.ion.server.features.transport.ChunkTransportManager
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.data.ChunkDataFixer
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.minecraft.world.level.chunk.LevelChunkSection
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.persistence.PersistentDataType.INTEGER

class IonChunk(val inner: Chunk) {
	val dataVersion = inner.persistentDataContainer.getOrDefault(NamespacedKeys.DATA_VERSION, INTEGER, 0)
	val locationKey = inner.chunkKey

	/** The origin X coordinate of this chunk (in real coordinates) **/
	val originX = inner.x.shl(4)
	/** The origin Z coordinate of this chunk (in real coordinates) **/
	val originZ = inner.z.shl(4)

	/** The X chunk coordinate **/
	val x = inner.x
	/** The Z chunk coordinate **/
	val z = inner.z

	val world get() = inner.world

	val sections: Iterable<IndexedValue<LevelChunkSection>> get() = inner.minecraft.sections.withIndex()

	// TODO
	//  - Wires
	//  - Ore upgrader
	//  - Explosion Reversal

	val multiblockManager = ChunkMultiblockManager(this)
	val transportNetwork: ChunkTransportManager = ChunkTransportManager(this)

	/**
	 * Logic upon loading the chunk
	 **/
	fun onLoad() {
		transportNetwork.setup()
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
		transportNetwork.save()
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

		@EventHandler
		fun onBlockBreak(event: BlockBreakEvent) {
			val ionChunk = event.block.chunk.ion()
			ionChunk.transportNetwork.processBlockRemoval(event)
		}

		@EventHandler
		fun onBlockBreak(event: BlockPlaceEvent) {
			val ionChunk = event.block.chunk.ion()
			ionChunk.transportNetwork.processBlockAddition(event)
		}

		/**
		 * Handles the creation, registration and loading of the chunk in the IonWorld
		 *
		 * It is imperative that every exception generated be handled
		 **/
		private fun registerChunk(chunk: Chunk): IonChunk {
			val ionWorld = chunk.world.ion

			val ionChunk = IonChunk(chunk)

			ionWorld.addChunk(ionChunk)

			ionChunk.onLoad()

			// Upgrade data after it has been loaded
			ChunkDataFixer.upgrade(ionChunk)

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

	override fun toString(): String {
		return "IonChunk[$x, $z]"
	}
}
