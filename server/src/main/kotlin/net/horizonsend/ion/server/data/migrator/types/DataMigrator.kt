package net.horizonsend.ion.server.data.migrator.types

import net.horizonsend.ion.server.data.migrator.types.item.MigratorResult

/**
 * Not safe for implementation on its own. Must be registered.
 * @see DataMigrators
 **/
abstract class DataMigrator<T: Any, W: Any> {
	protected abstract fun performMigration(subject: T) : MigratorResult<T>

	fun migrate(subject: T): MigratorResult<T> {
		val result = performMigration(subject)

		return result
	}
}
