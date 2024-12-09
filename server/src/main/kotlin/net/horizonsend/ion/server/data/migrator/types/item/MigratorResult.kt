package net.horizonsend.ion.server.data.migrator.types.item

sealed interface MigratorResult<T: Any> {
	class Mutation<T: Any>: MigratorResult<T>
	data class Replacement<T: Any>(val new: T) : MigratorResult<T>
}
