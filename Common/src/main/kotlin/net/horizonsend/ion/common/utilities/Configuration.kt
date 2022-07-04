package net.horizonsend.ion.common.utilities

import java.nio.file.Path
import kotlin.io.path.createDirectories
import net.horizonsend.ion.common.annotations.ConfigurationName
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import org.spongepowered.configurate.kotlin.objectMapperFactory
import org.spongepowered.configurate.kotlin.toNode

inline fun <reified T> loadConfiguration(directory: Path): T {
	directory.createDirectories()

	val configurationName = T::class.annotations.filterIsInstance<ConfigurationName>()[0].name

	val loader = HoconConfigurationLoader.builder()
		.path(directory.resolve("$configurationName.conf"))
		.defaultOptions { options ->
			options.serializers { builder ->
				builder.registerAnnotatedObjects(objectMapperFactory())
			}
		}
		.build()

	val node = loader.load()

	val configuration = node.get(T::class.java)!!

	configuration.toNode(node)
	loader.save(node)

	return configuration
}