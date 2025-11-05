package net.horizonsend.ion.server.miscellaneous.registrations.persistence

import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class PairType<A : Any, B : Any>(
	val firstSerializer: PersistentDataType<*, A>,
	val secondSerializer: PersistentDataType<*, B>
) : PersistentDataType<PersistentDataContainer, Pair<A, B>> {
	override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java
	@Suppress("UNCHECKED_CAST")
	override fun getComplexType(): Class<Pair<A, B>> = Pair::class.java as Class<Pair<A, B>>

	override fun toPrimitive(complex: Pair<A, B>, context: PersistentDataAdapterContext): PersistentDataContainer {
		val pdc = context.newPersistentDataContainer()

		pdc.set(FIRST, firstSerializer, complex.first)
		pdc.set(SECOND, secondSerializer, complex.second)

		return pdc
	}

	override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): Pair<A, B> {
		return Pair(primitive.get(FIRST, firstSerializer)!!, primitive.get(SECOND, secondSerializer)!!)
	}

	companion object {
		private val FIRST = NamespacedKeys.key("first")
		private val SECOND = NamespacedKeys.key("second")

		fun <A : Any, B : Any> pairListType(firstSerializer: PersistentDataType<*, A>, secondSerializer: PersistentDataType<*, B>) = PersistentDataType.LIST.listTypeFrom(PairType(firstSerializer, secondSerializer))
	}
}
