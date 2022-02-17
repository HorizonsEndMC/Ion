package net.starlegacy.command.progression

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import com.google.gson.Gson
import java.io.File
import java.io.FileReader
import java.util.UUID
import kotlin.math.pow
import net.starlegacy.command.SLCommand
import net.starlegacy.database.schema.economy.CargoCrateShipment
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.schema.misc.SLPlayerId
import net.starlegacy.database.uuid
import net.starlegacy.feature.progression.advancement.Advancements
import net.starlegacy.util.SLTextStyle
import net.starlegacy.util.green
import net.starlegacy.util.msg
import net.starlegacy.util.toCreditsString
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
/**
 * Admin only commands for manipulating player Advance data
 */
@CommandAlias("advanceadmin")
@CommandPermission("advance.admin")
object AdvanceAdminCommand : SLCommand() {
	@Subcommand("rebalance")
	@Description("Reload the levels config")
	fun onRebalance(sender: CommandSender) {
		Advancements.reloadConfig()
		sender msg green("Reloaded level & advancement balancing configs")
	}

	@Subcommand("listplayers")
	fun onListPlayers(sender: CommandSender) = asyncCommand(sender) {
		val ids: List<UUID> = SLPlayer.all().map { it._id.uuid }.toList()

		val text = ids.joinToString { id: UUID ->
			val player = Bukkit.getOfflinePlayer(id)
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
		else -> (1..level).sumOf { getPointsCost(it) }
	} + points

	private const val refundMultiplier = 7.5

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
}
