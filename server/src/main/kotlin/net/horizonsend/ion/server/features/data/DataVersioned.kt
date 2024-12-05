package net.horizonsend.ion.server.features.data

interface DataVersioned <T: Any> {
	val latestDataVersion: Int

	fun getDataVersion(subject: T): Int

	fun setDataVersion(subject: T, version: Int)
}
