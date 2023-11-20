package net.horizonsend.ion.server.miscellaneous.utils

import com.google.common.collect.Multimap
import com.google.common.collect.MultimapBuilder
import com.google.common.collect.Table
import net.horizonsend.ion.common.utils.miscellaneous.randomInt
import java.util.EnumSet
import java.util.concurrent.ThreadLocalRandom

fun <T> List<T>.randomEntry(): T = when {
	this.isEmpty() -> error("No entries in list to pick from!")
	this.size == 1 -> single()
	else -> this[randomInt(0, size)]
}

fun <K, V> multimapOf(): Multimap<K, V> = MultimapBuilder.hashKeys().arrayListValues().build()

operator fun <R, C, V> Table<R, C, V>.set(row: R, column: C, value: V): V? = put(row, column, value)

fun <T> Set<T>.randomEntry(): T = when {
	this.isEmpty() -> error("No entries in list to pick from!")
	this.size == 1 -> single()
	else -> {
		val index = ThreadLocalRandom.current().nextInt(this.size)
		val iter = this.iterator()
		for (i in 0 until index) {
			iter.next()
		}
		/*(return)*/ iter.next()
	}
}

inline fun <reified T : Enum<T>> enumSetOf(vararg elems: T): EnumSet<T> =
	EnumSet.noneOf(T::class.java).apply { addAll(elems) }

fun <K> Collection<Pair<K, *>>.firsts(): List<K> = this.map { it.first }
fun <V> Collection<Pair<*, V>>.seconds(): List<V> = this.map { it.second }
fun <K, V : Comparable<V>> Map<K, V>.keysSortedByValue(): List<K> = this.keys.sortedBy { this[it]!! }

fun <K, V: Comparable<V>> Map<K, V>.sortedByValue(): Map<K, V> {
	return this.entries.sortedBy { it.value }.toMap()
}

fun <K, V> Collection<Map.Entry<K, V>>.toMap(): Map<K, V> {
	return mutableMapOf(*this.map { it.toPair() }.toTypedArray())
}

fun <T> List<T>.safeSubList(fromIndex: Int, toIndex: Int): List<T> = this.subList(fromIndex.coerceAtLeast(this.size), toIndex.coerceAtMost(this.size))

/**
 * Returns a [List] containing all key-value pairs.
 */
fun <K, V> Map<out K, V>.toMutableList(): MutableList<Pair<K, V>> {
	if (isEmpty()) return mutableListOf()

	val iterator = entries.iterator()
	if (!iterator.hasNext()) return mutableListOf()

	val first = iterator.next()
	if (!iterator.hasNext()) return mutableListOf(first.toPair())

	val result = ArrayList<Pair<K, V>>(size)
	result.add(first.toPair())

	do {
		result.add(iterator.next().toPair())
	} while (iterator.hasNext())

	return result
}
