package net.horizonsend.ion.server.data

interface DataVersioned <T: Any> {
	val latestDataVersion: Int

	fun getDataVersion(subject: T): Int

	fun setDataVersion(subject: T, version: Int)
}
