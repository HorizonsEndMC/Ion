package net.horizonsend.ion.server.miscellaneous.utils

import kotlinx.coroutines.CoroutineScope
import org.bukkit.World
import org.bukkit.block.Block

//TODO async block requests, updates, etc

fun CoroutineScope.getBlock(world: World, x: Int, y: Int, z: Int): Block? {
	return Tasks.getSyncBlocking { world.getBlockAt(x, y, z) } // TODO
}
