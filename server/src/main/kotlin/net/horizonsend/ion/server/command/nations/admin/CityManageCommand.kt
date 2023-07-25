package net.horizonsend.ion.server.command.nations.admin

import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.server.miscellaneous.utils.msg
import org.bukkit.command.CommandSender

@CommandAlias("citymanage")
@CommandPermission("nations.citymanage")
internal object CityManageCommand : net.horizonsend.ion.server.command.SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		registerAsyncCompletion(manager, "settlements") { _ -> SettlementCache.all().map { it.name } }
	}

	@Subcommand("register")
	@CommandCompletion("@settlements")
	fun onRegister(sender: CommandSender, settlement: String) = asyncCommand(sender) {
		val settlementId = resolveSettlement(settlement)
		val settlementData = SettlementCache[settlementId]

		failIf(settlementData.cityState != null) { "Settlement ${settlementData.name} is already a city." }

		Settlement.setCityState(settlementId, Settlement.CityState.ACTIVE)
		sender msg "&aRegistered ${settlementData.name} as a trade city."
	}

	@Subcommand("unregister")
	@CommandCompletion("@settlements")
	fun onUnregister(sender: CommandSender, settlement: String) = asyncCommand(sender) {
		val settlementId = resolveSettlement(settlement)
		val settlementData = SettlementCache[settlementId]

		failIf(settlementData.cityState == null) { "${settlementData.name} is not a city" }

		Settlement.setCityState(settlementId, null)
		sender msg "&aUnregistered ${settlementData.name} as a trade city."
	}

	@Subcommand("list")
	fun onList(sender: CommandSender) = asyncCommand(sender) {
		sender msg Settlement.all().filter { it.cityState != null }.joinToString { it.name }
	}

	@Subcommand("setstate")
	@CommandCompletion("@settlements")
	fun onSetState(sender: CommandSender, settlement: String, state: Settlement.CityState) {
		val settlementId = resolveSettlement(settlement)
		val settlementData = SettlementCache[settlementId]

		failIf(settlementData.cityState == null) { "${settlementData.name} is not a city" }

		Settlement.setCityState(settlementId, state)
		sender msg "&aChanged state of ${settlementData.name} from ${settlementData.cityState} to $state"
	}
}
