package net.horizonsend.ion.common

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.io.IOException

object Configuration {
	@OptIn(ExperimentalSerializationApi::class)
	inline fun <reified T> load(directory: File, fileName: String): T {
		directory.mkdirs()
		val file = directory.resolve(fileName)

		val configuration: T = if (file.exists()) Json.decodeFromStream(file.inputStream()) else Json.decodeFromString("{}")

		try { save(configuration, directory, fileName) } catch (_: IOException) {
			System.err.println("Couldn't re-save configuration, this could cause problems later!")
		}

		return configuration
	}

	@OptIn(ExperimentalSerializationApi::class)
	inline fun <reified T> save(clazz: T, directory: File, fileName: String) {
		directory.mkdirs()
		val file = directory.resolve(fileName)

		Json.encodeToStream(clazz, file.outputStream())
	}
}
