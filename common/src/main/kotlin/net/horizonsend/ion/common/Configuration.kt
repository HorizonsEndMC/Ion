package net.horizonsend.ion.common

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.io.IOException

object Configuration {
	@PublishedApi
	@OptIn(ExperimentalSerializationApi::class)
	internal val json = Json {
		encodeDefaults = true
		ignoreUnknownKeys = true
		isLenient = true
		prettyPrint = true
		prettyPrintIndent = "\t"
	}

	@OptIn(ExperimentalSerializationApi::class)
	inline fun <reified T> load(directory: File, fileName: String): T {
		directory.mkdirs()
		val file = directory.resolve(fileName)

		val configuration: T = if (file.exists()) json.decodeFromStream(file.inputStream()) else json.decodeFromString("{}")

		try { save(configuration, directory, fileName) } catch (_: IOException) {
			System.err.println("Couldn't re-save configuration, this could cause problems later!")
		}

		return configuration
	}

	@OptIn(ExperimentalSerializationApi::class)
	inline fun <reified T> save(clazz: T, directory: File, fileName: String) {
		directory.mkdirs()
		val file = directory.resolve(fileName)

		json.encodeToStream(clazz, file.outputStream())
	}
}
