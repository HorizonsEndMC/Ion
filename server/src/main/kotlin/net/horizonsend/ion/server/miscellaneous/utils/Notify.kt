package net.horizonsend.ion.server.miscellaneous.utils

import github.scarsz.discordsrv.DiscordSRV
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getPluginManager
import org.litote.kmongo.id.WrappedObjectId
import java.util.UUID

object Notify : IonServerComponent() {
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

	fun settlement(settlementId: Oid<Settlement>, message: Component) {
		notifySettlementAction(settlementId.toString() to MiniMessage.miniMessage().serialize(message))
	}

	private val notifySettlementAction = { (idString, message): Pair<String, String> ->
		val id: Oid<Settlement> = WrappedObjectId(idString)
		Bukkit.getOnlinePlayers()
			.filter { PlayerCache[it].settlementOid == id }
			.forEach { it.sendMessage(MiniMessage.miniMessage().deserialize(message)) }
	}.registerRedisAction("notify-settlement", runSync = false)

	fun nation(nationId: Oid<Nation>, message: Component) {
		notifyNationAction(nationId.toString() to MiniMessage.miniMessage().serialize(message))
	}

	private val notifyNationAction = { (idString, message): Pair<String, String> ->
		val id: Oid<Nation> = WrappedObjectId(idString)
		Bukkit.getOnlinePlayers()
			.filter { PlayerCache[it].nationOid == id }
			.forEach { it.sendMessage(MiniMessage.miniMessage().deserialize(message)) }
	}.registerRedisAction("notify-nation", runSync = false)

	/** Must be registered in the CiscordSRV config **/
	fun getChannel(name: String): TextChannel? {
		if (!getPluginManager().isPluginEnabled("DiscordSRV")) {
			log.warn("DiscordSRV not enabled! Ignoring message.")
			return null
		}

		return DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(name)
	}

	fun sendDiscord(channel: TextChannel?, message: String) {
		if (!getPluginManager().isPluginEnabled("DiscordSRV")) return
		if (channel == null) {
			log.error("ERROR: No channel found!")
			return
		}

		Tasks.async {
			channel.sendMessage(message).queue()
		}
	}

	infix fun eventsChannel(message: String) = sendDiscord(getChannel("events"), message)

	infix fun globalChannel(message: String) = sendDiscord(getChannel("global"), message)

	infix fun changelogChannel(message: String) = sendDiscord(getChannel("changelog"), message)

}
