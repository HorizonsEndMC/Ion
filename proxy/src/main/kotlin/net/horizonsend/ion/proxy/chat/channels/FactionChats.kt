package net.horizonsend.ion.proxy.chat.channels

import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.proxy.Player
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.database.schema.nations.NationRelation
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.miniMessage
import net.horizonsend.ion.common.utils.luckPerms
import net.horizonsend.ion.proxy.chat.Channel
import net.horizonsend.ion.proxy.chat.ChannelManager
import net.horizonsend.ion.proxy.features.cache.PlayerCache
import net.horizonsend.ion.proxy.utils.isMuted
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage

class SettlementChat : Channel {
	override val name = "settlement"
	override val prefix = "<dark_aqua><bold>Settlement</bold> "
	override val displayName = "<aqua>Settlement"
	override val commands = listOf("schat", "sc", "settlementchat")
	override val color = NamedTextColor.AQUA
	override val checkPermission = false

	override fun receivers(player: Player): List<Player> {
		return super.receivers(player).filter { PlayerCache[it].settlementOid == PlayerCache[player].settlementOid }
	}

	override fun processMessage(player: Player, e: PlayerChatEvent): Boolean {
		val roleString = PlayerCache[player].settlementTag?.let { " $it " } ?: ""

		if (player.isMuted())
			return false

		PlayerCache[player].settlementOid ?: run {
			player.userError("You're not in a settlement! &o(Hint: To get back to global, use /global)")
			return false
		}

		val user = luckPerms.userManager.getUser(player.uniqueId)

		receivers(player).forEach {
			it.sendMessage(
				(
					prefix +
						"<reset>" + roleString +
						"<aqua>${player.username}</aqua>" +
						(user?.cachedData?.metaData?.suffix ?: " ") +
						"<dark_gray>»</dark_gray> "
					).miniMessage().hoverEvent(ChannelManager.playerInfo(player)).append(
						if (player.hasPermission("ion.minimessage"))
							e.message.miniMessage().color(color)
						else
							Component.text(
								MiniMessage.miniMessage()
									.stripTags(e.message)
							).color(color)
					)
			)
		}

		return false
	}
}

class NationChat : Channel {
	override val name = "nation"
	override val prefix = "<green><bold>Nation "
	override val displayName = "<green>Nation"
	override val commands = listOf("nchat", "nc", "nationchat")
	override val color = NamedTextColor.GREEN
	override val checkPermission = false

	override fun receivers(player: Player): List<Player> {
		return super.receivers(player).filter { PlayerCache[it].nationOid == PlayerCache[player].nationOid }
	}

	override fun processMessage(player: Player, e: PlayerChatEvent): Boolean {
		val playerData = PlayerCache[player]
		val settlement = playerData.settlementOid ?: run {
			player.userError("You're not in a settlement! &o(Hint: To get back to global, use /global)")
			return false
		}

		playerData.nationOid ?: run {
			player.userError("&cYou're not in a nation! &o(Hint: To get back to global, use /global)")
			return false
		}

		if (player.isMuted())
			return false

		val settlementName = SettlementCache[settlement].name
		val roleString = playerData.nationTag?.let { " $it " } ?: " "

		receivers(player).forEach {
			it.sendMessage(
				(
					prefix +
						"<reset>" +
						"<aqua>${settlementName}</aqua>" +
						roleString +
						"<red>${player.username}</red> " +
						"<dark_gray>»</dark_gray> "
					).miniMessage().hoverEvent(ChannelManager.playerInfo(player)).append(
						if (player.hasPermission("ion.minimessage"))
							e.message.miniMessage().color(color)
						else
							Component.text(
								MiniMessage.miniMessage()
									.stripTags(e.message)
							).color(color)
					)
			)
		}

		return false
	}
}

class AllyChat : Channel {
	override val name = "ally"
	override val prefix = "<dark_purple><bold>Ally "
	override val displayName = "<dark_purple>Ally"
	override val commands = listOf("achat", "ac", "allychat")
	override val color = NamedTextColor.LIGHT_PURPLE
	override val checkPermission = false

	override fun receivers(player: Player): List<Player> {
		return super.receivers(player)
			.filter {
				NationRelation.getRelationActual(PlayerCache[it].nationOid ?: return@filter false,
					PlayerCache[player].nationOid ?: return@filter false).ordinal >= NationRelation.Level.ALLY.ordinal
			}
	}

	override fun processMessage(player: Player, e: PlayerChatEvent): Boolean {
		val playerData = PlayerCache[player]
		val nation = playerData.nationOid ?: run {
			player.userError("You're not in a nation! (Hint: To get back to global, use /global)")
			return false
		}

		if (player.isMuted())
			return false

		val nationName = NationCache[nation].name
		val roleString = playerData.nationTag?.let { " $it " } ?: " "

		receivers(player).forEach {
			it.sendMessage(
				(
					prefix +
						"<reset>" +
						"<yellow>${nationName}</yellow>" +
						roleString +
						"<aqua>${player.username}</aqua> " +
						"<dark_gray>»</dark_gray> "
					).miniMessage().hoverEvent(ChannelManager.playerInfo(player)).append(
						if (player.hasPermission("ion.minimessage"))
							e.message.miniMessage().color(color)
						else
							Component.text(
								MiniMessage.miniMessage()
									.stripTags(e.message)
							).color(color)
					)
			)
		}

		return false
	}
}
