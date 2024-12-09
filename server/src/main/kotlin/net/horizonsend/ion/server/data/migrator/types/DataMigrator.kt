package net.horizonsend.ion.server.data.migrator.types

import net.horizonsend.ion.server.data.migrator.types.item.MigratorResult

/**
 * Not safe for implementation on its own. Must be registered.
 * @see DataMigrators
 **/
abstract class DataMigrator<T: Any, W: Any> {
	protected abstract fun performMigration(subject: T, wrapper: W) : MigratorResult<T>

	fun migrate(subject: T, wrapper: W): MigratorResult<T> {
		val result = performMigration(subject, wrapper)

		return result
	}
}
