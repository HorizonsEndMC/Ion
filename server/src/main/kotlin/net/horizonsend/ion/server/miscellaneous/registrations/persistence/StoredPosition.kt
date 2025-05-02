package net.horizonsend.ion.server.miscellaneous.registrations.persistence

import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class StoredPosition(
	val x: Int,
	val y: Int,
	val z: Int
) {
	constructor(vec3i: Vec3i) : this(vec3i.x, vec3i.y, vec3i.z)

	companion object : PersistentDataType<PersistentDataContainer, StoredPosition> {
		override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java
		override fun getComplexType(): Class<StoredPosition> = StoredPosition::class.java

		override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): StoredPosition {
			val x = primitive.get(NamespacedKeys.X, PersistentDataType.INTEGER)!!
			val y = primitive.get(NamespacedKeys.Y, PersistentDataType.INTEGER)!!
			val z = primitive.get(NamespacedKeys.Z, PersistentDataType.INTEGER)!!

			return StoredPosition(x, y, z)
		}

		override fun toPrimitive(complex: StoredPosition, context: PersistentDataAdapterContext): PersistentDataContainer {
			val primitive = context.newPersistentDataContainer()

			primitive.set(NamespacedKeys.X, PersistentDataType.INTEGER, complex.x)
			primitive.set(NamespacedKeys.Y, PersistentDataType.INTEGER, complex.y)
			primitive.set(NamespacedKeys.Z, PersistentDataType.INTEGER, complex.z)

			return primitive
		}
	}
}
