package net.horizonsend.ion.common

import io.leangen.geantyref.TypeToken
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import org.spongepowered.configurate.kotlin.objectMapperFactory
import org.spongepowered.configurate.kotlin.toNode
import java.io.File
import java.io.IOException
import java.nio.file.Path

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

@Deprecated("Use KotlinX Serialization based configuration")
inline fun <reified T> loadConfiguration(file: File, fileName: String): T = loadConfiguration(file.toPath(), fileName)

@Deprecated("Use KotlinX Serialization based configuration")
inline fun <reified T> loadConfiguration(path: Path, fileName: String): T {
	path.toFile().mkdirs()

	val loader = HoconConfigurationLoader.builder()
		.path(path.resolve(fileName))
		.defaultOptions { options ->
			options.serializers { builder ->
				builder.registerAnnotatedObjects(objectMapperFactory())
			}
		}
		.build()

	val node = loader.load()

	val configuration = node.get(object : TypeToken<T>() {})!!

	configuration.toNode(node)
	loader.save(node)

	return configuration
}
