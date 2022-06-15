package net.horizonsend.ion.common.configuration

import java.nio.file.Path
import net.horizonsend.ion.common.configuration.server.ServerConfiguration
import net.horizonsend.ion.common.configuration.shared.SharedConfiguration
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.objectMapperFactory

object ConfigurationProvider {
	lateinit var sharedConfiguration: SharedConfiguration
		private set

	lateinit var serverConfiguration: ServerConfiguration
		private set

	fun loadConfiguration(pluginDirectory: Path) {
		sharedConfiguration = HoconConfigurationLoader.builder()
			.path(pluginDirectory.resolve("shared.conf"))
			.defaultOptions { options ->
				options.serializers { builder ->
					builder.registerAnnotatedObjects(objectMapperFactory())
				}
			}
			.build()
			.load()
			.get()!!

		serverConfiguration = HoconConfigurationLoader.builder()
			.path(pluginDirectory.resolve("server.conf"))
			.defaultOptions { options ->
				options.serializers { builder ->
					builder.registerAnnotatedObjects(objectMapperFactory())
				}
			}
			.build()
			.load()
			.get()!!
	}
}