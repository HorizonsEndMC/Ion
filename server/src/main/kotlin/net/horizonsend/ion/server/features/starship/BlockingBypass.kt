package net.horizonsend.ion.server.features.starship

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.miscellaneous.utils.blockKey
import net.horizonsend.ion.server.miscellaneous.utils.blockKeyX
import net.horizonsend.ion.server.miscellaneous.utils.blockKeyY
import net.horizonsend.ion.server.miscellaneous.utils.blockKeyZ
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.data.BlockData
import java.util.Stack
import java.util.concurrent.TimeUnit

object BlockingBypass {
    private const val MAX_OBJECT_SIZE_TO_EXPLODE = 20

    fun objectIsSmallEnough(starship: ActiveStarship, blockKey: Long, world: World): Boolean {
        // Copied from StarshipDetection.detectNewState()

        // blocks that were accepted
        val blockTypes = Long2ObjectOpenHashMap<BlockData>()

        // blocks that are pending checking
        val queue = Stack<Long>()

        // blocks that were already checked and should not be detected twice
        val visited = mutableSetOf<Long>()

        // Jumpstart the queue by adding the origin block
        val originKey = blockKey
        visited.add(originKey)
        queue.push(originKey)

        val start = System.nanoTime()

        while (!queue.isEmpty()) {
            if (System.nanoTime() - start > TimeUnit.SECONDS.toNanos(30)) {
                // Took too long; assume that the object is too large
                return false
            }

            if (blockTypes.count() > MAX_OBJECT_SIZE_TO_EXPLODE) {
                return false
            }

            val key = queue.pop()
            val x = blockKeyX(key)
            val y = blockKeyY(key)
            val z = blockKeyZ(key)

            val blockPartOfStarship = ActiveStarships.findByBlock(Location(world, x.toDouble(), y.toDouble(), z.toDouble()))
            if (blockPartOfStarship == starship) {
                // Skip check only if the checked block is part of the checking starship
                continue
            } else if (blockPartOfStarship != null) {
                // Starship that is not part of the checking starship should always block
                return false
            }

            // Do not allow checking ships larger than render distance.
            // The type being null usually means the chunk is unloaded.
            val blockData = getBlockDataSafe(world, x, y, z) ?: return false

            val material = blockData.material

            if (material == Material.AIR || material == Material.VOID_AIR || material == Material.CAVE_AIR) {
                continue
            }

            // Add the location to the list of blocks that'll be set on the starships
            blockTypes[key] = blockData

            // Detect adjacent blocks

            for (offsetX in -1..1) {
                for (offsetY in -1..1) {
                    for (offsetZ in -1..1) {
                        val adjacentX = offsetX + x
                        val adjacentY = offsetY + y
                        val adjacentZ = offsetZ + z

                        // Ensure it's a valid Y-level before adding it to the queue
                        if (adjacentY < 0 || adjacentY > world.maxHeight) {
                            continue
                        }

                        val key1 = blockKey(adjacentX, adjacentY, adjacentZ)
                        // Ignore active starships (prevents self-detection)
                        if (visited.add(key1)) {
                            queue.push(key1)
                        }
                    }
                }
            }
        }

        // If the while loop completed, the object is small enough
        return true
    }
}