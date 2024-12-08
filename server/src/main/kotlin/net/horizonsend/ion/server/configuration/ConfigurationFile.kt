package net.horizonsend.ion.server.configuration

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.serializer
import net.horizonsend.ion.common.utils.configuration.Configuration
import net.horizonsend.ion.common.utils.configuration.Configuration.save
import java.io.File
import java.io.IOException
import kotlin.reflect.KClass
import kotlin.reflect.full.starProjectedType

class ConfigurationFile<T: Any>(val configurationClass: KClass<out T>, val directory: File, val fileName: String) {
	private var instance: T = load()

	fun reload() {
		instance = try {
			load()
		} catch (e: Throwable) {
			throw Throwable("There was an error loading $fileName.json:", e)
		}
	}

	fun saveToDisk() {
		save(configurationClass, instance, directory, fileName)
	}

	fun get(): T = instance

	operator fun invoke() = get()

	@OptIn(ExperimentalSerializationApi::class)
	private fun load(): T {
		directory.mkdirs()
		val file = directory.resolve(fileName)

		val json = Configuration.getJsonSerializer()
		val serializer = json.serializersModule.serializer(configurationClass.starProjectedType) as KSerializer<T>

		val configuration: T = if (file.exists()) json.decodeFromStream(serializer, file.inputStream()) else json.decodeFromString(serializer, "{}")

		try { save(serializer, directory, fileName) } catch (_: IOException) {
			System.err.println("Couldn't re-save configuration, this could cause problems later!")
		}

		return configuration
	}
}
