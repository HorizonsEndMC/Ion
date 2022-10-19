package net.starlegacy.util

import com.google.common.collect.Multimap
import com.google.common.collect.MultimapBuilder
import com.google.common.collect.Table
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
