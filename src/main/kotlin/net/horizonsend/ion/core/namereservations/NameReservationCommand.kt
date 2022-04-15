package net.horizonsend.ion.core.namereservations

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Single
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.core.namereservations.NameReservations.addNationReservation
import net.horizonsend.ion.core.namereservations.NameReservations.addSettlementReservation
import net.horizonsend.ion.core.namereservations.NameReservations.doesNationReservationExist
import net.horizonsend.ion.core.namereservations.NameReservations.doesSettlementReservationExist
import net.horizonsend.ion.core.namereservations.NameReservations.getNameReservationData
import net.horizonsend.ion.core.namereservations.NameReservations.removeNationReservation
import net.horizonsend.ion.core.namereservations.NameReservations.removeSettlementReservation
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.starlegacy.StarLegacy
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

@CommandAlias("reservations")
@CommandPermission("reservations")
@Description("Controls name reservations")
internal class NameReservationCommand(val plugin: StarLegacy): BaseCommand() {
	@Subcommand("list")
	@Description("Lists name reservations")
	@Suppress("unused") // Entrypoint (Command)
	inner class List: BaseCommand() {
		@Default
		@Subcommand("all")
		@Description("Lists all name reservations")
		fun commandReservationsListAll(sender: CommandSender) =
			sender.sendMessage(miniMessage().deserialize(
				mutableListOf<String>().apply {
					addAll(getSettlementNameReservations())
					addAll(getNationNameReservations())
					sort()
				}.joinToString("\n", "", "")
			))

		@Subcommand("settlements")
		@Description("Lists all settlement reservations")
		fun commandReservationsListSettlements(sender: CommandSender) =
			sender.sendMessage(miniMessage().deserialize(
				getSettlementNameReservations().joinToString("\n", "", "")
			))

		@Subcommand("nations")
		@Description("Lists all nation reservations")
		fun commandReservationsListNations(sender: CommandSender) =
			sender.sendMessage(miniMessage().deserialize(
				getNationNameReservations().joinToString("\n", "", "")
			))

		private fun getSettlementNameReservations() =
			getNameReservationData().settlements.map {
				"<aqua>Settlement</aqua> ${it.key} <aqua>for Player</aqua> ${(plugin.server.getPlayer(it.value) ?: Bukkit.getOfflinePlayer(it.value)).name ?: "Unknown"}"
			}

		private fun getNationNameReservations() =
			getNameReservationData().nations.map {
				"<aqua>Nation</aqua> ${it.key} <aqua>for Player</aqua> ${(plugin.server.getPlayer(it.value) ?: Bukkit.getOfflinePlayer(it.value)).name ?: "Unknown"}"
			}
	}

	@Subcommand("add")
	@Description("Adds name reservations")
	@Suppress("unused") // Entrypoint (Command)
	inner class Add: BaseCommand() {
		@Subcommand("settlement")
		@Description("Adds a settlement name reservation")
		fun commandReservationsAddSettlement(sender: CommandSender, @Single target: String, @Single name: String) {
			val targetPlayer = Bukkit.getPlayer(target)?.uniqueId ?: Bukkit.getOfflinePlayer(target).uniqueId

			if (doesSettlementReservationExist(name)) {
				sender.sendMessage(miniMessage().deserialize("<yellow>Settlement reservation <white>\"$name\"</white> already exists."))
				return
			}

			addSettlementReservation(name, targetPlayer)

			sender.sendMessage(miniMessage().deserialize("<green>Settlement reservation <white>\"$name\"</white> created."))
		}

		@Subcommand("nation")
		@Description("Adds a nation name reservation")
		fun commandReservationsAddNation(sender: CommandSender, @Single target: String, @Single name: String) {
			val targetPlayer = Bukkit.getPlayer(target)?.uniqueId ?: Bukkit.getOfflinePlayer(target).uniqueId

			if (doesNationReservationExist(name)) {
				sender.sendMessage(miniMessage().deserialize("<yellow>Nation reservation <white>\"$name\"</white> already exists."))
				return
			}

			addNationReservation(name, targetPlayer)

			sender.sendMessage(miniMessage().deserialize("<green>Nation reservation <white>\"$name\"</white> created."))
		}
	}

	@Subcommand("remove")
	@Description("Removes name reservations")
	@Suppress("unused") // Entrypoint (Command)
	inner class Remove: BaseCommand() {
		@Subcommand("settlement")
		@Description("Removes a settlement name reservation")
		fun commandReservationsRemoveSettlement(sender: CommandSender, @Single name: String) {
			if (!doesSettlementReservationExist(name)) {
				sender.sendMessage(miniMessage().deserialize("<yellow>Settlement reservation <white>\"$name\"</white> does not exist."))
				return
			}

			removeSettlementReservation(name)

			sender.sendMessage(miniMessage().deserialize("<green>Settlement reservation <white>\"$name\"</white> removed."))
		}

		@Subcommand("nation")
		@Description("Removes a nation name reservation")
		fun commandReservationsRemoveNation(sender: CommandSender, @Single name: String) {
			if (!doesNationReservationExist(name)) {
				sender.sendMessage(miniMessage().deserialize("<yellow>Nation reservation <white>\"$name\"</white> does not exist."))
				return
			}

			removeNationReservation(name)

			sender.sendMessage(miniMessage().deserialize("<green>Nation reservation <white>\"$name\"</white> removed."))
		}
	}
}