package net.horizonsend.ion.common.database

import org.bson.codecs.pojo.annotations.BsonId
import java.util.UUID

abstract class Document(
	@BsonId val uuid: UUID
) {
	internal abstract fun update()
}

fun <D : Document> D.update(action: D.() -> Unit): D {
	action(this)
	update()
	return this
}