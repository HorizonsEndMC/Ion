package net.horizonsend.ion.server

import co.aikar.commands.PaperCommandManager
import net.horizonsend.ion.common.Configuration
import net.horizonsend.ion.common.Connectivity
import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.server.configuration.BalancingConfiguration
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.customItems.CustomItems
import net.horizonsend.ion.server.features.generation.SpaceGenerationManager
import net.horizonsend.ion.server.features.whereisit.mod.FoundS2C
import net.horizonsend.ion.server.features.whereisit.mod.SearchC2S
import net.horizonsend.ion.server.features.whereisit.mod.Searcher
import net.horizonsend.ion.server.features.generation.generators.SpaceBiomeProvider
import net.horizonsend.ion.server.features.generation.generators.SpaceChunkGenerator
import net.horizonsend.ion.server.features.worlds.IonWorld
import net.horizonsend.ion.server.miscellaneous.commands
import net.horizonsend.ion.server.miscellaneous.handle
import net.horizonsend.ion.server.miscellaneous.initializeCrafting
import net.horizonsend.ion.server.miscellaneous.listeners
import net.minecraft.core.registries.BuiltInRegistries
import net.starlegacy.feature.economy.city.CityNPCs
import net.starlegacy.feature.economy.collectors.Collectors
import net.starlegacy.feature.hyperspace.HyperspaceBeacons
import net.starlegacy.feature.nations.NationsMap
import net.starlegacy.feature.space.SpaceMap
import net.starlegacy.feature.starship.hyperspace.HyperspaceMap
import net.starlegacy.legacyDisable
import net.starlegacy.legacyEnable
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.java.JavaPlugin

@Suppress("Unused")
class IonServer : JavaPlugin() {
	init {
		Ion = this
	}

	companion object {
		lateinit var Ion: IonServer private set
	}

	var balancing: BalancingConfiguration = Configuration.load(dataFolder, "balancing.json")
	var configuration: ServerConfiguration = Configuration.load(dataFolder, "server.json")

	override fun onEnable() {
		try {
			Connectivity.open(dataFolder)

			val pluginManager = server.pluginManager

			// Commands
			val commandManager = PaperCommandManager(this)

			@Suppress("Deprecation")
			commandManager.enableUnstableAPI("help")

			for (command in commands) commandManager.registerCommand(command)

			commandManager.commandCompletions.registerStaticCompletion(
				"achievements",
				Achievement.values().map { it.name }
			)

			commandManager.commandCompletions.registerCompletion("customItem") { context ->
				CustomItems.identifiers.filter { context.player.hasPermission("ion.customitem.$it") }
			}
			commandManager.commandCompletions.registerCompletion("particles") { context ->
				BuiltInRegistries.PARTICLE_TYPE.keySet()
					.filter { context.player.hasPermission("ion.settings.particle.$it") }
					.map { "$it" }
			}

			// The listeners are defined in a separate file for the sake of keeping the main class clean.
			for (listener in listeners) pluginManager.registerEvents(listener, this)

			Bukkit.getMessenger().registerIncomingPluginChannel(this, SearchC2S.ID.toString(), Searcher::handle)
			Bukkit.getMessenger().registerOutgoingPluginChannel(this, FoundS2C.ID.toString())

			// Same deal as listeners.
			initializeCrafting()

			// Basically exists as a catch all for any weird state which could result in worlds already being loaded at this
			// such as reloading or other plugins doing things they probably shouldn't.
			for (world in server.worlds) IonWorld.register(world.handle)

			legacyEnable(commandManager)

			Bukkit.getScheduler().runTaskLater(
				this,
				Runnable {
					SpaceMap.onEnable()
					NationsMap.onEnable()
					HyperspaceMap.onEnable()
					HyperspaceBeacons.reloadDynmap()
					Collectors.onEnable()
					CityNPCs.onEnable()

					pluginManager.registerEvents(CityNPCs, this)
				},
				1
			)
		} catch (exception: Exception) {
			slF4JLogger.error("An exception occurred during plugin startup! The server will now exit.", exception)
			Bukkit.shutdown()
		}
	}

	override fun onDisable() {
		IonWorld.unregisterAll()
		legacyDisable()
		Connectivity.close()
	}

	override fun getDefaultBiomeProvider(worldName: String, id: String?): BiomeProvider {
		return SpaceBiomeProvider()
	}

	override fun getDefaultWorldGenerator(worldName: String, id: String?): ChunkGenerator {
		return SpaceChunkGenerator()
	}
}
