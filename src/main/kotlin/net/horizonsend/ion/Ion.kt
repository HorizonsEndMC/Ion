package net.horizonsend.ion

import co.aikar.commands.PaperCommandManager
import net.horizonsend.ion.commands.IonReloadCommand
import net.horizonsend.ion.listeners.EnchantmentListener
import org.bukkit.Bukkit.shutdown
import org.bukkit.plugin.java.JavaPlugin
import org.spongepowered.configurate.ConfigurateException
import org.spongepowered.configurate.hocon.HoconConfigurationLoader.builder
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.objectMapperFactory

@Suppress("unused") // Plugin entrypoint
class Ion: JavaPlugin() {
	private val listeners = setOf(
		EnchantmentListener()
	)

	private val commands = setOf(
		IonReloadCommand(this)
	)

	private lateinit var configuration: Configuration

	internal fun loadConfiguration() {
		saveResource("config.conf", false) // Ensure the config file exists

		configuration = builder()
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
			.get()!!
	}

	override fun onEnable() {
		try {
			loadConfiguration()
		} catch (exception: ConfigurateException) {
			slF4JLogger.error("Failed to load Ion configuration: ${exception.message}")
			shutdown()
		}

		val commandManager = PaperCommandManager(this)

		commands.forEach { commandManager.registerCommand(it) }

		@Suppress("DEPRECATION")
		commandManager.enableUnstableAPI("help")

		listeners.forEach { server.pluginManager.registerEvents(it, this) }

//		/**
//		 * Check for IonCore
//		 */
//		if (getPluginManager().isPluginEnabled("IonCore")) {
//			// Register anything that requires IonCore here.
//
//		} else {
//			slF4JLogger.error("""
//				IonCore is missing! Depending on what your doing this is what you wanted, or a big problem.
//				The following features are currently disabled due to IonCore being absent:
//				**None, Ion is basically a dummy plugin right now.**
//			""".trimIndent())
//		}
	}
}