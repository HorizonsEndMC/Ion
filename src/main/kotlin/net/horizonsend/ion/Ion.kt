package net.horizonsend.ion

import co.aikar.commands.PaperCommandManager
import net.horizonsend.ion.ores.OreListener
import org.bukkit.Bukkit.shutdown
import org.bukkit.Material.DIORITE
import org.bukkit.Material.GLOWSTONE_DUST
import org.bukkit.Material.QUARTZ
import org.bukkit.Material.REDSTONE
import org.bukkit.NamespacedKey
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.spongepowered.configurate.ConfigurateException
import org.spongepowered.configurate.hocon.HoconConfigurationLoader.builder
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.objectMapperFactory

@Suppress("unused") // Plugin entrypoint
class Ion: JavaPlugin() {
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

		val listenerCommands = setOf(
			AutoRestart(this)
		)

		val commands = setOf(
			IonReloadCommand(this),
			ShrugCommand()
		)

		val commandManager = PaperCommandManager(this)

		commands.forEach { commandManager.registerCommand(it) }
		listenerCommands.forEach { commandManager.registerCommand(it) }

		@Suppress("DEPRECATION")
		commandManager.enableUnstableAPI("help")

		server.pluginManager.registerEvents(MiscellaneousListeners(), this)
		server.pluginManager.registerEvents(OreListener(this), this)

		listenerCommands.forEach { server.pluginManager.registerEvents(it, this) }

		this.server.addRecipe(FurnaceRecipe(NamespacedKey(this, "quartzrecipe"), ItemStack(QUARTZ), DIORITE, 1f, 400))
		this.server.addRecipe(FurnaceRecipe(NamespacedKey(this, "glowstonerecipe"), ItemStack(GLOWSTONE_DUST), REDSTONE, 1f, 400))

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