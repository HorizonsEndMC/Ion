package net.starlegacy.util

import com.sk89q.worldedit.world.block.BlockState as WorldEditBlockState
import net.minecraft.world.level.block.state.BlockState as MinecraftBlockState
import com.sk89q.worldedit.EditSession
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.function.operation.Operation
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.session.ClipboardHolder
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import net.starlegacy.util.blockplacement.BlockPlacement
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.block.data.BlockData

fun readSchematic(file: File): Clipboard? {
	val format = ClipboardFormats.findByFile(file) ?: return null

	FileInputStream(file).use { inputStream ->
		return format.getReader(inputStream).read()
	}
}

fun writeSchematic(clipboard: Clipboard, file: File) {
	val format = BuiltInClipboardFormat.SPONGE_SCHEMATIC
	FileOutputStream(file).use { outputStream ->
		return format.getWriter(outputStream).write(clipboard)
	}
}

fun World.worldEditSession(fastMode: Boolean = true, block: (EditSession) -> Unit) {
	WorldEdit.getInstance().editSessionFactory.getEditSession(BukkitAdapter.adapt(this), -1)
		.also {
			it.setFastMode(fastMode)
		}.use(block)
}

fun Clipboard.paste(world: World, x: Int, y: Int, z: Int, ignoreAir: Boolean = false) {
	world.worldEditSession { sess ->
		val operation: Operation = ClipboardHolder(this).createPaste(sess)
			.to(BlockVector3.at(x, y, z))
			.ignoreAirBlocks(ignoreAir)
			.build()
		Operations.complete(operation)
	}
}

fun placeSchematicEfficiently(
	schematic: Clipboard,
	world: World,
	target: Vec3i,
	ignoreAir: Boolean,
	callback: () -> Unit = {}
) {
	Tasks.async {
		val queue = Long2ObjectOpenHashMap<MinecraftBlockState>()
		val region = schematic.region.clone()
		val targetBlockVector = BlockVector3.at(target.x, target.y, target.z)
		val offset = targetBlockVector.subtract(schematic.origin)

		region.shift(offset)

		for (vector in region) {
			val baseBlock = schematic.getFullBlock(vector.subtract(offset))
			val blockData = baseBlock.toImmutableState().toBukkitBlockData()
			if (blockData.material.isAir) {
				continue
			}
			val blockKey = blockKey(vector.x, vector.y, vector.z)
			queue[blockKey] = blockData.nms
		}

		BlockPlacement.placeQueueEfficiently(world, queue) { world ->
			schematic.paste(world, target.x, target.y, target.z, ignoreAir)
			callback()
		}
	}
}

private val blockDataCache = mutableMapOf<String, BlockData>()

fun WorldEditBlockState.toBukkitBlockData(): BlockData {
	val string = this.asString
	return blockDataCache.getOrPut(string) {
		Bukkit.createBlockData(string)
	}
}
