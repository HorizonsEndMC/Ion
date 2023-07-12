package net.horizonsend.ion.server.miscellaneous

import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.miniMessage
import net.horizonsend.ion.common.utils.luckPerms
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.kyori.adventure.text.minimessage.MiniMessage
import net.starlegacy.SETTINGS
import net.starlegacy.feature.space.Space
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

object ChatBridge : IonServerComponent() {
	private val distanceSquared = SETTINGS.chat.localDistance * SETTINGS.chat.localDistance

	override fun onEnable() {
		{ (uuid, message): Pair<UUID, String> ->
			val player = Bukkit.getPlayer(uuid)!!

			for (other in player.world.players) {
				if (other.location.distanceSquared(player.location) <= distanceSquared) {
					msg("<yellow><bold>Local", "<yellow>", message, player, other)
				}
			}
		}.registerRedisAction("local-chat");

		{ (uuid, message): Pair<UUID, String> ->
			val player = Bukkit.getPlayer(uuid)!!

			if (Space.getPlanet(player.world) == null) {
				player.userError("You're not on a planet! To go back to global chat, use /global")
			} else for (other in player.world.players) {
				msg("<blue><bold>Local", "<dark_green>", message, player, other)
			}
		}.registerRedisAction("planet-chat", true)
	}

	fun msg(prefix: String, color: String, message: String, player: Player, other: Player) {
		val user = luckPerms.userManager.getUser(player.uniqueId)
		val userNation = PlayerCache[player].nationOid!!
		val relationColor =
			RelationCache[PlayerCache[other].nationOid!!, userNation].textStyle

		other.sendMessage(
			(
				"<yellow><bold>Local " +
					"<${relationColor}>${NationCache[userNation].name}</$relationColor> " +
					(user?.cachedData?.metaData?.prefix ?: " ") +
					"<white>${player.name}</white>" +
					"${user?.cachedData?.metaData?.suffix ?: " "} " +
					"<dark_gray>»</dark_gray> " +
					if (player.hasPermission("ion.minimessage")) message else MiniMessage.miniMessage()
						.stripTags(message)
				).miniMessage()
		)
	}
}
