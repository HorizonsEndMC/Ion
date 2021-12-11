package net.starlegacy.command.misc

import net.starlegacy.cache.nations.NationCache
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.command.SLCommand
import net.starlegacy.database.Oid
import net.starlegacy.database.schema.nations.Nation
import net.starlegacy.feature.progression.Levels
import net.starlegacy.feature.progression.SLXP
import co.aikar.commands.annotation.CommandAlias
import net.starlegacy.util.msg
import net.starlegacy.util.multimapOf
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object ListCommand : SLCommand() {
    @CommandAlias("list|who")
    fun execute(sender: CommandSender) {
        val players: Collection<Player> = Bukkit.getOnlinePlayers()

        if (players.isEmpty()) {
            sender msg "&c&oNo players online"
            return
        }

        val nationMap = multimapOf<Oid<Nation>?, Player>()

        for (player in players) {
            val playerNation: Oid<Nation>? = PlayerCache[player].nation
            nationMap[playerNation].add(player)
        }

        val nationIdsSortedByName: List<Oid<Nation>?> = nationMap.keySet()
            .sortedBy { id -> id?.let { NationCache[it].name } ?: "_" }

        for (nationId: Oid<Nation>? in nationIdsSortedByName) {
            val members: Collection<Player> = nationMap[nationId].sortedBy { SLXP[it] }

            val nationText = nationId?.let { "&5${NationCache[it].name}" } ?: "&e&oNationless"

            sender msg "$nationText &8&l:(&d${members.count()}&8&l):&7 ${members.joinToString { player ->
                val xpPrefix = Levels.toArabicNumeral(Levels[player])
                val nationPrefix = PlayerCache[player].nationTag?.let { "&r$it " } ?: ""
                return@joinToString "&7[&b&l$xpPrefix&7] $nationPrefix&7${player.name}"
            }}"
        }
    }
}
