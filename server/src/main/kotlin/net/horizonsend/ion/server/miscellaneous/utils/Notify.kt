package net.horizonsend.ion.server.miscellaneous.utils

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.common.utils.redis.messaging.Notifications
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.misc.messaging.ServerDiscordMessaging
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.litote.kmongo.id.WrappedObjectId
import java.util.UUID

object Notify : Notifications() {
	fun chatAndGlobal(message: Component) {
		notifyOnlineAction(message)
		ServerDiscordMessaging.globalEmbed(Embed(title = message.plainText(), color = message.color()?.value()))
	}

	override val notifyOnlineAction = { message: Component ->
		Bukkit.broadcast(message)
	}.createRedisAction("notify-online", runSync = false)

	fun chatAndEvents(message: Component) {
		chatAndGlobal(message)
		ServerDiscordMessaging.eventsMessage(message)
	}

	fun playerCrossServer(player: UUID, message: Component) = notifyPlayerAction(player to message)

	override val notifyPlayerAction = { (uuid, message): Pair<UUID, Component> ->
		Bukkit.getPlayer(uuid)?.sendMessage(message)
	}.createRedisAction("notify-player", runSync = false)

	fun settlementCrossServer(settlementId: Oid<Settlement>, message: Component) = notifySettlementAction(settlementId.toString() to message)

	override val notifySettlementAction = { (idString, message): Pair<String, Component> ->
		val id: Oid<Settlement> = WrappedObjectId(idString)
		Bukkit.getOnlinePlayers()
			.filter { PlayerCache[it].settlementOid == id }
			.forEach { it.sendMessage(message) }
	}.createRedisAction("notify-settlement", runSync = false)

	fun nationCrossServer(nationId: Oid<Nation>, message: Component) = notifyNationAction(nationId.toString() to message)

	override val notifyNationAction = { (idString, message): Pair<String, Component> ->
		val id: Oid<Nation> = WrappedObjectId(idString)

		Bukkit.getOnlinePlayers()
			.filter { PlayerCache[it].nationOid == id }
			.forEach { it.sendMessage(message) }
	}.createRedisAction("notify-nation", runSync = false)
}
