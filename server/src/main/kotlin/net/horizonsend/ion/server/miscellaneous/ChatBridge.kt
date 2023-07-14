package net.horizonsend.ion.server.miscellaneous

import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.database.schema.nations.NationRelation
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.miniMessage
import net.horizonsend.ion.common.utils.luckPerms
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.starlegacy.SETTINGS
import net.starlegacy.feature.progression.Levels
import net.starlegacy.feature.progression.SLXP
import net.starlegacy.feature.space.Space
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

object ChatBridge : IonServerComponent() {
	private val distanceSquared = SETTINGS.chat.localDistance * SETTINGS.chat.localDistance

	override fun onEnable() {
		{ (uuid, message): Pair<UUID, String> ->
			val player = Bukkit.getPlayer(uuid)

			if (player != null)
			for (other in player.world.players) {
				if (other.location.distanceSquared(player.location) <= distanceSquared) {
					msg("<yellow><bold>Local", NamedTextColor.YELLOW, message, player, other)
				}
			}
		}.registerRedisAction("local-chat");

		{ (uuid, message): Pair<UUID, String> ->
			val player = Bukkit.getPlayer(uuid)

			if (player != null)
			if (Space.getPlanet(player.world) == null) {
				player.userError("You're not on a planet! To go back to global chat, use /global")
			} else for (other in player.world.players) {
				msg("<blue><bold>Planet", NamedTextColor.DARK_GREEN, message, player, other)
			}
		}.registerRedisAction("planet-chat", true)
	}

	fun msg(prefix: String, color: TextColor, message: String, player: Player, other: Player) {
		val user = luckPerms.userManager.getUser(player.uniqueId)
		val userNation = PlayerCache[player].nationOid
		val relationColor =
			userNation?.let { user ->
				PlayerCache[other].nationOid?.let { RelationCache[it, user].textStyle }
			} ?: NationRelation.Level.NONE.textStyle

		other.sendMessage(
			(
				prefix +
					"<reset><${relationColor}>${userNation?.let { " " + NationCache[it].name.capitalize() + " " } ?: ""}</$relationColor>" +
					(user?.cachedData?.metaData?.prefix ?: "") +
					"<dark_gray>[<aqua>${PlayerCache[player].level}<dark_gray>] " + "<white>${player.name}</white>" +
					(user?.cachedData?.metaData?.suffix ?: " ") +
					"<dark_gray>»</dark_gray> "
				).miniMessage().hoverEvent(playerInfo(player)).append(
					if (player.hasPermission("ion.minimessage"))
						message.miniMessage().color(color)
					else
						text(
							MiniMessage.miniMessage()
								.stripTags(message)
						).color(color)
				)
		)
	}

	fun playerInfo(player: Player) = HoverEvent.showText(
		text(
			"""
			Level: ${Levels[player]}
			XP: ${SLXP[player]}
			Nation: ${PlayerCache[player].nationOid?.let(NationCache::get)?.name}
			Settlement: ${PlayerCache[player].settlementOid?.let(SettlementCache::get)?.name}
			Player: ${player.name}
			""".trimIndent()
		)
	)
}
