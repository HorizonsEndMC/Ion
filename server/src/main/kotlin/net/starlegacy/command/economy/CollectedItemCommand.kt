package net.starlegacy.command.economy

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.legacy.feedback.FeedbackType
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackMessage
import net.starlegacy.cache.trade.EcoStations
import net.starlegacy.command.SLCommand
import net.starlegacy.database.schema.economy.CollectedItem
import net.starlegacy.database.schema.economy.EcoStation
import net.starlegacy.feature.economy.collectors.CollectionMissions
import net.starlegacy.util.displayName
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.and
import org.litote.kmongo.eq

@CommandAlias("collecteditem|citem")
@CommandPermission("trade.collecteditem")
object CollectedItemCommand : SLCommand() {
	private fun validateValue(value: Double) {
		if (value <= 0) {
			throw InvalidCommandArgument("Value can't be <= 0")
		}
	}

	private fun resolveCollectedItem(name: String): CollectedItem {
		val split = name.split(".")

		if (!name.contains(".") || split.size != 2) {
			throw InvalidCommandArgument("Name must be formatted like 'ecostation.itemid'")
		}

		val stationName = split[0]
		val ecoStation: EcoStation = EcoStations.getByName(stationName)
			?: throw InvalidCommandArgument("Eco Station $stationName not found")

		val itemString = split[1]

		return CollectedItem.findOne(
			and(
				CollectedItem::station eq ecoStation._id,
				CollectedItem::itemString eq itemString
			)
		) ?: throw InvalidCommandArgument("$itemString not found for $stationName")
	}

	private fun validateStacks(minStacks: Int, maxStacks: Int) {
		if (minStacks > maxStacks) {
			throw InvalidCommandArgument("Min stacks can't > max stacks")
		}

		if (minStacks < 0) {
			throw InvalidCommandArgument("Min stacks can't be < 0")
		}

		if (maxStacks < 0) {
			throw InvalidCommandArgument("Max stacks can't be < 0")
		}
	}

	@Suppress("Unused")
	@Subcommand("string")
	fun onString(sender: Player) {
		val item: ItemStack = sender.inventory.itemInMainHand

		val itemString = CollectionMissions.getString(item)

		sender.sendFeedbackMessage(FeedbackType.INFORMATION, "{0} item string: {1}", item.displayName, itemString)
	}

	@Suppress("Unused")
	@Subcommand("add")
	@CommandCompletion("@ecostations DIAMOND:0|chetherite 10|20|50|100|200|300 1|2|3|5 1|3|6|9|27")
	fun onAdd(
		sender: CommandSender,
		station: EcoStation,
		itemString: String,
		value: Double,
		minStacks: Int,
		maxStacks: Int
	) = asyncCommand(sender) {
		validateValue(value)
		validateStacks(minStacks, maxStacks)

		if (CollectionMissions.getItemFromString(itemString) == null) {
			throw InvalidCommandArgument("$itemString is not a valid item string")
		}

		if (CollectedItem.findAllAt(station._id).any { it.itemString == itemString }) {
			throw InvalidCommandArgument("$itemString is already at ${station.name}")
		}

		val id = CollectedItem.create(station._id, itemString, minStacks, maxStacks, value)

		CollectionMissions.reset()

		sender.sendFeedbackMessage(
			FeedbackType.SUCCESS,
			"Added item {0} with value {1} and stack range {2}..{3}, and refreshed collector missions.",
			itemString,
			value,
			minStacks,
			maxStacks
		)
		sender.sendFeedbackMessage(FeedbackType.INFORMATION, "(ID: {0})", id)
	}

	@Suppress("Unused")
	@Subcommand("list")
	@CommandCompletion("@ecostations")
	fun onList(sender: CommandSender, station: EcoStation) {
		val items: List<CollectedItem> = CollectedItem.findAllAt(station._id).toList()
			.takeIf { it.isNotEmpty() } ?: throw InvalidCommandArgument("No items at ${station.name}")

		sender.sendFeedbackMessage(FeedbackType.INFORMATION, "Station {0} Items:", station.name)
		sender.sendFeedbackMessage(
			FeedbackType.INFORMATION,
			"(ID: item string, value, min stacks, max stacks, stock)"
		)

		for (item: CollectedItem in items) {
			sender.sendFeedbackMessage(
				FeedbackType.INFORMATION,
				"{0}: {1}, {2}, {3}, {4}, {5}",
				item._id,
				item.itemString,
				item.value,
				item.minStacks,
				item.maxStacks,
				item.stock
			)
		}
	}

	@Suppress("Unused")
	@Subcommand("remove")
	@CommandCompletion("@ecostations @collecteditems")
	fun onRemove(sender: CommandSender, station: EcoStation, item: String) = asyncCommand(sender) {
		val collectedItem = resolveCollectedItem(item)

		if (collectedItem.station != station._id) {
			throw InvalidCommandArgument("That item is not collected at that station!")
		}

		CollectedItem.delete(collectedItem._id)

		CollectionMissions.reset()

		sender.sendFeedbackMessage(
			FeedbackType.SUCCESS,
			"Removed item {0} from {1} and refreshed collector missions.",
			collectedItem.itemString,
			station.name
		)
	}

	@Suppress("Unused")
	@Subcommand("set value")
	@CommandCompletion("@collecteditems 10|20|50|100|200|300")
	fun onSetValue(sender: CommandSender, item: String, value: Double) = asyncCommand(sender) {
		val collectedItem = resolveCollectedItem(item)

		validateValue(value)

		CollectedItem.setValue(collectedItem._id, value)

		CollectionMissions.reset()

		sender.sendFeedbackMessage(
			FeedbackType.SUCCESS,
			"Changed value of {0} at {1} from {2} to {3} and refreshed collector missions.",
			collectedItem.itemString,
			EcoStations[collectedItem.station].name,
			collectedItem.value,
			value
		)
	}

	@Suppress("Unused")
	@Subcommand("set stock")
	@CommandCompletion("@collecteditems 0")
	fun onSetStock(sender: CommandSender, item: String, stock: Int) = asyncCommand(sender) {
		val collectedItem = resolveCollectedItem(item)

		if (stock < 0) {
			throw InvalidCommandArgument("Cannot have negative stock!")
		}

		CollectedItem.setStock(collectedItem._id, stock)

		CollectionMissions.reset()

		sender.sendFeedbackMessage(
			FeedbackType.SUCCESS,
			"Changed stock of {0} at {1} from {2} to {3} and refreshed collector missions.",
			collectedItem.itemString,
			EcoStations[collectedItem.station].name,
			collectedItem.stock,
			stock
		)
	}

	@Suppress("Unused")
	@Subcommand("set stackrange")
	@CommandCompletion("@collecteditems 1 10")
	fun onSetStackRange(sender: CommandSender, item: String, minStacks: Int, maxStacks: Int) {
		asyncCommand(sender) {
			val collectedItem = resolveCollectedItem(item)

			validateStacks(minStacks, maxStacks)

			CollectedItem.setStackRange(collectedItem._id, minStacks, maxStacks)

			CollectionMissions.reset()

			sender.sendFeedbackMessage(
				FeedbackType.SUCCESS,
				"Changed stack range of {0} at {1} to {2}..{3} and refreshed collector missions.",
				collectedItem.itemString,
				EcoStations[collectedItem.station].name,
				minStacks,
				maxStacks
			)
		}
	}
}
