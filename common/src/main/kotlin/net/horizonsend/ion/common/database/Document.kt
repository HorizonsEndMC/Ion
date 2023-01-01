package net.horizonsend.ion.common.database

abstract class Document {
	internal abstract fun update()
}

fun <D : Document> D.update(action: D.() -> Unit): D {
	action(this)
	update()
	return this
}
