package net.horizonsend.ion.common.configuration

import java.nio.file.Path
import net.horizonsend.ion.common.configuration.server.ServerConfiguration
import net.horizonsend.ion.common.configuration.shared.SharedConfiguration
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.objectMapperFactory

object ConfigurationProvider {
	private lateinit var sharedConfigurationLoader: HoconConfigurationLoader

	private lateinit var sharedConfigurationNode: CommentedConfigurationNode

	lateinit var sharedConfiguration: SharedConfiguration
		private set

	private lateinit var serverConfigurationLoader: HoconConfigurationLoader

	private lateinit var serverConfigurationNode: CommentedConfigurationNode

	lateinit var serverConfiguration: ServerConfiguration
		private set

	fun loadConfiguration(pluginDirectory: Path) {
		sharedConfigurationLoader = HoconConfigurationLoader.builder()
			.path(pluginDirectory.resolve("shared.conf"))
			.defaultOptions { options ->
				options.serializers { builder ->
					builder.registerAnnotatedObjects(objectMapperFactory())
				}
			}
			.build()

		sharedConfigurationNode = sharedConfigurationLoader.load()
		sharedConfiguration = sharedConfigurationNode.get()!!

		sharedConfigurationNode.set(sharedConfiguration)
		sharedConfigurationLoader.save(sharedConfigurationNode)

		serverConfigurationLoader = HoconConfigurationLoader.builder()
			.path(pluginDirectory.resolve("server.conf"))
			.defaultOptions { options ->
				options.serializers { builder ->
					builder.registerAnnotatedObjects(objectMapperFactory())
				}
			}
			.build()

		serverConfigurationNode = serverConfigurationLoader.load()
		serverConfiguration = serverConfigurationNode.get()!!

		serverConfigurationNode.set(serverConfiguration)
		serverConfigurationLoader.save(serverConfigurationNode)
	}
}