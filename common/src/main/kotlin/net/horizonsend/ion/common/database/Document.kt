package net.horizonsend.ion.common.database

internal abstract class Document {
	abstract fun update()
}

internal fun <D : Document> D.update(action: D.() -> Unit): D {
	action(this)
	update()
	return this
}
