package net.horizonsend.ion.server.features.transport

import com.sk89q.worldedit.EditSession
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.AbstractDelegateExtent
import com.sk89q.worldedit.extent.Extent
import com.sk89q.worldedit.function.pattern.Pattern
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.Region
import com.sk89q.worldedit.world.block.BlockStateHolder
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.World

class IonUpdateExtent(extent: Extent, val world: World, private val stage: EditSession.Stage) : AbstractDelegateExtent(extent) {
	init {
	    println("Initializing ion update extent")
	}

	override fun <T : BlockStateHolder<T>?> setBlock(x: Int, y: Int, z: Int, block: T): Boolean {
		println("Setting block 1")
		println("Got extent: $stage")

		handleUpdate(BlockVector3.at(x, y, z), block)

		return extent.setBlock(x, y, z, block)
	}

	@Deprecated("")
	override fun <T : BlockStateHolder<T>?> setBlock(position: BlockVector3, block: T): Boolean {
		println("Setting block 2")

		handleUpdate(position, block)

		return extent.setBlock(position.x, position.y, position.z, block)
	}

	override fun <B : BlockStateHolder<B>?> setBlocks(region: Region, block: B): Int {

		println("Setting blocks 1")
		return extent.setBlocks(region, block)
	}

	override fun setBlocks(region: Region, pattern: Pattern): Int {
		println("Setting blocks 2")
		processPattern(region, pattern)

		return extent.setBlocks(region, pattern)
	}

	override fun setBlocks(vset: MutableSet<BlockVector3>?, pattern: Pattern?): Int {
		println("Setting blocks 3")
		return extent.setBlocks(vset, pattern)
	}

	private fun processPattern(region: Region, pattern: Pattern) {
		val newBlocks = region.clone().mapNotNull { position ->
			val newBlock = pattern.applyBlock(position) ?: return@mapNotNull null

			BukkitAdapter.adapt(newBlock)
		}

//		TransportManager.handleBlockAdditions(world, newBlocks)
	}

	private fun handleUpdate(position: BlockVector3, newBlock: BlockStateHolder<*>?) {
		if (newBlock == null) return

		val material = BukkitAdapter.adapt(newBlock.blockType)
		val blockData = BukkitAdapter.adapt(newBlock.toBaseBlock())

		val x = position.x
		val y = position.y
		val z = position.z

		TransportManager.handleBlockRemoval(world, toBlockKey(x, y, z))
//		TransportManager.handleBlockAddition(world, BlockSnapshot(world, x, y, z, material, blockData))
	}
}
