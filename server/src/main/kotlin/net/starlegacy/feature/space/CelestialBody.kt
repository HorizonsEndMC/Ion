package net.starlegacy.feature.space

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.starlegacy.util.Vec3i
import net.starlegacy.util.blockplacement.BlockPlacement.placeImmediate
import net.starlegacy.util.blockplacement.BlockPlacement.queue
import org.bukkit.Bukkit
import org.bukkit.World

abstract class CelestialBody(spaceWorldName: String, location: Vec3i) {
	val spaceWorld: World? get() = Bukkit.getWorld(spaceWorldName)

	var spaceWorldName = spaceWorldName; private set
	var location = location; private set

	protected abstract fun createStructure(): Map<Vec3i, BlockState>

	private fun airQueue(structure: Map<Vec3i, BlockState>): Long2ObjectOpenHashMap<BlockState> {
		val blocks = Long2ObjectOpenHashMap<BlockState>(structure.size * 2)

		val air = Blocks.AIR.defaultBlockState()

		for ((x, y, z) in structure.keys) {
			blocks[BlockPos.asLong(x + location.x, y + location.y, z + location.z)] = air
		}
		return blocks
	}

	fun erase() {
		val spaceWorld = this.spaceWorld ?: return
		val blocks = airQueue(createStructure())
		placeImmediate(spaceWorld, blocks)
	}

	fun generate() {
		val spaceWorld = this.spaceWorld ?: return

		val structure = createStructure()

		placeImmediate(
			spaceWorld,
			structure.mapKeysTo(Long2ObjectOpenHashMap(structure.size)) { (intTrio, _) ->
				BlockPos.asLong(intTrio.x + location.x, intTrio.y + location.y, intTrio.z + location.z)
			}
		)
	}

	fun move(newLoc: Vec3i, urgent: Boolean = false) {
		val spaceWorld = this.spaceWorld ?: return

		val structure = createStructure()

		val blocks = airQueue(structure)

		structure.mapKeysTo(blocks) { (intTrio, _) ->
			BlockPos.asLong(intTrio.x + newLoc.x, intTrio.y + newLoc.y, intTrio.z + newLoc.z)
		}

		queue(spaceWorld, blocks)

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
