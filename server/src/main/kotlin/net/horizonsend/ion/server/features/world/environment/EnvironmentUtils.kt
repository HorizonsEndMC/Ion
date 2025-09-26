package net.horizonsend.ion.server.features.world.environment

import net.horizonsend.ion.common.utils.miscellaneous.d
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player

fun isWearingSpaceSuit(player: Player): Boolean {
	val inventory = player.inventory

	return inventory.helmet?.type == Material.CHAINMAIL_HELMET &&
		inventory.chestplate?.type == Material.CHAINMAIL_CHESTPLATE &&
		inventory.leggings?.type == Material.CHAINMAIL_LEGGINGS &&
		inventory.boots?.type == Material.CHAINMAIL_BOOTS
}

private val directionArray = arrayOf(
	BlockFace.EAST,
	BlockFace.WEST,
	BlockFace.SOUTH,
	BlockFace.NORTH,
	BlockFace.UP,
	BlockFace.DOWN
)

fun isInside(location: Location, extraChecks: Int): Boolean {
	fun getRelative(location: Location, direction: BlockFace, i: Int): Block? {
		val x = (direction.modX * i).d()
		val y = (direction.modY * i).d()
		val z = (direction.modZ * i).d()
		val newLocation = location.clone().add(x, y, z)

		return when {
			location.world.isChunkLoaded(newLocation.blockX shr 4, newLocation.blockZ shr 4) -> newLocation.block
			else -> null
		}
	}

	if (location.isChunkLoaded && !location.block.type.isAir) {
		return true
	}

	val airBlocks = HashSet<Block>()

	quickLoop@
	for (direction in directionArray) {
		if (direction.oppositeFace == direction) {
			continue
		}

		var block: Block?

		for (i in 1..189) {
			block = getRelative(location, direction, i)

			if (block == null) {
				continue@quickLoop
			}

			if (block.type != Material.AIR) {
				val relative = getRelative(location, direction, i - 1)

				if (relative != null) {
					airBlocks.add(relative)
				}

				continue@quickLoop
			}
		}
		return false
	}

	var check = 0

	while (check < extraChecks && airBlocks.isNotEmpty()) {
		edgeLoop@ for (airBlock in airBlocks.toList()) {
			for (direction in directionArray) {
				if (direction.oppositeFace == direction) {
					continue
				}

				var block: Block?

				for (i in 0..189) {
					block = getRelative(airBlock.location, direction, i)

					if (block == null) {
						break
					}

					if (block.type != Material.AIR) {
						if (i != 0) {
							airBlocks.add(airBlock.getRelative(direction, i))
						}

						airBlocks.remove(airBlock)
						continue@edgeLoop
					}
				}

				return false
			}
		}
		check++
	}

	return true
}
