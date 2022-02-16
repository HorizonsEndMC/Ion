package net.starlegacy.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Subcommand
import java.util.UUID
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.command.SLCommand
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.schema.nations.NationRelation
import net.starlegacy.database.slPlayerId
import net.starlegacy.feature.multiblock.defenseturret.APTurret
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.feature.nations.region.types.RegionSpaceStation
import net.starlegacy.feature.nations.region.types.RegionTerritory
import net.starlegacy.util.msg
import org.bukkit.entity.Player

@CommandAlias("ap")
object APCommand : SLCommand() {
	@Subcommand("add|set|a|+")
	@CommandCompletion("@players")
	fun onAdd(sender: Player, target: String) = asyncCommand(sender) {
		val playerId: UUID = resolveOfflinePlayer(target)
		val slPlayer = SLPlayer[playerId.slPlayerId] ?: fail {
			"Player not in database"
		}

		val targetSettlement = slPlayer.settlement
		failIf(targetSettlement != null && PlayerCache[sender].settlement == targetSettlement) {
			"Cannot target settlement members"
		}

		val targetNation = slPlayer.nation
		failIf(targetNation != null && getRelation(sender, targetNation) >= NationRelation.Level.ALLY) {
			"Cannot target allies"
		}

		for (region in Regions.find(sender.location)) {
			if (region !is RegionTerritory && region !is RegionSpaceStation) {
				continue
			}

			if (region.getInaccessMessage(sender) != null) {
				sender msg "You need build access to control targets"
				continue
			}

			if (!APTurret.regionalTargets[region.id].add(playerId)) {
				sender msg "&c$target was already targeted"
				continue
			}

			sender msg "&aTargeted $target"

			APTurret.regionalTargets[region.id].add(playerId)
		}
	}

	@Subcommand("remove|unset|r|-")
	@CommandCompletion("@players")
	fun onRemove(sender: Player, target: String) = asyncCommand(sender) {
		val playerId: UUID = resolveOfflinePlayer(target)

		for (region in Regions.find(sender.location)) {
			if (region !is RegionTerritory && region !is RegionSpaceStation) {
				continue
			}

			if (region.getInaccessMessage(sender) != null) {
				sender msg "You need build access to control targets"
				continue
			}

			if (!APTurret.regionalTargets[region.id].remove(playerId)) {
				sender msg "&c$target was not targeted"
				continue
			}

			sender msg "&aUn-targeted $target"
		}
	}
}
