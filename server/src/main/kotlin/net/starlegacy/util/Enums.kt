package net.starlegacy.util

inline fun <reified T : Enum<T>> enumValueOfOrNull(name: String): T? {
	return enumValues<T>().find { it.name == name }
}
