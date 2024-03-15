package net.horizonsend.ion.server.features.transport

import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import java.util.concurrent.ConcurrentLinkedQueue

class ExtractorData(
	val extractorLocations: ConcurrentLinkedQueue<Vec3i>
) {
	companion object : PersistentDataType<LongArray, ExtractorData> {
		override fun getPrimitiveType(): Class<LongArray> = LongArray::class.java
		override fun getComplexType(): Class<ExtractorData> = ExtractorData::class.java

		override fun fromPrimitive(primitive: LongArray, context: PersistentDataAdapterContext): ExtractorData {
			val list = ConcurrentLinkedQueue<Vec3i>()

			for (key in primitive) {
				list.add(toVec3i(key))
			}

			return ExtractorData(list)
		}

		override fun toPrimitive(complex: ExtractorData, context: PersistentDataAdapterContext): LongArray {
			return complex.extractorLocations.map { toBlockKey(it) }.toLongArray()
		}
	}
}
