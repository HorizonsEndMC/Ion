package net.horizonsend.ion.common.database

abstract class Document<T> {
	@Suppress("PropertyName")
	abstract val _id: T

	internal abstract fun update()
}

fun <D : Document<*>> D.update(action: D.() -> Unit): D {
	action(this)
	update()
	return this
}