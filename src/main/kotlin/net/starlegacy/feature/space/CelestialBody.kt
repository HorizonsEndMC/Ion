package net.starlegacy.feature.space

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.starlegacy.util.NMSBlockState
import net.starlegacy.util.NMSBlocks
import net.starlegacy.util.Vec3i
import net.starlegacy.util.blockKey
import net.starlegacy.util.blockplacement.BlockPlacement
import org.bukkit.Bukkit
import org.bukkit.World

abstract class CelestialBody(spaceWorldName: String, location: Vec3i) {
	val spaceWorld: World? get() = Bukkit.getWorld(spaceWorldName)

	var spaceWorldName = spaceWorldName; private set
	var location = location; private set

	protected abstract fun createStructure(): Map<Vec3i, NMSBlockState>

	private fun airQueue(structure: Map<Vec3i, NMSBlockState>): Long2ObjectOpenHashMap<NMSBlockState> {
		val blocks = Long2ObjectOpenHashMap<NMSBlockState>(structure.size * 2)

		val air = NMSBlocks.AIR.defaultBlockState()

		for ((x, y, z) in structure.keys) {
			blocks[blockKey(x + location.x, y + location.y, z + location.z)] = air
		}
		return blocks
	}

	fun erase() {
		val spaceWorld = this.spaceWorld ?: return
		val blocks = airQueue(createStructure())
		BlockPlacement.queue(spaceWorld, blocks)
	}

	fun generate() {
		val spaceWorld = this.spaceWorld ?: return

		val structure = createStructure()

		BlockPlacement.queue(spaceWorld, structure.mapKeysTo(Long2ObjectOpenHashMap(structure.size)) { (intTrio, _) ->
			blockKey(intTrio.x + location.x, intTrio.y + location.y, intTrio.z + location.z)
		})
	}

	fun move(newLoc: Vec3i) {
		val spaceWorld = this.spaceWorld ?: return

		val structure = createStructure()

		val blocks = airQueue(structure)

		structure.mapKeysTo(blocks) { (intTrio, _) ->
			blockKey(intTrio.x + newLoc.x, intTrio.y + newLoc.y, intTrio.z + newLoc.z)
		}

		BlockPlacement.queue(spaceWorld, blocks)

		blocks.clear()

		location = newLoc
	}

	fun move(newLoc: Vec3i, newSpaceWorld: World) {
		if (newSpaceWorld == spaceWorld) {
			move(newLoc)
			return
		}

		erase()

		spaceWorldName = newSpaceWorld.name
		location = newLoc

		generate()
	}
}
