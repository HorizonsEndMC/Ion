package net.horizonsend.ion.server

import co.aikar.commands.PaperCommandManager
import net.horizonsend.ion.common.CommonConfig
import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.database.DBManager
import net.horizonsend.ion.common.extensions.prefixProvider
import net.horizonsend.ion.common.utils.Configuration
import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.common.utils.getUpdateMessage
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.configuration.GassesConfiguration
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.configuration.StarshipTypeBalancing
import net.horizonsend.ion.server.configuration.TradeConfiguration
import net.horizonsend.ion.server.features.misc.messaging.ServerDiscordMessaging
import net.horizonsend.ion.server.features.space.generation.generators.SpaceBiomeProvider
import net.horizonsend.ion.server.features.space.generation.generators.SpaceChunkGenerator
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.IonWorld
import net.horizonsend.ion.server.miscellaneous.LegacyConfig
import net.horizonsend.ion.server.miscellaneous.registrations.commands
import net.horizonsend.ion.server.miscellaneous.registrations.components
import net.horizonsend.ion.server.miscellaneous.registrations.listeners
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.loadConfig
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

val LegacySettings get() = IonServer.legacySettings
val ServerConfiguration get() = IonServer.configuration

val sharedDataFolder by lazy { File(LegacySettings.sharedFolder).apply { mkdirs() } }

object IonServer : JavaPlugin() {
	val configurationFolder = dataFolder.resolve("configuration").apply { mkdirs() }

	var pvpBalancing: PVPBalancingConfiguration = Configuration.load(configurationFolder, "pvpbalancing.json")
	var starshipBalancing: StarshipTypeBalancing = Configuration.load(configurationFolder, "starshipbalancing.json")

	var configuration: ServerConfiguration = Configuration.load(configurationFolder, "server.json")
	var gassesConfiguration: GassesConfiguration = Configuration.load(configurationFolder, "gasses.json")
	var tradeConfiguration: TradeConfiguration = Configuration.load(configurationFolder, "trade.json")
	var aiShipConfiguration: AIShipConfiguration = Configuration.load(configurationFolder, "aiships.json")
	var legacySettings: LegacyConfig = loadConfig(configurationFolder, "config") // Setting

	override fun onEnable(): Unit =
		runCatching(::internalEnable).fold(
			{
				val message = getUpdateMessage(dataFolder) ?: return
				slF4JLogger.info(message)

				try {
					ServerDiscordMessaging.changelogMessage(text("${configuration.serverName} $message"))
				} catch (_: Exception) {
				}
			},
			{
				slF4JLogger.error("An exception occurred during plugin startup! The server will now exit.", it)
				Bukkit.shutdown()
			}
		)

	private fun internalEnable() {
		CommonConfig.init(IonServer.dataFolder) // DB Configs

		prefixProvider = { // Audience extensions
			when (it) {
				is Player -> "to ${it.name}: "
				else -> ""
			}
		}

		// Basically exists as a catch all for any weird state which could result in worlds already being loaded at this
		// such as reloading or other plugins doing things they probably shouldn't.
		for (world in server.worlds) IonWorld.register(world.minecraft)

		for (component in components) { // Components
			if (component is IonServerComponent) {
				if (component.runAfterTick)
					Tasks.sync { component.onEnable() }
				else component.onEnable()

				IonServer.server.pluginManager.registerEvents(component, IonServer)
			} else component.onEnable()
		}

		// The listeners are defined in a separate file for the sake of keeping the main class clean.
		for (listener in listeners) {
			if (listener is SLEventListener)
				listener.register()
			else
				server.pluginManager.registerEvents(listener, IonServer)
		}

		// Commands
		val commandManager = PaperCommandManager(this).apply {
			enableUnstableAPI("help")
			// enableUnstableAPI("brigadier") BROKEN DO NOT ENABLE
		}

		// First register all the completions, then register the actual commands
		commands.forEach { it.onEnable(commandManager) }
		commands.forEach {
			commandManager.registerCommand(it)

			if (it is Listener) {
				server.pluginManager.registerEvents(it, this)
			}
		}

		// Checkmark is not an emoji?
		ServerDiscordMessaging.globalEmbed(Embed(title = "✅ ${configuration.serverName} has started", color = NamedTextColor.GREEN.value()))
		DBManager.INITIALIZATION_COMPLETE = true // Start handling reads from the DB
	}

	override fun onDisable() {
		IonWorld.unregisterAll()

		SLCommand.ASYNC_COMMAND_THREAD.shutdown()

		for (component in components.asReversed()) try {
			component.onDisable()
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	override fun getDefaultBiomeProvider(worldName: String, id: String?): BiomeProvider {
		return SpaceBiomeProvider()
	}

	override fun getDefaultWorldGenerator(worldName: String, id: String?): ChunkGenerator {
		return SpaceChunkGenerator()
	}
}

abstract class IonServerComponent(
	val runAfterTick: Boolean = false
) : Listener, IonComponent()
