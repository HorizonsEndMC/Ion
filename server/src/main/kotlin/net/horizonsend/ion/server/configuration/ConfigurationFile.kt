package net.horizonsend.ion.server.configuration

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.serializer
import net.horizonsend.ion.common.utils.configuration.Configuration
import net.horizonsend.ion.common.utils.configuration.Configuration.save
import java.io.File
import java.io.IOException
import kotlin.reflect.KClass

class ConfigurationFile<T: Any>(val configurationClass: KClass<out T>, val directory: File, val fileName: String) {
	private var instance: T = load()

	fun reload() {
		instance = load()
	}

	fun get(): T = instance

	@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
	private fun load(): T {
		directory.mkdirs()
		val file = directory.resolve(fileName)

		val serializer = configurationClass.serializer()

		val json = Configuration.getJson()
		val configuration: T = if (file.exists()) json.decodeFromStream(serializer, file.inputStream()) else json.decodeFromString(serializer, "{}")

		try { save(serializer, directory, fileName) } catch (_: IOException) {
			System.err.println("Couldn't re-save configuration, this could cause problems later!")
		}

		return configuration
	}
}
