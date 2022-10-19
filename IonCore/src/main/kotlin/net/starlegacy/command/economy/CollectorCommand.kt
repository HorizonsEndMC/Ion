package net.starlegacy.command.economy

import co.aikar.commands.ConditionFailedException
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.starlegacy.cache.trade.EcoStations
import net.starlegacy.command.SLCommand
import net.starlegacy.database.schema.economy.CollectedItem
import net.starlegacy.database.schema.economy.EcoStation
import net.starlegacy.feature.economy.collectors.Collectors
import net.starlegacy.util.SLTextStyle
import net.starlegacy.util.msg
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

		sender msg "&7&oDetected station &a&o${ecoStation.name}&7&o, using..."

		EcoStation.addCollector(ecoStation._id, location.blockX, location.blockY, location.blockZ)

		sender msg "&aRegistered collector npc in database, synchronizing NPCs..."

		Collectors.synchronizeNPCsAsync {
			sender msg "&bSynchronized citizens NPCs successfully."
		}
	}

	@Subcommand("clear|delete")
	fun onClear(sender: Player) = asyncCommand(sender) {
		val ecoStation = getEcoStation(sender.location)

		EcoStation.clearCollectors(ecoStation._id)

		sender msg "&aDeleted in database, synchronizing NPCs.."

		Collectors.synchronizeNPCsAsync {
			sender msg "&bSynchronized citizens NPCs successfully."
		}
	}

	@Subcommand("sold")
	fun onStock(sender: CommandSender) {
		sender msg "&7&oNote: This may not be accurate if you're on a different server from the station(s)"

		for (ecoStation in EcoStations.getAll()) {
			sender msg "&7Station &3${ecoStation.name}&7:"

			val items = CollectedItem.findAllAt(ecoStation._id).toList()

			if (items.isEmpty()) {
				sender msg "  &4>> Empty?!"
				continue
			}

			for (item: CollectedItem in items) {
				val sold: String = when (item.sold) {
					0 -> SLTextStyle.RED
					else -> SLTextStyle.GOLD
				}.toString() + item.sold.toString()

				sender msg "  &b${item.itemString} &8>> $sold&e stacks sold &8(&7&o${item.stock} in stock&8)"
			}
		}
	}
}
