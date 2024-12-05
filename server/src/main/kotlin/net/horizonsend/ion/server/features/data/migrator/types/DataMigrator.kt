package net.horizonsend.ion.server.features.data.migrator.types

import net.horizonsend.ion.server.features.data.DataVersioned

/**
 * Not safe for implementation on its own. Must be registered.
 * @see DataMigrators
 **/
abstract class DataMigrator<Z: Any, T: DataVersioned<Z>>(val dataVersion: Int) : Comparable<DataMigrator<Z, T>> {
	override fun compareTo(other: DataMigrator<Z, T>): Int {
		return dataVersion.compareTo(other.dataVersion)
	}

	protected abstract fun performMigration(subject: Z, wrapper: T)

	fun migrate(subject: Z, wrapper: T) {
		performMigration(subject, wrapper)
		wrapper.setDataVersion(subject, dataVersion)
	}
}
