package net.horizonsend.ion.server.miscellaneous.utils

import com.fastasyncworldedit.core.FaweAPI
import com.sk89q.worldedit.EditSession
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.function.operation.Operation
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.Region
import com.sk89q.worldedit.session.ClipboardHolder
import com.sk89q.worldedit.world.block.BlockState as WorldEditBlockState
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.miscellaneous.utils.blockplacement.BlockPlacement
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKey
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
import net.minecraft.world.level.block.state.BlockState as MinecraftBlockState
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.enginehub.linbus.tree.LinByteArrayTag
import org.enginehub.linbus.tree.LinByteTag
import org.enginehub.linbus.tree.LinCompoundTag
import org.enginehub.linbus.tree.LinDoubleTag
import org.enginehub.linbus.tree.LinFloatTag
import org.enginehub.linbus.tree.LinIntArrayTag
import org.enginehub.linbus.tree.LinIntTag
import org.enginehub.linbus.tree.LinListTag
import org.enginehub.linbus.tree.LinLongArrayTag
import org.enginehub.linbus.tree.LinLongTag
import org.enginehub.linbus.tree.LinShortTag
import org.enginehub.linbus.tree.LinStringTag
import org.enginehub.linbus.tree.LinTag
import org.enginehub.linbus.tree.LinTagType
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL

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
    callback: (LongOpenHashSet) -> Unit = {}
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
			val blockKey = blockKey(vector.x(), vector.y(), vector.z())
			queue[blockKey] = blockData.nms
		}

		BlockPlacement.placeQueueEfficiently(world, queue) { world ->
			schematic.paste(world, target.x, target.y, target.z, ignoreAir)

			callback(LongOpenHashSet(queue.keys))
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

fun LinCompoundTag.nms(): net.minecraft.nbt.CompoundTag {
	val base = net.minecraft.nbt.CompoundTag()

	for (entry in this.value()) {
		base.put(entry.key, entry.value.nms())
	}

	return base
}

fun LinListTag<*>.nms(): ListTag {
	val base = ListTag()

	for (tag in this.value()) {
		val nmsTag = tag.nms()

		base.add(nmsTag)
	}

	return base
}

fun LinTag<*>.nms(): net.minecraft.nbt.Tag {
	return when (this.type()) {
		LinTagType.endTag() -> EndTag.INSTANCE
		LinTagType.byteTag() -> ByteTag.valueOf((this as LinByteTag).value())
		LinTagType.shortTag() -> ShortTag.valueOf((this as LinShortTag).value())
		LinTagType.intTag() -> IntTag.valueOf((this as LinIntTag).value())
		LinTagType.longTag() -> LongTag.valueOf((this as LinLongTag).value())
		LinTagType.floatTag() -> FloatTag.valueOf((this as LinFloatTag).value())
		LinTagType.doubleTag() -> DoubleTag.valueOf((this as LinDoubleTag).value())
		LinTagType.byteArrayTag() -> ByteArrayTag((this as LinByteArrayTag).value())
		LinTagType.stringTag() -> StringTag.valueOf((this as LinStringTag).value())
		LinTagType.listTag<LinListTag<*>>() -> this.nms()
		LinTagType.compoundTag() -> this.nms()
		LinTagType.intArrayTag() -> IntArrayTag((this as LinIntArrayTag).value())
		LinTagType.longArrayTag() -> LongArrayTag((this as LinLongArrayTag).value())
		else -> throw IllegalArgumentException()
	}
}

fun Player.getSelection(): Region? {
	val session = WorldEdit.getInstance().sessionManager.findByName(name) ?: return null
	return session.getSelection(session.selectionWorld)
}

/** Uploads the clipboard to the specified schematic upload site */
fun Clipboard.upload(): URL? {
	return try { FaweAPI.upload(this, BuiltInClipboardFormat.FAST) } catch (_: Throwable) { return null }
}

fun Clipboard.uploadAsync(callback: (URL?) -> Unit = {}) {
	Tasks.async {
		val url: URL? = this.upload()
		callback(url)
	}
}
