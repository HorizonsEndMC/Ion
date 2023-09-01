package net.horizonsend.ion.server.features.explosion.reversal

import kotlinx.serialization.Serializable
import org.bukkit.block.data.BlockData

/**
 *
 * @param explodedTime the time to start the regenerate timer, NOT necessarily the time that it exploded
 *
 **/
@Serializable
data class ExplodedBlockData(
	val x: Int,
	val y: Int,
	val z: Int,
	val explodedTime: Long,
	val blockData: BlockData,
	val tileData: ByteArray?
) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as ExplodedBlockData

		if (x != other.x) return false
		if (y != other.y) return false
		if (z != other.z) return false
		if (explodedTime != other.explodedTime) return false
		if (blockData != other.blockData) return false
		if (tileData != null) {
			if (other.tileData == null) return false
			if (!tileData.contentEquals(other.tileData)) return false
		} else if (other.tileData != null) return false

		return true
	}

	override fun hashCode(): Int {
		var result = x
		result = 31 * result + y
		result = 31 * result + z
		result = 31 * result + explodedTime.hashCode()
		result = 31 * result + blockData.hashCode()
		result = 31 * result + (tileData?.contentHashCode() ?: 0)
		return result
	}
}
