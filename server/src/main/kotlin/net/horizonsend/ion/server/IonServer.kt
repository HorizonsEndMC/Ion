package net.horizonsend.ion.server

import co.aikar.commands.PaperCommandManager
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import github.scarsz.discordsrv.DiscordSRV
import io.netty.buffer.Unpooled
import net.horizonsend.ion.common.Configuration
import net.horizonsend.ion.server.features.achievements.Achievement
import net.horizonsend.ion.common.extensions.prefixProvider
import net.horizonsend.ion.common.getUpdateMessage
import net.horizonsend.ion.server.configuration.BalancingConfiguration
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.client.networking.Packets
import net.horizonsend.ion.server.features.client.networking.packets.ShipData
import net.horizonsend.ion.server.features.client.whereisit.mod.FoundS2C
import net.horizonsend.ion.server.features.client.whereisit.mod.SearchC2S
import net.horizonsend.ion.server.features.client.whereisit.mod.Searcher
import net.horizonsend.ion.server.features.customitems.CustomItems
import net.horizonsend.ion.server.features.space.encounters.Encounters
import net.horizonsend.ion.server.features.space.generation.SpaceGenerationManager
import net.horizonsend.ion.server.features.space.generation.generators.SpaceBiomeProvider
import net.horizonsend.ion.server.features.space.generation.generators.SpaceChunkGenerator
import net.horizonsend.ion.server.miscellaneous.*
import net.horizonsend.ion.server.miscellaneous.events.IonDisableEvent
import net.horizonsend.ion.server.miscellaneous.events.IonEnableEvent
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.FriendlyByteBuf
import net.starlegacy.feature.economy.city.CityNPCs
import net.starlegacy.feature.economy.collectors.Collectors
import net.starlegacy.feature.hyperspace.HyperspaceBeacons
import net.starlegacy.feature.machine.AreaShields
import net.starlegacy.feature.nations.NationsMap
import net.starlegacy.feature.space.SpaceMap
import net.starlegacy.feature.starship.hyperspace.HyperspaceMap
import net.starlegacy.legacyDisable
import net.starlegacy.legacyEnable
import net.starlegacy.util.Notify
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.java.JavaPlugin

object IonServer : JavaPlugin() {
	var balancing: BalancingConfiguration = Configuration.load(dataFolder, "balancing.json")
	var configuration: ServerConfiguration = Configuration.load(dataFolder, "server.json")

	override fun onEnable() {
		val exception = runCatching(::internalEnable).exceptionOrNull() ?: return
		slF4JLogger.error("An exception occurred during plugin startup! The server will now exit.", exception)
		Bukkit.shutdown()
	}

	private fun internalEnable() {
		PacketType.values().forEach { ProtocolLibrary.getProtocolManager().addPacketListener(IonPacketListener(it)) }

		prefixProvider = {
			when (it) {
				is Player -> "to ${it.name}: "
				else -> ""
			}
		}

		val pluginManager = server.pluginManager

		// Commands
		val commandManager = PaperCommandManager(this)

		@Suppress("Deprecation")
		commandManager.enableUnstableAPI("help")

		for (command in commands) {
			commandManager.registerCommand(command)

			if (command is Listener) {
				Bukkit.getPluginManager().registerEvents(command, this)
			}
		}

		commandManager.commandCompletions.registerStaticCompletion(
			"achievements",
			Achievement.values().map { it.name }
		)

		commandManager.commandCompletions.registerCompletion("customItem") { context ->
			CustomItems.identifiers.filter { context.player.hasPermission("ion.customitem.$it") }
		}
		commandManager.commandCompletions.registerCompletion("wreckEncounters") { Encounters.identifiers }
		commandManager.commandCompletions.registerCompletion("particles") { context ->
			BuiltInRegistries.PARTICLE_TYPE.keySet()
				.filter { context.player.hasPermission("ion.settings.particle.$it") }
				.map { "$it" }
		}
		commandManager.commandCompletions.registerCompletion("hyperspaceGates") {
			configuration.beacons.map { it.name.replace(" ", "_") }
		}

		// The listeners are defined in a separate file for the sake of keeping the main class clean.
		for (listener in listeners) pluginManager.registerEvents(listener, this)

		Bukkit.getPluginManager().callEvent(IonEnableEvent(commandManager))

		// WIT networking
		Bukkit.getMessenger().registerIncomingPluginChannel(this, SearchC2S.ID.toString(), Searcher::handle)
		Bukkit.getMessenger().registerOutgoingPluginChannel(this, FoundS2C.ID.toString())

		// Void networking
		for (packet in Packets.values()) {
			logger.info("Registering ${packet.id}")

			Bukkit.getMessenger().registerOutgoingPluginChannel(this, packet.id.toString())
			Bukkit.getMessenger().registerIncomingPluginChannel(
				this,
				packet.id.toString()
			) { s: String, player: Player, bytes: ByteArray ->
				logger.info("Received message on $s by ${player.name}")
				val buf = FriendlyByteBuf(Unpooled.wrappedBuffer(bytes))
				packet.handler.c2s(buf, player)
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
			Runnable
			{
				SpaceMap.onEnable()
				NationsMap.onEnable()
				HyperspaceMap.onEnable()
				HyperspaceBeacons.reloadDynmap()
				Collectors.onEnable()
				CityNPCs.onEnable()
				ShipData.enable()
				AreaShields.loadData()

				pluginManager.registerEvents(CityNPCs, this)

				commandManager.commandCompletions.registerCompletion("wreckSchematics") { context ->
					SpaceGenerationManager.getGenerator(
						(context.player.world as CraftWorld).handle
					)?.schematicMap?.keys
				}

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
