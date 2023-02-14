package net.horizonsend.ion.server

import co.aikar.commands.PaperCommandManager
import io.netty.buffer.Unpooled
import net.horizonsend.ion.common.Configuration
import net.horizonsend.ion.common.Connectivity
import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.common.extensions.prefixProvider
import net.horizonsend.ion.server.configuration.BalancingConfiguration
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.client.Packets
import net.horizonsend.ion.server.features.client.whereisit.mod.FoundS2C
import net.horizonsend.ion.server.features.client.whereisit.mod.SearchC2S
import net.horizonsend.ion.server.features.client.whereisit.mod.Searcher
import net.horizonsend.ion.server.features.customItems.CustomItems
import net.horizonsend.ion.server.features.space.generation.generators.SpaceBiomeProvider
import net.horizonsend.ion.server.features.space.generation.generators.SpaceChunkGenerator
import net.horizonsend.ion.server.features.whereisit.mod.FoundS2C
import net.horizonsend.ion.server.features.whereisit.mod.SearchC2S
import net.horizonsend.ion.server.features.whereisit.mod.Searcher
import net.horizonsend.ion.server.features.worlds.IonWorld
import net.horizonsend.ion.server.miscellaneous.commands
import net.horizonsend.ion.server.miscellaneous.initializeCrafting
import net.horizonsend.ion.server.miscellaneous.listeners
import net.horizonsend.ion.server.miscellaneous.minecraft
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.FriendlyByteBuf
import net.horizonsend.ion.server.features.generation.generators.SpaceBiomeProvider
import net.horizonsend.ion.server.features.generation.generators.SpaceChunkGenerator
import net.starlegacy.feature.economy.city.CityNPCs
import net.starlegacy.feature.economy.collectors.Collectors
import net.starlegacy.feature.hyperspace.HyperspaceBeacons
import net.starlegacy.feature.nations.NationsMap
import net.starlegacy.feature.space.SpaceMap
import net.starlegacy.feature.starship.hyperspace.HyperspaceMap
import net.starlegacy.legacyDisable
import net.starlegacy.legacyEnable
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.java.JavaPlugin

object IonServer : JavaPlugin() {
	var balancing: BalancingConfiguration = Configuration.load(dataFolder, "balancing.json")
	var configuration: ServerConfiguration = Configuration.load(dataFolder, "server.json")

	override fun onEnable() {
		try {
			Connectivity.open(dataFolder)

			prefixProvider = {
				when (it) {
					is Player -> "to ${it.name}:"
					else -> ""
				}
			}

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

			// WIT networking
			Bukkit.getMessenger().registerIncomingPluginChannel(this, SearchC2S.ID.toString(), Searcher::handle)
			Bukkit.getMessenger().registerOutgoingPluginChannel(this, FoundS2C.ID.toString())

			// Void networking
			for (packet in Packets.values()) {
				logger.info("Registering ${packet.id}")

				if (packet.s2c != null) {
					Bukkit.getMessenger().registerOutgoingPluginChannel(this, packet.id.toString())
				}

				if (packet.c2s != null) {
					Bukkit.getMessenger().registerIncomingPluginChannel(
						this,
						packet.id.toString()
					) { s: String, player: Player, bytes: ByteArray ->
						logger.info("Received message on $s by ${player.name}")
						val buf = FriendlyByteBuf(Unpooled.wrappedBuffer(bytes))
						val c2s = packet.c2s!!
						c2s.invoke(buf, player)
					}
				}
			}

			// Same deal as listeners.
			initializeCrafting()

			// Basically exists as a catch all for any weird state which could result in worlds already being loaded at this
			// such as reloading or other plugins doing things they probably shouldn't.
			for (world in server.worlds) IonWorld.register(world.minecraft)

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
