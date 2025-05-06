package net.horizonsend.ion.common.utils

import com.google.common.collect.Table

operator fun <R, C, V> Table<R, C, V>.set(row: R, column: C, value: V): V? = put(row, column, value)
