package net.horizonsend.ion.common

import io.leangen.geantyref.TypeToken
import java.io.File
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import org.spongepowered.configurate.kotlin.objectMapperFactory
import org.spongepowered.configurate.kotlin.toNode

inline fun <reified T> loadConfiguration(directory: File, fileName: String): T {
	directory.mkdirs()

	val loader = HoconConfigurationLoader.builder()
		.path(directory.resolve(fileName).toPath())
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