package net.horizonsend.ion.common.configuration

import java.nio.file.Path
import net.horizonsend.ion.common.Reloadable
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import org.spongepowered.configurate.kotlin.objectMapperFactory

object ConfigurationProvider : Reloadable {
	lateinit var pluginDirectory: Path

	lateinit var sharedConfiguration: SharedConfiguration
		private set

	override fun reload() {
		sharedConfiguration = load()
	}

	private inline fun <reified T> load(): T {
		val configurationName = T::class.annotations.filterIsInstance<ConfigurationName>()[0].name

		val loader = HoconConfigurationLoader.builder()
			.path(pluginDirectory.resolve("$configurationName.conf"))
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