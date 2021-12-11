package net.starlegacy.feature.starship

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.util.Tasks
import net.starlegacy.util.Vec3i
import net.starlegacy.util.blockKey
import net.starlegacy.util.blockKeyX
import net.starlegacy.util.blockKeyY
import net.starlegacy.util.blockKeyZ
import net.starlegacy.util.chunkKey
import net.starlegacy.util.toBukkitBlockData
import org.bukkit.World
import org.bukkit.block.data.BlockData
import java.io.InputStream
import java.io.OutputStream
import kotlin.collections.Iterable
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.collections.mapTo
import kotlin.collections.plusAssign
import kotlin.collections.set

data class PlayerStarshipState(
    /** Set of chunks included in the saved ship */
    val coveredChunks: LongOpenHashSet,
    /** Map of location to material type id */
    val blockMap: Long2ObjectOpenHashMap<BlockData>,
    val minPoint: Vec3i,
    val maxPoint: Vec3i
) {
    companion object {
        fun createFromActiveShip(starship: ActiveStarship): PlayerStarshipState {
            val world = starship.world
            val blocks = starship.blocks
            return createFromBlocks(world, blocks)
        }

        fun createFromBlocks(world: World, blocks: Iterable<Long>): PlayerStarshipState {
            Tasks.checkMainThread()
            val coveredChunks = LongOpenHashSet()
            val blockMap = Long2ObjectOpenHashMap<BlockData>()

            var minX: Int? = null
            var minY: Int? = null
            var minZ: Int? = null

            var maxX: Int? = null
            var maxY: Int? = null
            var maxZ: Int? = null

            for (key in blocks) {
                val x = blockKeyX(key)
                val y = blockKeyY(key)
                val z = blockKeyZ(key)

                if (minX == null || minX > x) minX = x
                if (minY == null || minY > y) minY = y
                if (minZ == null || minZ > z) minZ = z
                if (maxX == null || maxX < x) maxX = x
                if (maxY == null || maxY < y) maxY = y
                if (maxZ == null || maxZ < z) maxZ = z

                val blockData = world.getBlockAt(x, y, z).blockData

                if (blockData.material.isAir) {
                    continue
                }

                coveredChunks += chunkKey(x shr 4, z shr 4)
                blockMap[blockKey(x, y, z)] = blockData
            }

            checkNotNull(minX)
            checkNotNull(minY)
            checkNotNull(minZ)
            checkNotNull(maxX)
            checkNotNull(maxY)
            checkNotNull(maxZ)

            return PlayerStarshipState(coveredChunks, blockMap, Vec3i(minX, minY, minZ), Vec3i(maxX, maxY, maxZ))
        }

        fun readFromStream(stream: InputStream): PlayerStarshipState {
            val clipboard = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getReader(stream).use { reader: ClipboardReader ->
                reader.read()
            }
            val min = Vec3i(clipboard.minimumPoint.x, clipboard.minimumPoint.y, clipboard.minimumPoint.z)
            val max = Vec3i(clipboard.maximumPoint.x, clipboard.maximumPoint.y, clipboard.maximumPoint.z)

            val blockMap = Long2ObjectOpenHashMap<BlockData>()

            for (vec: BlockVector3 in clipboard.region) {
                val blockData = clipboard.getBlock(vec).toBukkitBlockData()
                if (blockData.material.isAir) {
                    continue
                }
                val blockKey = blockKey(vec.x, vec.y, vec.z)
                blockMap[blockKey] = blockData
            }

            val chunks = clipboard.region.chunks
            val coveredChunks = chunks.mapTo(LongOpenHashSet(chunks.size)) { chunkKey(it.x, it.z) }
            return PlayerStarshipState(coveredChunks, blockMap, min, max)
        }
    }

    fun writeToStream(stream: OutputStream) {
        val min = BlockVector3.at(minPoint.x, minPoint.y, minPoint.z)
        val max = BlockVector3.at(maxPoint.x, maxPoint.y, maxPoint.z)
        val region = CuboidRegion(min, max)
        val clipboard = BlockArrayClipboard(region)

        for ((blockKey: Long, data: BlockData) in blockMap) {
            val vector = BlockVector3.at(blockKeyX(blockKey), blockKeyY(blockKey), blockKeyZ(blockKey))
            val adaptedData = BukkitAdapter.adapt(data)
            clipboard.setBlock(vector, adaptedData)
        }

        BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(stream).use { writer: ClipboardWriter ->
            writer.write(clipboard)
        }
    }
}
