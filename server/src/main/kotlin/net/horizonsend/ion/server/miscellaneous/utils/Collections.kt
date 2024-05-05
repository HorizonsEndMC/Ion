package net.horizonsend.ion.server.miscellaneous.utils

import com.google.common.collect.Multimap
import com.google.common.collect.MultimapBuilder
import com.google.common.collect.Table
import net.horizonsend.ion.common.utils.miscellaneous.randomInt
import java.util.EnumSet
import java.util.concurrent.ThreadLocalRandom
import kotlin.random.Random
import kotlin.random.asKotlinRandom
import kotlin.reflect.KClass

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

fun <K, V, R, Z> Map<K, V>.mapTo(other: MutableMap<R, Z>, transform: (Map.Entry<K,V>) -> Pair<R, Z>) = other.putAll(map(transform))
fun <K, V, R, Z> Map<K, V>.mapNotNullTo(other: MutableMap<R, Z>, transform: (Map.Entry<K,V>) -> Pair<R, Z>?) = other.putAll(map(transform).filterNotNull())
fun <T> MutableSet<T>.and(vararg others: T): MutableSet<T> = apply { others.forEach { add(it) } }

fun <T, R : Any> Iterable<T>.filterIsInstance(clazz: KClass<out R>, transform: (T) -> Any?): List<T> {
	val destination = ArrayList<T>()

	for (element in this) if (clazz.isInstance(transform(element))) destination.add(element)

	return destination
}

fun <T> Iterable<T>.weightedRandomOrNull(random: Random = ThreadLocalRandom.current().asKotlinRandom(), selector: (T) -> Double): T? {
	val sum = this.sumOf { selector(it) }
	if (sum <= 0.0) return null
	val selectionPoint = random.nextDouble(0.0, sum)

	var running = 0.0

	for (entry: T in this) {
		val probability = selector(entry)

		if (selectionPoint in running..(running + probability)) return entry

		running += probability
	}

	return null
}

fun <T> Iterable<T>.weightedRandom(random: Random = ThreadLocalRandom.current().asKotlinRandom(), selector: (T) -> Double): T = weightedRandomOrNull(random, selector) ?: throw NoSuchElementException()

// Idk why I did this
operator fun <A> Pair<A, A>.iterator(): Iterator<A> = object : Iterator<A> {
	var current: Boolean = false

	override fun hasNext(): Boolean {
		return !current
	}

	override fun next(): A {
		current = true
		return second
	}
}

fun <K, V, R : Comparable<R>> MutableMap<K, V>.popMaxByOrNull(selector: (Map.Entry<K, V>) -> R): Map.Entry<K, V>? {
	val max = maxByOrNull(selector) ?: return null
	remove(max.key)

	return max
}

inline fun <K, V> Iterable<K>.associateWithNotNull(valueSelector: (K) -> V?): Map<K, V> {
	@Suppress("UNCHECKED_CAST")
	return associateWith(valueSelector).filterValues { it != null } as Map<K, V>
}

inline fun <reified T, K, V> Map<K, V>.filterValuesIsInstance(): Map<K, T> {
	@Suppress("UNCHECKED_CAST")
	return filterValues { it is T } as Map<K, T>
}

inline fun <reified T, K, V> Map<K, V>.filterKeysIsInstance(): Map<T, V> {
	@Suppress("UNCHECKED_CAST")
	return filterKeys { it is T } as Map<T, V>
}
