package net.horizonsend.ion.server.features.ai.spawning

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.World

/** An exception relating to a cause of a failed spawn. */
class SpawningException(
    message: String,
    val world: World,
    val spawningLocation: Vec3i?,
): Throwable(message) {
    /** The locations of any placed blocks. Will be empty if the error occured before any were placed. */
    var blockLocations: LongOpenHashSet = LongOpenHashSet()
}
