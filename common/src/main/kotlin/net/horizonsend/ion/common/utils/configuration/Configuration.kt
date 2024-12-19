package net.horizonsend.ion.common.utils.configuration

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.serializer
import java.io.File
import java.io.IOException
import kotlin.reflect.KClass
import kotlin.reflect.full.starProjectedType

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

	fun getJsonSerializer() = json

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
	inline fun <reified T> loadOrDefault(directory: File, fileName: String, default: T): T {
		directory.mkdirs()
		val file = directory.resolve(fileName)

		val configuration: T = if (file.exists()) json.decodeFromStream(file.inputStream()) else default

		try { save(configuration, directory, fileName) } catch (_: IOException) {
			System.err.println("Couldn't re-save configuration, this could cause problems later!")
		}

		return configuration
	}

	inline fun <reified T> parse(text: String): T {
		return json.decodeFromString<T>(text)
	}

	@OptIn(ExperimentalSerializationApi::class)
	inline fun <reified T> save(clazz: T, directory: File, fileName: String) {
		directory.mkdirs()
		val file = directory.resolve(fileName)

		json.encodeToStream(clazz, file.outputStream())
	}

	@OptIn(ExperimentalSerializationApi::class)
	fun <T : Any> save(clazz: KClass<out T>, instance: T, directory: File, fileName: String) {
		directory.mkdirs()
		val file = directory.resolve(fileName)

		json.encodeToStream(json.serializersModule.serializer(clazz.starProjectedType), instance, file.outputStream())
	}

	inline fun <reified T> write(clazz: T): String {
		return json.encodeToString(clazz)
	}
}
