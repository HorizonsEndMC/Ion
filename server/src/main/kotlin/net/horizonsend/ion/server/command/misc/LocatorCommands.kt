package net.horizonsend.ion.server.command.misc

import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.schema.nations.NationRelation
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_BLUE
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.join
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.starship.hyperspace.HyperspaceBeaconManager
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.listen
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID

object LocatorCommands : SLCommand() {
	@CommandAlias("getpos")
	@CommandCompletion("@players")
	@Suppress("Unused")
	fun onGetPos(sender: CommandSender, name: String) = asyncCommand(sender) {
		val target = Bukkit.getPlayer(name) ?: fail { "Player $name not found!" }

		failIf(target.hasPermission("group.dutymode")) { "Player $name not found!" }

		var relation: NationRelation.Level = NationRelation.Level.NONE
		var distance = 0.0

		// All other conditions the sender is console / a command block
		if (sender is Player) {
			val targetNation = PlayerCache[target].nationOid

			PlayerCache[sender].nationOid?.let {
				if (targetNation != null) relation = RelationCache[it, targetNation]
			}

			distance = if (sender.world != target.world) {
				failIf(relation < NationRelation.Level.ALLY) {
					"You need to be closer to ${target.name} to do that!"
				}

				0.0
			} else sender.location.distance(target.location)

			val gates = HyperspaceBeaconManager.beaconWorlds[target.world]
			val gateDistance = gates?.let { it.minOfOrNull { gate -> gate.spaceLocation.toLocation().distance(target.location) } }

			println("Relation: $relation")

			if (relation < NationRelation.Level.ALLY) failIf(
				distance > ConfigurationFiles.serverConfiguration().getPosMaxRange &&
				(gateDistance != null && gateDistance > 2000)
			) {
				"You need to be closer to ${target.name} to do that!"
			}
		}

		sender.sendMessage(ofChildren(
			text(target.name, relation.color), text("'s position", HE_MEDIUM_GRAY), newline(),
			text("World: ", HE_LIGHT_GRAY), text(target.world.name, HE_LIGHT_BLUE), newline(),
			text("X: ", HE_LIGHT_GRAY), text(target.location.blockX, HE_LIGHT_BLUE), newline(),
			text("Y: ", HE_LIGHT_GRAY), text(target.location.blockY, HE_LIGHT_BLUE), newline(),
			text("Z: ", HE_LIGHT_GRAY), text(target.location.blockZ, HE_LIGHT_BLUE), newline(),
			text("Yaw: ", HE_LIGHT_GRAY), text(target.location.yaw.toDouble().roundToHundredth(), HE_LIGHT_BLUE), newline(),
			text("Pitch: ", HE_LIGHT_GRAY), text(target.location.pitch.toDouble().roundToHundredth(), HE_LIGHT_BLUE)
		))
	}

	@CommandAlias("near")
	@Suppress("Unused")
	fun onNear(sender: Player) {
		val players = sender.location.getNearbyPlayers(ConfigurationFiles.serverConfiguration().nearMaxRange) {
			it.uniqueId != sender.uniqueId
		}.toList()

		if (players.isEmpty()) {
			sender.information("There are no nearby players")

			return
		}

		val senderNation = PlayerCache[sender].nationOid

		val count = players.count()

		val body = players.mapNotNull { player ->
			val cached = PlayerCache.getIfOnline(player) ?: return@mapNotNull null
			if (player.hasPermission("group.dutymode")) return@mapNotNull null
			val nation = cached.nationOid

			var nameColor = NamedTextColor.WHITE

			if (senderNation != null && nation != null) {
				nameColor = RelationCache[senderNation, nation].color
			}

			val distance = player.location.distance(sender.location)

			ofChildren(text(player.name, nameColor), text(": ", HE_DARK_GRAY), text(distance.roundToHundredth(), HE_LIGHT_GRAY))
		}.join(separator = newline())

		sender.sendMessage(ofChildren(text("Nearby Players:", HE_MEDIUM_GRAY), if (count == 0) empty() else newline(), body))
	}

	private val autoNearPlayers = mutableListOf<UUID>()

	override fun onEnable(manager: PaperCommandManager) {
		Tasks.syncRepeat(600L, 600L) {
			for (player in autoNearPlayers.mapNotNull(Bukkit::getPlayer)) {
				onNear(player)
			}
		}

		listen<PlayerQuitEvent> { event ->
			autoNearPlayers.remove(event.player.uniqueId)
		}
	}

	@CommandAlias("autonear")
	@Suppress("Unused")
	fun onAutoNear(sender: Player) {
		if (autoNearPlayers.contains(sender.uniqueId)) {
			autoNearPlayers.remove(sender.uniqueId)

			sender.success("Stopppd auto near. Run this command again to enable it")
			return
		}

		sender.success("Started auto near. Run this command again to disable it")
		autoNearPlayers.add(sender.uniqueId)
	}
}
