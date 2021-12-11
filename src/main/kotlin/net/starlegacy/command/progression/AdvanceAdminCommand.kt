package net.starlegacy.command.progression

import com.google.gson.Gson
import net.starlegacy.command.SLCommand
import net.starlegacy.database.schema.economy.CargoCrateShipment
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.schema.misc.SLPlayerId
import net.starlegacy.database.slPlayerId
import net.starlegacy.database.uuid
import net.starlegacy.feature.economy.cargotrade.EvilShipmentDrainer
import net.starlegacy.feature.economy.cargotrade.EvilShipmentDrainer.drain
import net.starlegacy.feature.progression.Levels
import net.starlegacy.feature.progression.PlayerXPLevelCache
import net.starlegacy.feature.progression.SLXP
import net.starlegacy.feature.progression.advancement.Advancements
import co.aikar.commands.ConditionFailedException
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.*
import net.starlegacy.util.SLTextStyle
import net.starlegacy.util.green
import net.starlegacy.util.msg
import net.starlegacy.util.toCreditsString
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import java.io.File
import java.io.FileReader
import java.util.*
import kotlin.math.abs
import kotlin.math.pow

/**
 * Admin only commands for manipulating player Advance data
 */
@CommandAlias("advanceadmin")
@CommandPermission("advance.admin")
object AdvanceAdminCommand : SLCommand() {
    @Subcommand("xp get")
    @CommandCompletion("@players")
    fun onXPGet(sender: CommandSender, player: String) = asyncCommand(sender) {
        val playerId: UUID = resolveOfflinePlayer(player)

        val xp: Int = SLPlayer.getXP(playerId.slPlayerId) ?: throw InvalidCommandArgument("Player not stored")

        sender msg green("$player has $xp XP")

        Bukkit.getPlayer(playerId)?.let {
            val cached: PlayerXPLevelCache.CachedAdvancePlayer = PlayerXPLevelCache[playerId]
                ?: throw ConditionFailedException("$player has no cache!")

            if (cached.xp != xp) {
                throw ConditionFailedException("$player's cached XP is ${cached.xp} instead of $xp")
            }
        }
    }

    @Subcommand("xp give")
    @CommandCompletion("@players @nothing")
    fun onXPGive(sender: CommandSender, player: String, amount: Int) = asyncCommand(sender) {
        val playerId: UUID = resolveOfflinePlayer(player)

        // If it's a negative amount, we need to make sure we're not accidentally giving them negative XP
        val oldXP: Int = PlayerXPLevelCache.fetchSLXP(playerId)
        if (oldXP + amount < 0) {
            throw InvalidCommandArgument("$player does not have ${abs(amount)} XP, only $oldXP XP")
        }

        PlayerXPLevelCache.addSLXP(playerId, amount)

        val newXP: Int = PlayerXPLevelCache.fetchSLXP(playerId)
        sender msg green("Gave $amount XP to $player. Now they have $newXP XP.")
    }

    @Subcommand("xp set")
    @CommandCompletion("@players @nothing")
    fun onXPSet(sender: CommandSender, player: String, amount: Int) = asyncCommand(sender) {
        val playerId = resolveOfflinePlayer(player)
        val oldXP = PlayerXPLevelCache.fetchSLXP(playerId)
        SLXP.setAsync(playerId, amount)
        sender msg green("Changed $player's XP from $oldXP to $amount.")
    }

    @Subcommand("rebalance")
    @Description("Reload the levels config")
    fun onRebalance(sender: CommandSender) {
        Levels.reloadConfig()
        Advancements.reloadConfig()
        sender msg green("Reloaded level & advancement balancing configs")
    }

    @Subcommand("level get")
    @CommandCompletion("@players")
    fun onLevelGet(sender: CommandSender, player: String) = asyncCommand(sender) {
        val playerId = resolveOfflinePlayer(player)

        val level: Int = SLPlayer.getLevel(playerId.slPlayerId) ?: throw InvalidCommandArgument("Player not stored")

        sender msg green("$player's level is $level")

        Bukkit.getPlayer(playerId)?.let {
            val cached = PlayerXPLevelCache[playerId] ?: throw ConditionFailedException("$player has no cache!")
            if (cached.level != level) throw ConditionFailedException("$player's cached level is ${cached.level} instead of $level")
        }
    }

    @Subcommand("level set")
    @CommandCompletion("@players @levels")
    fun onLevelSet(sender: CommandSender, player: String, level: Int) = asyncCommand(sender) {
        val playerId: UUID = resolveOfflinePlayer(player)
        val oldLevel: Int = PlayerXPLevelCache.fetchLevel(playerId)
        PlayerXPLevelCache.setLevel(playerId, level)
        sender msg green("Changed $player's level from $oldLevel to $level.")
    }

    @Subcommand("listplayers")
    fun onListPlayers(sender: CommandSender) = asyncCommand(sender) {
        val ids: List<UUID> = SLPlayer.all().map { it._id.uuid }.toList()

        val text = ids.joinToString { id: UUID ->
            val player = Bukkit.getOfflinePlayer(id) ?: return@joinToString SLTextStyle.RED.toString() + id.toString()
            val color = if (player.isOnline) SLTextStyle.GREEN else SLTextStyle.GRAY
            return@joinToString color.toString() + player.name
        }

        sender msg text
    }

    enum class RankTrack(val refund: Double) { COLONIST(0.8), PRIVATEER(1.0), PIRATE(1.3) }

    data class ProgressionData(val players: Map<UUID, OldPlayerXPLevelCache>) {
        data class OldPlayerXPLevelCache(val track: RankTrack, val level: Int, val points: Int)
    }

    private val progressionData: Map<UUID, ProgressionData.OldPlayerXPLevelCache> by lazy {
        return@lazy FileReader(File(plugin.dataFolder, "progression.json")).use {
            Gson().fromJson(
                it,
                ProgressionData::class.java
            )
        }.players
    }

    private fun getPointsCost(level: Int) = (3.0.pow(level - 1) * 10000).toInt()

    private fun getRefund(level: Int, points: Int) = when (level) {
        0 -> 0
        else -> (1..level).sumBy { getPointsCost(it) }
    } + points

    private const val refundMultiplier = 7.5

    @Subcommand("refund")
    fun onRefund(sender: CommandSender, target: String) = asyncCommand(sender) {
        val playerId = resolveOfflinePlayer(target)

        val data = progressionData[playerId] ?: throw InvalidCommandArgument("No data saved for that player.")

        val refund = getRefund(data.level, data.points)
            .div(refundMultiplier).times(data.track.refund).toInt()

        var remaining = refund
        var level = 1
        while (remaining > 0) {
            val nextLevel = level + 1
            val cost = Levels.getLevelUpCost(nextLevel)
            if (cost > remaining) break
            level = nextLevel
            remaining -= cost
        }

        sender msg "&aThat player would be refunded $refund XP, enough to get to level $level with $remaining XP remaining."
        sender msg "&7&oThey were a level ${data.level} ${data.track.name.toLowerCase()}, with ${data.points} points."
    }

    @Subcommand("giverefund")
    fun onGiveRefund(sender: CommandSender, target: String) = asyncCommand(sender) {
        if (sender !is ConsoleCommandSender) {
            sender msg "lolno"
            return@asyncCommand
        }

        val playerId = resolveOfflinePlayer(target)

        val data = progressionData[playerId] ?: throw InvalidCommandArgument("No data saved for that player.")

        val refund = getRefund(data.level, data.points)
            .div(refundMultiplier).times(data.track.refund).toInt()

        SLXP.addAsync(playerId, refund)
        sender msg "&aRefunded $refund XP"
    }

    @Subcommand("scanabuse")
    fun onScanAbuse(sender: CommandSender) = asyncCommand(sender) {
        val cratesMap = mutableMapOf<SLPlayerId, Int>()
        val creditsMap = mutableMapOf<SLPlayerId, Double>()

        for (shipment in CargoCrateShipment.all()) {
            if (shipment.soldCrates > shipment.totalCrates) {
                val extraCrates = shipment.soldCrates - shipment.totalCrates
                cratesMap[shipment.player] = extraCrates + cratesMap.getOrDefault(shipment.player, 0)
                val extraCredits = shipment.crateRevenue * extraCrates
                creditsMap[shipment.player] = extraCredits + creditsMap.getOrDefault(shipment.player, 0.0)
            }
        }

        for (key in creditsMap.keys.sortedByDescending { cratesMap.getValue(it) }) {
            val extraCredits = creditsMap.getValue(key)
            val extraCrates = cratesMap.getValue(key)
            sender msg "${SLPlayer.getName(key)} has ${extraCredits.toCreditsString()} extra money from ${extraCrates}"
        }
    }

    @Subcommand("evildrain executer")
    fun onEvilDrainExecute(sender: CommandSender, forReal: Boolean) = asyncCommand(sender) {
        try {
            drain(forReal)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Subcommand("evildrain refund")
    fun onEvilDrainRefund(sender: CommandSender, percent: Double) = asyncCommand(sender) {
        try {
            EvilShipmentDrainer.refund(percent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
/*
    @Subcommand("giverefunds")
    fun onGiveRefunds(sender: CommandSender) = asyncCommand(sender) {
        if (sender !is ConsoleCommandSender) {
            sender msg "lolno"
            return@asyncCommand
        }

        val nameMap = progressionData.keys.parallelStream().map { playerID ->
            val result = sql { SLPlayers.select { SLPlayers.id eq playerID }.firstOrNull() }
            var name = result?.get(SLPlayers.lastKnownName)

            if (name == null) {
                val profile = Bukkit.createProfile(playerID)

                if (!profile.completeFromCache()) {
                    sender msg "Failed to lookup username for $playerID!"
                    return@map playerID to playerID.toString()
                }

                name = profile.name!!

                sql {
                    SLPlayers.insert {
                        it[this.id] = EntityID(playerID, SLPlayers)
                        it[this.lastKnownName] = name
                        it[this.lastSeen] = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(700)
                    }
                }

            }

            log.info("$playerID mapped to $name...")

            return@map playerID to name
        }.toList().toMap().filter { it.key.toString() != it.value }


        val refundMap = mutableMapOf<UUID, Int>()

        for ((playerID, data) in progressionData) {
            try {
                if (data.level == 0) continue

                val refund = getRefund(data.level, data.points)
                    .div(refundMultiplier).times(data.track.refund).toInt()

                refundMap[playerID] = refund

            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }

        sender msg "Would refund ${refundMap.size} players"

        sql(unsafe = true) {
            for ((playerID, refund) in refundMap) {
                val name = nameMap[playerID] ?: continue

                AdvancePlayers.update({ AdvancePlayers.player eq playerID }) {
                    with(SqlExpressionBuilder) {
                        it.update(xp, xp + refund)
                    }
                }

                sender msg "Refunded $name $refund SLXP"
            }
        }
    }*/
}
