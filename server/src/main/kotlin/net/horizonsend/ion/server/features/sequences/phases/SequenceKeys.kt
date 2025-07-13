package net.horizonsend.ion.server.features.sequences.phases

object SequenceKeys {
	val allKeys = mutableListOf<String>()

	fun getAll(): List<String> = allKeys

	val TUTORIAL = registerKey("TUTORIAL")

	fun registerKey(key: String): String {
		allKeys.add(key)
		return key
	}
}
