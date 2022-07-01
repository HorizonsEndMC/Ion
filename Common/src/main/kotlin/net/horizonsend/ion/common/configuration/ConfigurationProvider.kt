package net.horizonsend.ion.common.configuration

import java.nio.file.Path
import net.horizonsend.ion.common.Reloadable
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import org.spongepowered.configurate.kotlin.objectMapperFactory

object ConfigurationProvider : Reloadable {
	override fun onLoad() {
		sharedConfiguration = loadConfiguration()
	}

	lateinit var configDirectory: Path

	lateinit var sharedConfiguration: SharedConfiguration
		private set

	private inline fun <reified T> loadConfiguration(): T {
		val configurationName = T::class.annotations.filterIsInstance<ConfigurationName>()[0].name

		val loader = HoconConfigurationLoader.builder()
			.path(configDirectory.resolve("$configurationName.conf"))
			.defaultOptions { options ->
				options.serializers { builder ->
					builder.registerAnnotatedObjects(objectMapperFactory())
				}
			}
			.build()

		val node = loader.load()

		loader.save(node)

		return node.get(T::class.java)!!
	}
}