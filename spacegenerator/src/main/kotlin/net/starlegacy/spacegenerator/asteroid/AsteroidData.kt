package net.starlegacy.spacegenerator.asteroid

import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.world.block.BlockState
import net.starlegacy.util.Vec3i
import net.starlegacy.util.readSchematic
import net.starlegacy.util.toBukkitBlockData
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import java.io.File
import java.util.EnumSet
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.math.max
import kotlin.math.min

object AsteroidData {
    internal lateinit var cachedAsteroids: Map<String, Lazy<CachedAsteroid>>

    @JvmField
    val ORE_REPLACE_TYPES: EnumSet<Material> = EnumSet.of(
        Material.STONE,
        Material.GRANITE,
        Material.POLISHED_GRANITE,
        Material.DIORITE,
        Material.POLISHED_DIORITE,
        Material.ANDESITE,
        Material.POLISHED_ANDESITE,
        Material.COBBLESTONE,
        Material.QUARTZ_BLOCK,
        Material.SAND,
        Material.RED_SAND,
        Material.SOUL_SAND,
        Material.RED_SANDSTONE,
        Material.ICE,
        Material.PACKED_ICE,
        Material.RED_NETHER_BRICKS,
        Material.PURPUR_BLOCK,
        Material.PURPUR_PILLAR,
        Material.END_STONE
    )

    internal fun loadAsteroids(folder: File) {
        val asteroids: MutableMap<String, Lazy<CachedAsteroid>> = mutableMapOf()

        folder.mkdirs()

        for (file: File in folder.listFiles()) {
            if (file.extension != "schematic") {
                continue
            }

            val asteroidName = file.nameWithoutExtension

            check(file.exists())

            addClipboard(file, asteroids, asteroidName)

            println("loaded asteroid $asteroidName")
        }

        cachedAsteroids = asteroids
    }

    private fun addClipboard(file: File, asteroids: MutableMap<String, Lazy<CachedAsteroid>>, asteroidName: String) {
        asteroids[asteroidName] = lazy { loadAsteroid(file) }
    }

    private fun loadAsteroid(file: File): CachedAsteroid {
        val clipboard: Clipboard = readSchematic(file) ?: error("Invalid schematic file ${file.path}")

        val blocks: MutableMap<Vec3i, BlockData> = mutableMapOf()

        for (vector: BlockVector3 in clipboard.region) {
            val block: BlockState = clipboard.getBlock(vector)

            val coordinates = Vec3i(vector.blockX, vector.blockY, vector.blockZ)
            val blockData = block.toBukkitBlockData()

            // some schematics use red wool for selection points
            if (blockData.material == Material.RED_WOOL) {
                continue
            }

            if (blockData.material.isAir) {
                continue
            }

            blocks[coordinates] = blockData
        }

        val first = blocks.keys.first()
        var minX = first.x
        var minY = first.y
        var minZ = first.z
        var maxX = first.x
        var maxY = first.y
        var maxZ = first.z

        for ((x, y, z) in blocks.keys) {
            minX = min(x, minX)
            minY = min(y, minY)
            minZ = min(z, minZ)
            maxX = max(x, maxX)
            maxY = max(y, maxY)
            maxZ = max(z, maxZ)
        }

        val width: Int = maxX - minX
        val height: Int = maxY - minY
        val length: Int = maxZ - minZ

        // ensures that coordinates are relative to min x/y/z
        val adjustedBlocks = blocks.mapKeys { (key, _) ->
            Vec3i(key.x - minX, key.y - minY, key.z - minZ)
        }

        return CachedAsteroid(adjustedBlocks, width, height, length)
    }
}
