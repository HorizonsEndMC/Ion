package net.horizonsend.ion.common.database.types

import org.bson.types.ObjectId
import org.jetbrains.exposed.sql.BinaryColumnType
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.Table
import org.litote.kmongo.id.WrappedObjectId

@Deprecated("Use Integer Ids")
class ObjectIdColumnType<T> : IColumnType by BinaryColumnType(12) {
	override fun valueFromDB(value: Any): WrappedObjectId<*> = when (value) {
		is WrappedObjectId<*> -> value
		is ObjectId -> WrappedObjectId<T>(value)
		is ByteArray -> WrappedObjectId<T>(ObjectId(value))
		else -> error("Unsupported type ${value::class.qualifiedName}")
	}

	override fun notNullValueToDB(value: Any): ByteArray = when (value) {
		is WrappedObjectId<*> -> value.id.toByteArray()
		is ObjectId -> value.toByteArray()
		is ByteArray -> value
		else -> error("Unsupported type ${value::class.qualifiedName}")
	}

	override fun valueToDB(value: Any?): ByteArray? = when (value) {
		is WrappedObjectId<*> -> value.id.toByteArray()
		is ObjectId -> value.toByteArray()
		is ByteArray -> value
		null -> null
		else -> error("Unsupported type ${value::class.qualifiedName}")
	}
}

@Deprecated("Use Integer Ids")
@Suppress("DeprecatedCallableAddReplaceWith")
fun <T> Table.oid(name: String) = registerColumn<WrappedObjectId<T>>(name, ObjectIdColumnType<WrappedObjectId<T>>())
