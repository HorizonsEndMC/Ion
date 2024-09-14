package net.horizonsend.ion.server.features.transport

import com.fastasyncworldedit.core.history.changeset.AbstractChangeSet
import com.sk89q.jnbt.CompoundTag
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.history.change.Change
import com.sk89q.worldedit.world.World
import com.sk89q.worldedit.world.biome.BiomeType
import com.sk89q.worldedit.world.block.BlockTypesCache
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.block.data.BlockData
import java.util.Collections

class IonChangeSet(world: World) : AbstractChangeSet(world) {
	private var counter: Int = 0
	val bukkitWorld = BukkitAdapter.adapt(world)

	override fun add(x: Int, y: Int, z: Int, combinedFrom: Int, combinedTo: Int) {
		counter++
		addWriteTask {
			TransportManager.handleBlockChange(
				bukkitWorld,
				toBlockKey(x, y, z),
				BukkitAdapter.adapt(BlockTypesCache.states[combinedTo])
			)
		}
	}

	private val changes = mutableMapOf<BlockKey, BlockData>()

	private var recording: Boolean = true

	override fun isRecordingChanges(): Boolean {
		return recording
	}

	override fun setRecordChanges(recordChanges: Boolean) {
		recording = recordChanges
	}

	override fun size(): Int {
		return counter
	}

	override fun addTileCreate(tag: CompoundTag?) {}
	override fun addTileRemove(tag: CompoundTag?) {}
	override fun addEntityRemove(tag: CompoundTag?) {}
	override fun addEntityCreate(tag: CompoundTag?) {}
	override fun addBiomeChange(x: Int, y: Int, z: Int, from: BiomeType?, to: BiomeType?) {}
	override fun getIterator(redo: Boolean): MutableIterator<Change> {
		return Collections.emptyIterator()
	}
}
