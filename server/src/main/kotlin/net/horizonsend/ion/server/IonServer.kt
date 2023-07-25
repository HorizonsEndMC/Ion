package net.horizonsend.ion.server

import co.aikar.commands.PaperCommandManager
import github.scarsz.discordsrv.DiscordSRV
import net.horizonsend.ion.common.CommonConfig
import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.database.DBManager
import net.horizonsend.ion.common.extensions.prefixProvider
import net.horizonsend.ion.common.utils.Configuration
import net.horizonsend.ion.common.utils.getUpdateMessage
import net.horizonsend.ion.server.configuration.BalancingConfiguration
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.CombatNPCs
import net.horizonsend.ion.server.features.client.networking.packets.ShipData
import net.horizonsend.ion.server.features.space.generation.SpaceGenerationManager
import net.horizonsend.ion.server.features.space.generation.generators.SpaceBiomeProvider
import net.horizonsend.ion.server.features.space.generation.generators.SpaceChunkGenerator
import net.horizonsend.ion.server.legacy.NewPlayerProtection
import net.horizonsend.ion.server.miscellaneous.IonWorld
import net.horizonsend.ion.server.miscellaneous.events.IonDisableEvent
import net.horizonsend.ion.server.miscellaneous.firsts
import net.horizonsend.ion.server.miscellaneous.minecraft
import net.horizonsend.ion.server.miscellaneous.registrations.commands
import net.horizonsend.ion.server.miscellaneous.registrations.components
import net.horizonsend.ion.server.miscellaneous.registrations.listeners
import net.starlegacy.LegacySettings
import net.starlegacy.feature.economy.city.CityNPCs
import net.starlegacy.feature.economy.collectors.Collectors
import net.starlegacy.feature.hyperspace.HyperspaceBeacons
import net.starlegacy.feature.machine.AreaShields
import net.starlegacy.feature.nations.NationsMap
import net.starlegacy.feature.nations.NationsMasterTasks
import net.starlegacy.feature.space.SpaceMap
import net.starlegacy.feature.starship.hyperspace.HyperspaceMap
import net.starlegacy.legacyDisable
import net.starlegacy.legacyEnable
import net.starlegacy.listener.SLEventListener
import net.starlegacy.scheduleNationTasks
import net.starlegacy.util.Notify
import net.starlegacy.util.Tasks
import net.starlegacy.util.loadConfig
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.java.JavaPlugin

abstract class IonServerComponent(
	val runAfterTick: Boolean = false
) : Listener, IonComponent()

object IonServer : JavaPlugin() {
	var balancing: BalancingConfiguration = Configuration.load(dataFolder, "balancing.json")
	var configuration: ServerConfiguration = Configuration.load(dataFolder, "server.json")

	override fun onEnable() {
		val exception = runCatching(::internalEnable).exceptionOrNull() ?: return
		slF4JLogger.error("An exception occurred during plugin startup! The server will now exit.", exception)
		Bukkit.shutdown()
	}

	private fun internalEnable() {
		CommonConfig.init(IonServer.dataFolder)// s

		prefixProvider = {
			when (it) {
				is Player -> "to ${it.name}: "
				else -> ""
			}
		}

		// Commands
		val commandManager = PaperCommandManager(this).apply { enableUnstableAPI("help") }

		for (command in commands) {
			commandManager.registerCommand(command)
			command.onEnable(commandManager)

			if (command is Listener) {
				server.pluginManager.registerEvents(command, this)
			}
		}

		// The listeners are defined in a separate file for the sake of keeping the main class clean.
		for (listener in listeners) {
			if (listener is SLEventListener)
				listener.register()
			else
				server.pluginManager.registerEvents(listener, IonServer)
		}

		// Basically exists as a catch all for any weird state which could result in worlds already being loaded at this
		// such as reloading or other plugins doing things they probably shouldn't.
		for (world in server.worlds) IonWorld.register(world.minecraft)

		LegacySettings = loadConfig(IonServer.dataFolder, "config") // Setting

		for (component in components) { // Components
			if (component is IonServerComponent) {
				if (component.runAfterTick)
					Tasks.sync { component.onEnable() }
				else component.onEnable()

				IonServer.server.pluginManager.registerEvents(component, IonServer)
			} else component.onEnable()
		}

		scheduleNationTasks()

		NewPlayerProtection.onEnable()

		if (LegacySettings.master) {
			// 20 ticks * 60 = 1 minute, 20 ticks * 60 * 60 = 1 hour
			Tasks.asyncRepeat(20 * 60, 20 * 60 * 60) {
				NationsMasterTasks.executeAll()
			}
		}

		DBManager.INITIALIZATION_COMPLETE = true // Start handling reads from the DB

		legacyEnable(commandManager)

		Bukkit.getScheduler().runTaskLater(
			this,
			Runnable
			{
				val message = getUpdateMessage(dataFolder) ?: return@Runnable
				slF4JLogger.info(message)

				Notify.eventsChannel("${configuration.serverName} $message")
				DiscordSRV.getPlugin().jda.getTextChannelById(1096907580577697833L)
					?.sendMessage("${configuration.serverName} $message")
			},
			1
		)
	}

	override fun onDisable() {
		Bukkit.getPluginManager().callEvent(IonDisableEvent())
		IonWorld.unregisterAll()
		legacyDisable()
		CombatNPCs.npcToPlayer.values.firsts().forEach(CombatNPCs::destroyNPC)
	}

	override fun getDefaultBiomeProvider(worldName: String, id: String?): BiomeProvider {
		return SpaceBiomeProvider()
	}

	override fun getDefaultWorldGenerator(worldName: String, id: String?): ChunkGenerator {
		return SpaceChunkGenerator()
	}
}
