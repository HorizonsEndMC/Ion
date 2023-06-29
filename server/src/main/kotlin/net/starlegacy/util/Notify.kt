package net.starlegacy.util

import github.scarsz.discordsrv.DiscordSRV
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel
import java.util.UUID
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.horizonsend.ion.server.IonComponent
import net.starlegacy.cache.nations.PlayerCache
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.schema.nations.Nation
import net.horizonsend.ion.server.database.schema.nations.Settlement
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getPluginManager
import org.litote.kmongo.id.WrappedObjectId

object Notify : IonComponent() {
	infix fun online(message: Component) {
		notifyOnlineAction(MiniMessage.miniMessage().serialize(message))
		globalChannel(PlainTextComponentSerializer.plainText().serialize(message))
	}

	private val notifyOnlineAction = { message: String ->
		Bukkit.broadcast(MiniMessage.miniMessage().deserialize(message))
	}.registerRedisAction("notify-online", runSync = false)

	infix fun all(message: Component) {
		online(message)
		eventsChannel(PlainTextComponentSerializer.plainText().serialize(message))
	}

	fun player(player: UUID, message: Component) {
		notifyPlayerAction(player to MiniMessage.miniMessage().serialize(message))
	}

	private val notifyPlayerAction = { (uuid, message): Pair<UUID, String> ->
		Bukkit.getPlayer(uuid)?.sendMessage(MiniMessage.miniMessage().deserialize(message))
	}.registerRedisAction("notify-player", runSync = false)

	fun settlement(settlementId: Oid<Settlement>, message: String) {
		notifySettlementAction(settlementId.toString() to "&3$message".colorize())
	}

	private val notifySettlementAction = { (idString, message): Pair<String, String> ->
		val id: Oid<Settlement> = WrappedObjectId(idString)
		Bukkit.getOnlinePlayers()
			.filter { PlayerCache[it].settlementOid == id }
			.forEach { it.sendMessage(message) }
	}.registerRedisAction("notify-settlement", runSync = false)

	fun nation(nationId: Oid<Nation>, message: String) {
		notifyNationAction(nationId.toString() to "&6$message".colorize())
	}

	private val notifyNationAction = { (idString, message): Pair<String, String> ->
		val id: Oid<Nation> = WrappedObjectId(idString)
		Bukkit.getOnlinePlayers()
			.filter { PlayerCache[it].nationOid == id }
			.forEach { it.sendMessage(message) }
	}.registerRedisAction("notify-nation", runSync = false)

	infix fun eventsChannel(message: String) {
		if (getPluginManager().isPluginEnabled("DiscordSRV")) {
			Tasks.async {
				val channel: TextChannel? = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("events")

				if (channel == null) {
					System.err.println("ERROR: No events channel found!")
					return@async
				}

				channel.sendMessage(message).queue()
			}
		}
	}

	infix fun globalChannel(message: String) {
		if (getPluginManager().isPluginEnabled("DiscordSRV")) {
			Tasks.async {
				val channel: TextChannel? = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("global")

				if (channel == null) {
					System.err.println("ERROR: No events channel found!")
					return@async
				}

				channel.sendMessage("**$message**").queue()
			}
		}
	}

	override fun supportsVanilla(): Boolean {
		return true
	}
}
