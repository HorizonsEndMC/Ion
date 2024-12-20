package net.horizonsend.ion.server.features.transport.util

import com.fastasyncworldedit.core.history.changeset.AbstractChangeSet
import com.fastasyncworldedit.core.history.changeset.ChangeExchangeCoordinator
import com.fastasyncworldedit.core.nbt.FaweCompoundTag
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.inventory.BlockBag
import com.sk89q.worldedit.history.change.Change
import com.sk89q.worldedit.world.World
import com.sk89q.worldedit.world.biome.BiomeType
import com.sk89q.worldedit.world.block.BlockTypesCache
import net.horizonsend.ion.server.features.multiblock.MultiblockEntities
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.isWallSign
import org.bukkit.block.Sign
import java.util.Collections

class IonChangeSet(world: World) : AbstractChangeSet(world) {
	private var counter: Int = 0
	private val bukkitWorld = BukkitAdapter.adapt(world)

	override fun add(x: Int, y: Int, z: Int, combinedFrom: Int, combinedTo: Int) {
		counter++

		addWriteTask {
			NewTransport.invalidateCache(bukkitWorld, x, y, z)
			val type = BukkitAdapter.adapt(BlockTypesCache.states[combinedTo].blockType)

			if (type.isWallSign) processMultiblock(x, y, z)
			if (type.isAir) removeMultiblock(x, y, z)
		}
	}

	override fun addTileCreate(tag: FaweCompoundTag?) {}
	override fun addTileRemove(tag: FaweCompoundTag?) {}
	override fun addEntityRemove(tag: FaweCompoundTag?) {}
	override fun addEntityCreate(tag: FaweCompoundTag?) {}

	private fun processMultiblock(x: Int, y: Int, z: Int) {
		Tasks.sync {
			val state = bukkitWorld.getBlockState(x, y, z) as? Sign ?: return@sync
			MultiblockEntities.loadFromSign(state)
		}
	}

	private fun removeMultiblock(x: Int, y: Int, z: Int) {
		MultiblockEntities.removeMultiblockEntity(bukkitWorld, x, y, z)
	}

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

	override fun addBiomeChange(x: Int, y: Int, z: Int, from: BiomeType?, to: BiomeType?) {}
	override fun getCoordinatedChanges(
		blockBag: BlockBag?,
		mode: Int,
		dir: Boolean,
	): ChangeExchangeCoordinator? {
		TODO("Not yet implemented")
	}

	override fun getIterator(redo: Boolean): MutableIterator<Change> {
		return Collections.emptyIterator()
	}
}
