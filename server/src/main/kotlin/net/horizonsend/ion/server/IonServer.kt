package net.horizonsend.ion.server

import co.aikar.commands.PaperCommandManager
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.database.DBManager
import net.horizonsend.ion.common.extensions.prefixProvider
import net.horizonsend.ion.common.utils.configuration.CommonConfig
import net.horizonsend.ion.common.utils.getUpdateMessage
import net.horizonsend.ion.server.command.GlobalCompletions
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.ConfigurationFiles.configurationFolder
import net.horizonsend.ion.server.features.chat.Discord
import net.horizonsend.ion.server.features.client.networking.packets.ShipData
import net.horizonsend.ion.server.features.custom.items.type.armor.AscendingMode
import net.horizonsend.ion.server.features.custom.items.type.armor.StrafingMode
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.RocketBoostingMod
import net.horizonsend.ion.server.features.misc.WorldReset
import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.features.world.generation.generators.bukkit.EmptyChunkGenerator
import net.horizonsend.ion.server.features.world.generation.generators.bukkit.SpaceBiomeProvider
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.registrations.commands
import net.horizonsend.ion.server.miscellaneous.registrations.components
import net.horizonsend.ion.server.miscellaneous.registrations.listeners
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.java.JavaPlugin
import xyz.xenondevs.invui.InvUI
import kotlin.system.measureTimeMillis

object IonServer : JavaPlugin() {
	val configProvider = ConfigurationFiles // Ensure initialization
	private lateinit var protocolManager: ProtocolManager

	override fun onLoad() {
		WorldReset.onStartup()
	}

	override fun onEnable(): Unit =
		runCatching(::internalEnable).fold(
			{
				protocolManager = ProtocolLibrary.getProtocolManager()
				protocolManager.addPacketListener(object: PacketAdapter(IonServer, PacketType.Play.Client.STEER_VEHICLE) {
					override fun onPacketReceiving(event: PacketEvent) {
						val player = event.player
						val input = (event.packet.handle as ServerboundPlayerInputPacket).input

						if ((input.left && input.right) || (!input.left && !input.right)) { // both or neither pressed
							RocketBoostingMod.strafingMode.remove(player.uniqueId)
						} else {
							if(input.left) RocketBoostingMod.strafingMode[player.uniqueId] = StrafingMode.LEFT
							if(input.right) RocketBoostingMod.strafingMode[player.uniqueId] = StrafingMode.RIGHT
						}
						if((input.forward && input.backward) || (!input.forward && !input.backward)) { // both or neither pressed
							RocketBoostingMod.ascendingMode.remove(player.uniqueId)
						} else {
							if(input.forward) RocketBoostingMod.ascendingMode[player.uniqueId] = AscendingMode.DESCENDING
							if(input.backward) RocketBoostingMod.ascendingMode[player.uniqueId] = AscendingMode.ASCENDING
						}

					}
				})

				Tasks.sync {
					val message = getUpdateMessage(dataFolder) ?: return@sync
					slF4JLogger.info(message)

					try {
						Discord.sendMessage(ConfigurationFiles.discordSettings().changelogChannel,"${ConfigurationFiles.serverConfiguration().serverName} $message")
					} catch (e: Exception) {
						slF4JLogger.error(e.message)
						e.printStackTrace()
					}
				}
			},
			{
				slF4JLogger.error("An exception occurred during plugin startup! The server will now exit.", it)
				Bukkit.shutdown()
			}
		)

	private fun internalEnable() {
		CommonConfig.init(configurationFolder) // DB Configs
		InvUI.getInstance().setPlugin(IonServer)

		prefixProvider = { // Audience extensions
			when (it) {
				is Player -> "to ${it.name}: "
				else -> ""
			}
		}

		// Basically exists as a catch all for any weird state which could result in worlds already being loaded at this
		// such as reloading or other plugins doing things they probably shouldn't.
		for (world in server.worlds) IonWorld.register(world)

		for (component in components) { // Components
			fun startAndMeasureTime(component: IonComponent) {
				val time = measureTimeMillis { component.onEnable() }
				slF4JLogger.info("Enabled ${component.javaClass.simpleName} in $time ms")
			}

			if (component is IonServerComponent) {
				if (component.runAfterTick) Tasks.sync { startAndMeasureTime(component) }
				else startAndMeasureTime(component)

				server.pluginManager.registerEvents(component, IonServer)
			} else startAndMeasureTime(component)
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

		GlobalCompletions.onEnable(commandManager)
		// First register all the completions, then register the actual commands
		commands.forEach { it.onEnable(commandManager) }
		commands.forEach {
			commandManager.registerCommand(it)

			if (it is Listener) {
				server.pluginManager.registerEvents(it, this)
			}
		}

		ShipData.enable()

		// Checkmark is not an emoji?
		DBManager.INITIALIZATION_COMPLETE = true // Start handling reads from the DB
	}

	override fun onDisable() {
		SLCommand.ASYNC_COMMAND_THREAD.shutdown()

		for (component in components.asReversed()) try {
			component.onDisable()
			slF4JLogger.info("Disabled ${component.javaClass.simpleName}")
		} catch (e: Exception) {
			slF4JLogger.error("There was an error shutting down ${component.javaClass.simpleName}! ${e.message}")
			e.printStackTrace()
		}

		IonWorld.unregisterAll()
	}

	override fun getDefaultBiomeProvider(worldName: String, id: String?): BiomeProvider {
		return SpaceBiomeProvider()
	}

	override fun getDefaultWorldGenerator(worldName: String, id: String?): ChunkGenerator {
		return EmptyChunkGenerator
	}
}

abstract class IonServerComponent(
	val runAfterTick: Boolean = false
) : Listener, IonComponent()
