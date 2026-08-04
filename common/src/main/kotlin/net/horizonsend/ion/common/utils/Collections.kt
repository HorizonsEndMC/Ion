package net.horizonsend.ion.common.utils

import com.google.common.collect.Table

operator fun <R, C, V> Table<R, C, V>.set(row: R, column: C, value: V): V? = put(row, column, value)

fun <R, C, V> Table<R, C, V>.getOrPut(row: R, column: C, defaultValue: () -> V): V {
	get(row, column)?.let { return it }

	val new = defaultValue()
	put(row, column, new)

	return new
}

fun <R, C, V, T : Table<R, C, V>> T.removeRow(row: R): T {
	rowKeySet().remove(row)
	return this
}

fun <R, C, V, T : Table<R, C, V>> T.removeColumn(column: C): T {
	columnKeySet().remove(column)
	return this
}
