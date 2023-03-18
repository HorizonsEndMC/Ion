package net.starlegacy.util

import com.sk89q.jnbt.CompoundTag
import com.sk89q.jnbt.NBTUtils
import com.sk89q.jnbt.Tag
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
import net.minecraft.nbt.ByteArrayTag
import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.DoubleTag
import net.minecraft.nbt.EndTag
import net.minecraft.nbt.FloatTag
import net.minecraft.nbt.IntArrayTag
import net.minecraft.nbt.IntTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.LongArrayTag
import net.minecraft.nbt.LongTag
import net.minecraft.nbt.ShortTag
import net.minecraft.nbt.StringTag
import net.starlegacy.util.blockplacement.BlockPlacement
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.block.data.BlockData
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import com.sk89q.worldedit.world.block.BlockState as WorldEditBlockState
import net.minecraft.world.level.block.state.BlockState as MinecraftBlockState

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

fun CompoundTag.nms(): net.minecraft.nbt.CompoundTag {
	val base = net.minecraft.nbt.CompoundTag()

	for ((key, tag) in this.value) {
		val nmsTag = tag.nms()

		base.put(key, nmsTag)
	}

	return base
}

fun com.sk89q.jnbt.ListTag.nms(): ListTag {
	val base = ListTag()

	for (tag in this.value) {
		val nmsTag = tag.nms()

		base.add(nmsTag)
	}

	return base
}

fun Tag.nms(): net.minecraft.nbt.Tag {
	return when (NBTUtils.getTypeCode(this.javaClass)) {
		0 -> EndTag.INSTANCE
		1 -> ByteTag.valueOf((this as com.sk89q.jnbt.ByteTag).value)
		2 -> ShortTag.valueOf((this as com.sk89q.jnbt.ShortTag).value)
		3 -> IntTag.valueOf((this as com.sk89q.jnbt.IntTag).value)
		4 -> LongTag.valueOf((this as com.sk89q.jnbt.LongTag).value)
		5 -> FloatTag.valueOf((this as com.sk89q.jnbt.FloatTag).value)
		6 -> DoubleTag.valueOf((this as com.sk89q.jnbt.DoubleTag).value)
		7 -> ByteArrayTag((this as com.sk89q.jnbt.ByteArrayTag).value)
		8 -> StringTag.valueOf((this as com.sk89q.jnbt.StringTag).value)
		9 -> (this as com.sk89q.jnbt.ListTag).nms()
		10 -> (this as CompoundTag).nms()
		11 -> IntArrayTag((this as com.sk89q.jnbt.IntArrayTag).value)
		12 -> LongArrayTag((this as com.sk89q.jnbt.LongArrayTag).value)
		else -> throw IllegalArgumentException()
	}
}
