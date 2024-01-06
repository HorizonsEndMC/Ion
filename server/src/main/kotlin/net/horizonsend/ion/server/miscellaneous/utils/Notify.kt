package net.horizonsend.ion.server.miscellaneous.utils

import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.litote.kmongo.id.WrappedObjectId
import java.util.UUID

object Notify : IonComponent() {
	fun chatAndGlobal(message: Component) {
		notifyOnlineAction(message)
		Discord.sendEmbed(IonServer.discordSettings.globalChannel, Embed(description = message.plainText(), color = message.color()?.value()))
	}

	val notifyOnlineAction = { message: Component ->
		Bukkit.broadcast(message)
	}.registerRedisAction("notify-online", runSync = false)

	fun chatAndEvents(message: Component) {
		chatAndGlobal(message)
		Discord.sendMessage(IonServer.discordSettings.eventsChannel, message.plainText())
	}

	fun playerCrossServer(player: UUID, message: Component) = notifyPlayerAction(player to message)

	val notifyPlayerAction = { (uuid, message): Pair<UUID, Component> ->
		Bukkit.getPlayer(uuid)?.sendMessage(message)
	}.registerRedisAction("notify-player", runSync = false)

	fun settlementCrossServer(settlementId: Oid<Settlement>, message: Component) = notifySettlementAction(settlementId.toString() to message)

	val notifySettlementAction = { (idString, message): Pair<String, Component> ->
		val id: Oid<Settlement> = WrappedObjectId(idString)
		Bukkit.getOnlinePlayers()
			.filter { PlayerCache[it].settlementOid == id }
			.forEach { it.sendMessage(message) }
	}.registerRedisAction("notify-settlement", runSync = false)

	fun nationCrossServer(nationId: Oid<Nation>, message: Component) = notifyNationAction(nationId.toString() to message)

	val notifyNationAction = { (idString, message): Pair<String, Component> ->
		val id: Oid<Nation> = WrappedObjectId(idString)

		Bukkit.getOnlinePlayers()
			.filter { PlayerCache[it].nationOid == id }
			.forEach { it.sendMessage(message) }
	}.registerRedisAction("notify-nation", runSync = false)
}
