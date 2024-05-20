package net.horizonsend.ion.server.features.ores.storage

import com.manya.pdc.base.array.StringArrayDataType
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.nio.charset.Charset

/**
 * Represents stored chunk ores
 *
 * @param dataVersion The data version of the chunk
 * @param positions An array of positions, stored as longs
 * @param indexes A corresponding array of indexes pointing to values in the ores array
 * @param ores An array of Ores that appear in the chunk
 *
 * An ore can be accessed by getting its position from positions, and its ore from indexes and ores
 **/
class OreData(
	val dataVersion: Int,
	val positions: LongArray,
	val indexes: ByteArray,
	val ores: Array<Ore>
) {
	companion object : PersistentDataType<PersistentDataContainer, OreData> {
		override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java
		override fun getComplexType(): Class<OreData> = OreData::class.java

		val stringType = StringArrayDataType(Charset.defaultCharset())

		override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): OreData {
			val oresRaw = primitive.getOrDefault(NamespacedKeys.ORE_DATA, stringType, arrayOf())
			val ores = Array(oresRaw.size) { Ore.valueOf(oresRaw[it]) }

			return OreData(
				dataVersion = primitive.getOrDefault(NamespacedKeys.DATA_VERSION, PersistentDataType.INTEGER, 0),
				positions = primitive.getOrDefault(NamespacedKeys.ORE_POSITIONS, PersistentDataType.LONG_ARRAY, longArrayOf()),
				indexes = primitive.getOrDefault(NamespacedKeys.ORE_INDEXES, PersistentDataType.BYTE_ARRAY, byteArrayOf()),
				ores = ores
			)
		}

		override fun toPrimitive(complex: OreData, context: PersistentDataAdapterContext): PersistentDataContainer {
			val pdc = context.newPersistentDataContainer()

			pdc.set(NamespacedKeys.DATA_VERSION, PersistentDataType.INTEGER, complex.dataVersion)
			pdc.set(NamespacedKeys.ORE_POSITIONS, PersistentDataType.LONG_ARRAY, complex.positions)
			pdc.set(NamespacedKeys.ORE_INDEXES, PersistentDataType.BYTE_ARRAY, complex.indexes)
			pdc.set(NamespacedKeys.ORE_INDEXES, stringType, Array(complex.ores.size) { complex.ores[it].name })

			return pdc
		}
	}
}
