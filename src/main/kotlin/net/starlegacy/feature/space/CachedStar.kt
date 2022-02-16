package net.starlegacy.feature.space

import net.starlegacy.database.Oid
import net.starlegacy.database.schema.space.Star
import net.starlegacy.util.CBMagicNumbers
import net.starlegacy.util.NMSBlockState
import net.starlegacy.util.Vec3i
import net.starlegacy.util.getSphereBlocks
import org.bukkit.Material

class CachedStar(
	val databaseId: Oid<Star>,
	override val name: String,
	spaceWorldName: String,
	location: Vec3i,
	private val material: Material,
	size: Double
) : CelestialBody(spaceWorldName, location),
	NamedCelestialBody {
	companion object {
		private const val MAX_SIZE = 100
	}

	init {
		require(size > 0 && size <= 1)
	}

	val sphereRadius = (MAX_SIZE * size).toInt()

	override fun createStructure(): Map<Vec3i, NMSBlockState> {
		val blockData = CBMagicNumbers.getBlock(material).defaultBlockState()

		return getSphereBlocks(sphereRadius).associateWith { blockData }
	}
}
