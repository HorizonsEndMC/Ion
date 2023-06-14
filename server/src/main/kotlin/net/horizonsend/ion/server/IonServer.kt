package net.horizonsend.ion.server

import co.aikar.commands.PaperCommandManager
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import github.scarsz.discordsrv.DiscordSRV
import io.netty.buffer.Unpooled
import net.horizonsend.ion.common.Configuration
import net.horizonsend.ion.common.Connectivity
import net.horizonsend.ion.common.database.Cryopod
import net.horizonsend.ion.common.database.Nation
import net.horizonsend.ion.common.database.PlayerAchievement
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.common.database.enums.Achievement
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
import net.starlegacy.database.Oid
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.schema.nations.Settlement
import net.starlegacy.database.slPlayerId
import net.starlegacy.feature.economy.city.CityNPCs
import net.starlegacy.feature.economy.collectors.Collectors
import net.starlegacy.feature.hyperspace.HyperspaceBeacons
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
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.litote.kmongo.addToSet
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

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
		Connectivity.open(dataFolder)

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

		// Temporary Migration Code
		transaction {
			if (net.starlegacy.database.schema.nations.Nation.all().isNotEmpty()) return@transaction
			if (net.horizonsend.ion.server.database.schema.Cryopod.all().isNotEmpty()) return@transaction

			val sqlNations = Nation
			val mongoNation = net.starlegacy.database.schema.nations.Nation

			for (sqlNation in sqlNations.all()) {
				mongoNation.create(
					sqlNation.name,
					sqlNation.capital as Oid<Settlement>,
					sqlNation.color
				)
			}

			for (cryopod in Cryopod.all()) {
				val owner = SLPlayer[cryopod.owner.id.value] ?: continue

				val newpod = net.horizonsend.ion.server.database.schema.Cryopod.create(
					SLPlayer.findById(cryopod.owner.id.value.slPlayerId)!!,
					cryopod.location.vec3i(),
					cryopod.location.world
				)

				SLPlayer.updateById(owner._id, addToSet(SLPlayer::cryopods, newpod))
			}

			for (playerAchievement in PlayerAchievement.all()) {
				val owner = SLPlayer[playerAchievement.player.id.value] ?: continue

				SLPlayer.updateById(owner._id, addToSet(SLPlayer::achievements, playerAchievement.achievement))
			}

			for (sqlPlayer in PlayerData.all()) {
				val mongoPlayer = SLPlayer[sqlPlayer.id.value] ?: continue
				val selectedCryo = net.horizonsend.ion.server.database.schema.Cryopod.findOne(and(net.horizonsend.ion.server.database.schema.Cryopod::owner eq mongoPlayer._id, net.horizonsend.ion.server.database.schema.Cryopod::active eq true))

				SLPlayer.updateById(mongoPlayer._id, setValue(SLPlayer::selectedCryopod, selectedCryo?._id))
			}
		}
	}

	override fun onDisable() {
		Bukkit.getPluginManager().callEvent(IonDisableEvent())
		IonWorld.unregisterAll()
		legacyDisable()
		CombatNPCs.npcToPlayer.values.firsts().forEach(CombatNPCs::destroyNPC)
		Connectivity.close()
	}

	override fun getDefaultBiomeProvider(worldName: String, id: String?): BiomeProvider {
		return SpaceBiomeProvider()
	}

	override fun getDefaultWorldGenerator(worldName: String, id: String?): ChunkGenerator {
		return SpaceChunkGenerator()
	}
}
