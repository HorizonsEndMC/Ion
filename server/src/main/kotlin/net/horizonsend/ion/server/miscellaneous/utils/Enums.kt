package net.horizonsend.ion.server.miscellaneous.utils

inline fun <reified T : Enum<T>> enumValueOfOrNull(name: String): T? {
	return enumValues<T>().find { it.name == name }
}
