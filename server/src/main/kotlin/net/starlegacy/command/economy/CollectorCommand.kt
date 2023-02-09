package net.starlegacy.command.economy

import co.aikar.commands.ConditionFailedException
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.legacy.feedback.FeedbackType
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackMessage
import net.horizonsend.ion.server.extensions.sendInformation
import net.starlegacy.cache.trade.EcoStations
import net.starlegacy.command.SLCommand
import net.starlegacy.database.schema.economy.CollectedItem
import net.starlegacy.database.schema.economy.EcoStation
import net.starlegacy.feature.economy.collectors.Collectors
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("collector")
@CommandPermission("trade.collector")
object CollectorCommand : SLCommand() {
	@Throws(ConditionFailedException::class)
	private fun getEcoStation(location: Location): EcoStation = EcoStations.getAll()
		.filter { it.world == location.world.name }
		.filter { it.distance(location.x, location.y, location.z) < 200 }
		.sortedBy { it.distance(location.x, location.y, location.z) }
		.firstOrNull()
		?: throw ConditionFailedException("You're not within 200 blocks of any eco station!")

	@Subcommand("create")
	fun onCreate(sender: Player) = asyncCommand(sender) {
		val location: Location = sender.location

		val ecoStation = getEcoStation(location)

		sender.sendInformation("Detected station {ecoStation.name}, using...")

		EcoStation.addCollector(ecoStation._id, location.blockX, location.blockY, location.blockZ)

		sender.sendInformation("Registered collector NPC in database, synchronizing NPCs...")

		Collectors.synchronizeNPCsAsync {
			sender.sendInformation("Synchronized citizens NPCs successfully.")
		}
	}

	@Suppress("Unused")
	@Subcommand("clear|delete")
	fun onClear(sender: Player) = asyncCommand(sender) {
		val ecoStation = getEcoStation(sender.location)

		EcoStation.clearCollectors(ecoStation._id)

		sender.sendInformation("Deleted in database, synchronizing NPCs..")

		Collectors.synchronizeNPCsAsync {
			sender.sendInformation("Synchronized citizens NPCs successfully.")
		}
	}

	@Suppress("Unused")
	@Subcommand("sold")
	fun onStock(sender: CommandSender) {
		sender.sendFeedbackMessage(
			FeedbackType.INFORMATION,
			"Note: This may not be accurate if you're on a different server from the station(s)"
		)

		for (ecoStation in EcoStations.getAll()) {
			sender.sendFeedbackMessage(FeedbackType.INFORMATION, "Station {0}:", ecoStation.name)

			val items = CollectedItem.findAllAt(ecoStation._id).toList()

			if (items.isEmpty()) {
				sender.sendFeedbackMessage(FeedbackType.USER_ERROR, "  &4>> Empty?!")
				continue
			}

			for (item: CollectedItem in items) {
				val sold: String = when (item.sold) {
					0 -> "<red>"
					else -> "<gold>"
				}.toString() + item.sold.toString()

				sender.sendFeedbackMessage(FeedbackType.INFORMATION, "  {0} >> {1} stacks sold ({2} in stock)", item.itemString, sold, item.stock)
			}
		}
	}
}
