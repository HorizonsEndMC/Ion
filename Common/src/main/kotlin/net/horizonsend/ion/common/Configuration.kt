package net.horizonsend.ion.common

import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import org.spongepowered.configurate.kotlin.objectMapperFactory
import org.spongepowered.configurate.kotlin.toNode
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createDirectories

inline fun <reified T> loadConfiguration(file: File, fileName: String): T = loadConfiguration(file.toPath(), fileName)

inline fun <reified T> loadConfiguration(path: Path, fileName: String): T {
	path.createDirectories()

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