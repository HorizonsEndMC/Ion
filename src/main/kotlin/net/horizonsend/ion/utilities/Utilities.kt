package net.horizonsend.ion.utilities

import java.util.EnumSet

inline fun <reified T : Enum<T>> enumSetOf(vararg elems: T): EnumSet<T> =
	EnumSet.noneOf(T::class.java).apply {
		addAll(elems)
	}