package net.starlegacy.command.economy

import co.aikar.commands.ConditionFailedException
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.starlegacy.command.SLCommand
import net.horizonsend.ion.common.database.schema.economy.CityNPC
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.uuid
import net.starlegacy.feature.economy.city.CityNPCs
import net.starlegacy.feature.economy.city.TradeCities
import net.starlegacy.feature.economy.city.TradeCityData
import net.starlegacy.feature.economy.city.TradeCityType
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.feature.nations.region.types.RegionTerritory
import net.starlegacy.util.Skins
import net.starlegacy.util.distanceSquared
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.UUID

@CommandAlias("citynpc|cnpc")
object CityNpcCommand : SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		registerStaticCompletion(manager, "npctypes", CityNPC.Type.entries.joinToString("|") { it.name })
	}

	private fun getCurrentCityContext(sender: Player): Triple<Location, RegionTerritory, TradeCityData> {
		val location: Location = sender.location
		val territory: RegionTerritory = Regions.findFirstOf(location)
			?: throw ConditionFailedException("You aren't in a territory")
		val cityData: TradeCityData = TradeCities.getIfCity(territory)
			?: throw ConditionFailedException("You are not in a protected trade city")
		return Triple(location, territory, cityData)
	}

	private fun requireCanManage(sender: Player, cityData: TradeCityData) {
		if (sender.hasPermission("trade.citynpc.admin")) {
			return
		}

		when (cityData.type) {
			TradeCityType.NPC -> throw ConditionFailedException("You don't have control over NPC cities!")
			TradeCityType.SETTLEMENT -> requireSettlementLeader(sender, cityData.settlementId)
		}
	}

	@Subcommand("create")
	@CommandCompletion("@npctypes @nothing")
	fun onCreate(sender: Player, type: CityNPC.Type, skin: String) = asyncCommand(sender) {
		val (location, territory, cityData) = getCurrentCityContext(sender)
		requireCanManage(sender, cityData)

		val id: UUID = getIdFromName(skin)
		val skinData: Skins.SkinData = Skins[id] ?: fail { "Failed to retrieve skin for $skin!" }

		CityNPC.create(territory.id, location.x, location.y, location.z, skinData.toBytes(), type)
		CityNPCs.synchronizeNPCsAsync()

		sender.success("Created NPC!")
	}

	private fun getIdFromName(name: String): UUID = SLPlayer[name]?._id?.uuid
		?: Bukkit.createProfile(name).apply { complete(false) }.id
		?: fail { "Player $name not found" }

	@Subcommand("delete|remove")
	fun onDelete(sender: Player) = asyncCommand(sender) {
		val npc = requireNearbyNPC(sender)

		CityNPC.delete(npc._id)
		CityNPCs.synchronizeNPCsAsync() // update the actual npc

		sender.success("Deleted NPC!")
	}

	fun requireNearbyNPC(sender: Player, permission: Boolean = true): CityNPC {
		val (location, territory, cityData) = getCurrentCityContext(sender)

		val npcList: List<CityNPC> = CityNPC.findAt(territory.id).toList()

		if (npcList.isEmpty()) {
			throw ConditionFailedException("City ${cityData.displayName} has no NPCs!")
		}

		val x = location.x
		val y = location.y
		val z = location.z

		val npc = npcList.minByOrNull { distanceSquared(it.x, it.y, it.z, x, y, z) } ?: fail { "No nearby NPCs" }
		val distance = Location(location.world, npc.x, npc.y, npc.z).distance(location)

		if (distance > 5) {
			throw ConditionFailedException("There are no NPCs within 5 blocks! Nearest distance: $distance")
		}

		if (permission) {
			requireCanManage(sender, cityData)
		}

		return npc
	}

	@Suppress("Unused")
	@Subcommand("sync")
	fun onSync(sender: CommandSender) {
		sender.information("Synchronizing...")
		CityNPCs.synchronizeNPCsAsync {
			sender.success("Synchronized!")
		}
	}
}
