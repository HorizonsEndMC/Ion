package net.horizonsend.ion

import org.bukkit.Bukkit.shutdown
import org.bukkit.plugin.java.JavaPlugin
import org.spongepowered.configurate.hocon.HoconConfigurationLoader.builder
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.objectMapperFactory

@Suppress("unused") // Plugin entrypoint
class Ion: JavaPlugin() {
	val configuration: Configuration

	init {
		saveResource("config.conf", false) // Ensure the config file exists

		val newConfiguration: Configuration? = builder()
			// Specify configuration file path
			.path(dataFolder.toPath().resolve("config.conf"))

			// Register deserializer
			.defaultOptions { options ->
				options.serializers { builder ->
					builder.registerAnnotatedObjects(
						objectMapperFactory()
					)
				}
			}

			// Load configuration
			.build()
			.load()
			.get()

		if (newConfiguration == null) {
			slF4JLogger.error("Failed to load configuration, server is stopping.")
			shutdown()
			throw IllegalStateException("Failed to load configuration, server is stopping.")
		}

		configuration = newConfiguration
	}
}