package net.horizonsend.ion.server.command.nations

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.schema.nations.DominionTerritorySiegeData
import net.horizonsend.ion.common.database.schema.nations.FrontierNationSiegeData
import net.horizonsend.ion.common.database.schema.nations.GasDepot
import net.horizonsend.ion.common.database.schema.nations.GasDepotSiegeData
import net.horizonsend.ion.common.database.schema.nations.NationRole
import net.horizonsend.ion.common.database.schema.nations.SolarSiegeData
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.miscellaneous.getDurationBreakdown
import net.horizonsend.ion.common.utils.text.lineBreak
import net.horizonsend.ion.common.utils.text.minecraftLength
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.repeatString
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionCapturableStation
import net.horizonsend.ion.server.features.nations.region.types.RegionDominionTerritory
import net.horizonsend.ion.server.features.nations.region.types.RegionFrontierTerritory
import net.horizonsend.ion.server.features.nations.region.types.RegionGasDepot
import net.horizonsend.ion.server.features.nations.region.types.RegionSolarSiegeZone
import net.horizonsend.ion.server.features.nations.sieges.DominionTerritorySiege
import net.horizonsend.ion.server.features.nations.sieges.DominionTerritorySieges
import net.horizonsend.ion.server.features.nations.sieges.FrontierNationSiege
import net.horizonsend.ion.server.features.nations.sieges.FrontierNationSieges
import net.horizonsend.ion.server.features.nations.sieges.GasDepotSieges
import net.horizonsend.ion.server.features.nations.sieges.KingOfTheHills
import net.horizonsend.ion.server.features.nations.sieges.SiegeRewardsGui
import net.horizonsend.ion.server.features.nations.sieges.SolarSiege
import net.horizonsend.ion.server.features.nations.sieges.SolarSieges
import net.horizonsend.ion.server.features.nations.sieges.StationSieges
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import org.bukkit.entity.Player
import org.litote.kmongo.EMPTY_BSON
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.not
import org.litote.kmongo.or
import org.litote.kmongo.size
import java.time.Duration
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

@CommandAlias("siege")
object SiegeCommand : SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandCompletions.registerAsyncCompletion("solarSieges") {
			return@registerAsyncCompletion SolarSieges.getAllSieges().map { it.region.name.replace(' ', '_') }
		}
		manager.commandCompletions.registerAsyncCompletion("pastSolarSieges") {
			return@registerAsyncCompletion SolarSiegeData.findProp(EMPTY_BSON, SolarSiegeData::zone).mapTo(mutableListOf()) { Regions.get<RegionSolarSiegeZone>(it).name.replace(' ', '_') }
		}
		manager.commandContexts.registerContext(SolarSiege::class.java) { c ->
			val name = c.popFirstArg()
			SolarSieges.getAllSieges().firstOrNull { it.region.name.replace(' ', '_').equals(name, ignoreCase = true) } ?: throw InvalidCommandArgument("Active siege in $name not found!")
		}
		manager.commandCompletions.setDefaultCompletion("solarSieges", SolarSiege::class.java)

		manager.commandCompletions.registerAsyncCompletion("frontierSieges") {
			return@registerAsyncCompletion FrontierNationSieges.getAllSieges().map { it.region.name.replace(' ', '_') }
		}
		manager.commandCompletions.registerAsyncCompletion("pastFrontierSieges") {
			return@registerAsyncCompletion FrontierNationSiegeData.findProp(EMPTY_BSON, FrontierNationSiegeData::zone).mapTo(mutableListOf()) { Regions.get<RegionFrontierTerritory>(it).name.replace(' ', '_') }
		}
		manager.commandContexts.registerContext(FrontierNationSiege::class.java) { c ->
			val name = c.popFirstArg()
			FrontierNationSieges.getAllSieges().firstOrNull { it.region.name.replace(' ', '_').equals(name, ignoreCase = true) } ?: throw InvalidCommandArgument("Active siege in $name not found!")
		}
		manager.commandCompletions.setDefaultCompletion("frontierSieges", FrontierNationSiege::class.java)

		manager.commandCompletions.registerAsyncCompletion("dominionSieges") {
			return@registerAsyncCompletion DominionTerritorySieges.getAllSieges().map { it.region.name.replace(' ', '_') }
		}
		manager.commandCompletions.registerAsyncCompletion("pastDominionSieges") {
			return@registerAsyncCompletion DominionTerritorySiegeData.findProp(EMPTY_BSON, DominionTerritorySiegeData::zone).mapTo(mutableListOf()) { Regions.get<RegionDominionTerritory>(it).name.replace(' ', '_') }
		}
		manager.commandContexts.registerContext(DominionTerritorySiege::class.java) { c ->
			val name = c.popFirstArg()
			DominionTerritorySieges.getAllSieges().firstOrNull { it.region.name.replace(' ', '_').equals(name, ignoreCase = true) } ?: throw InvalidCommandArgument("Active siege in $name not found!")
		}
		manager.commandCompletions.setDefaultCompletion("dominionSieges", DominionTerritorySiege::class.java)

		manager.commandCompletions.registerAsyncCompletion("gasDepots") {
			return@registerAsyncCompletion GasDepotSieges.activeSieges.map {
				val depot: RegionGasDepot = Regions[it.depotId]
				depot.name.replace(' ', '_')
			}
		}
		manager.commandContexts.registerContext(GasDepot::class.java) { c ->
			val name = c.popFirstArg()
			val activeSiege = GasDepotSieges.activeSieges.firstOrNull {
				val depot: RegionGasDepot = Regions[it.depotId]
				depot.name.replace(' ', '_').equals(name, ignoreCase = true)
			} ?: throw InvalidCommandArgument("Active siege in $name not found!")
			GasDepot.findById(activeSiege.depotId) ?: throw InvalidCommandArgument("Gas depot not found!")
		}

		manager.commandCompletions.setDefaultCompletion("gasDepots", GasDepot::class.java)
	}

	@Default
	fun execute(sender: Player) {
		ensurePilotingStarship(sender)
		beginSiege(sender)
	}

	@Subcommand("current")
	fun onGetCurrentSiegableStations(sender: Player) {
		tellPlayerCurrentlySiegableStations(sender)
	}

	@Subcommand("scoreboard")
	fun tellPlayerKothScoreboard(sender: Player) {
		val activeKoths = KingOfTheHills.getKOTHS()
		for (koth in activeKoths) {
			val scores = koth.kothPoints
			val name = koth.kothId
			sender.sendRichMessage("<gray>Current scores for $name:\n<gold> $scores")
		}
	}

	private fun tellPlayerCurrentlySiegableStations(sender: Player) {
		val currentKothNames = KingOfTheHills.getCurrentKoth().joinToString {
			val stationName = it.name
			val world = it.world
			val x = it.x
			val z = it.z
			"<dark_gray>[<aqua>$stationName <gray>in <yellow>$world <gray>(<yellow>$x<gray>, <yellow>$z<gray>)<dark_gray>]"
		}
		val currentStationNames = StationSieges.getStationsNow().joinToString {
			val nationName = it.nation?.let(NationCache::get)?.name
			val stationName = it.name
			val world = it.world
			val x = it.x
			val z = it.z
			"<dark_gray>[<aqua>$nationName<gray>'s <aqua>$stationName <gray>in <yellow>$world <gray>(<yellow>$x<gray>, <yellow>$z<gray>)<dark_gray>]"
		}

		sender.sendRichMessage("<gray>Current Stations: $currentStationNames")
		sender.sendRichMessage("<gray>Current KOTHs: $currentKothNames")
	}

	private fun ensurePilotingStarship(sender: Player) {
		getStarshipPiloting(sender)
	}

	private fun beginSiege(sender: Player) {
		requireNationIn(sender)
		if (!NationRole.hasPermission(sender.slPlayerId, NationRole.Permission.START_NATION_SIEGE)) {
			sender.userError("Your nation prevents you from starting station sieges!")
			return
		}

		if(getStarshipPiloting(sender).initialBlockCount < 4000) {
			sender.userError("You must be piloting a ship larger than 4000 blocks to initiate a siege!")
			return
		}

		if (Regions.findFirstOf<RegionCapturableStation>(sender.location) != null) return StationSieges.beginSiege(sender)
		if (Regions.findFirstOf<RegionSolarSiegeZone>(sender.location) != null) return SolarSieges.initSiege(sender)
		if (Regions.findFirstOf<RegionFrontierTerritory>(sender.location) != null) return FrontierNationSieges.initSiege(sender)
		if (Regions.findFirstOf<RegionDominionTerritory>(sender.location) != null) return DominionTerritorySieges.initSiege(sender)
		if (Regions.findFirstOf<RegionGasDepot>(sender.location) != null) return GasDepotSieges.beginSiege(sender)
	}

	val SIEGE_INFO_WIDTH get() = 48

		@Subcommand("abandonSolar")
		@CommandCompletion("@solarSieges")
		fun onAbandonSolar(sender: Player, siege: SolarSiege) {
			if (siege.isAttacker(sender.slPlayerId)) return SolarSieges.attackerAbandonSiege(sender, siege)
			if (siege.isDefender(sender.slPlayerId)) return SolarSieges.defenderAbandonSiege(sender, siege)
			fail { "You aren't a participant of this siege!" }
		}

		@Subcommand("solarstatus")
		@CommandCompletion("@solarSieges")
		fun onSolarStatus(sender: Player, siege: SolarSiege) {
			failIf(siege.isPreparationPeriod()) {
				val (_, hours, minutes, seconds) = getDurationBreakdown(siege.getActivePeriodStart() - System.currentTimeMillis())
				"That siege has not yet started! It will be begin in $hours hours, $minutes minutes, and $seconds seconds."
			}

			failIf(siege.isAbandoned) { "That siege has ended!" }

			val lineBreak = lineBreak(48)
			val totalWidth = lineBreak.minecraftLength + 8

			val totalPoints = (siege.defenderPoints + siege.attackerPoints).toDouble()
			val attackerWidth = ((siege.attackerPoints.toDouble() / max(totalPoints, 1.0)) * SIEGE_INFO_WIDTH).roundToInt()
			val defenderWidth = ((siege.defenderPoints.toDouble() / max(totalPoints, 1.0)) * SIEGE_INFO_WIDTH).roundToInt()

			val attackerColor = NationCache[siege.attacker].textColor
			val defenderColor = NationCache[siege.defender].textColor

			val guiText = GuiText("", guiWidth = totalWidth, initialShiftDown = -1)

			guiText.add(text(repeatString("=", attackerWidth), attackerColor), 0, GuiText.TextAlignment.LEFT)
			guiText.add(text(repeatString("=", defenderWidth), defenderColor), 0, GuiText.TextAlignment.RIGHT)

			guiText.add(siege.attackerNameFormatted, 1, GuiText.TextAlignment.LEFT)
			guiText.add(siege.defenderNameFormatted, 1, GuiText.TextAlignment.RIGHT)

			guiText.add(text(siege.attackerPoints, attackerColor), 2, GuiText.TextAlignment.LEFT)
			guiText.add(text(siege.defenderPoints, defenderColor), 2, GuiText.TextAlignment.RIGHT)

			val remaining = siege
				.getRemainingTime()
				.toSeconds() // Get seconds value
				.seconds // Convert to kotlin duration
				.toComponents { hours, minutes, seconds, _ -> // Format string
					"$hours Hour${if (hours == 1L) "" else "s"} $minutes Minute${if (minutes == 1) "" else "s"} $seconds Second${if (seconds == 1) "" else "s"}"
				}

			guiText.add(text("$remaining remaining", YELLOW), 3, GuiText.TextAlignment.CENTER)
			guiText.add(template(text("{0}'s siege of {1}'s Solar Siege Zone {2}", YELLOW), siege.attackerNameFormatted, siege.defenderNameFormatted, siege.region.name), -1, GuiText.TextAlignment.CENTER)

			sender.sendMessage(ofChildren(
				lineBreak, newline(),
				guiText.build(),
				newline(),
				newline(),
				newline(),
				newline(),
				newline(),
				lineBreak
			))
		}

	// DOMINION
	@Subcommand("abandonDominion")
	@CommandCompletion("@dominionSieges")
	fun onAbandonDominion(sender: Player, siege: DominionTerritorySiege) {
		if (siege.isAttacker(sender.slPlayerId)) return DominionTerritorySieges.attackerAbandonSiege(sender, siege)
		if (siege.isDefender(sender.slPlayerId)) return DominionTerritorySieges.defenderAbandonSiege(sender, siege)
		fail { "You aren't a participant of this siege!" }
	}

	@Subcommand("dominionstatus")
	@CommandCompletion("@dominionSieges")
	fun onDominionStatus(sender: Player, siege: DominionTerritorySiege) {
		failIf(siege.isPreparationPeriod()) {
			val (_, hours, minutes, seconds) = getDurationBreakdown(siege.getActivePeriodStart() - System.currentTimeMillis())
			"That siege has not yet started! It will begin in $hours hours, $minutes minutes, and $seconds seconds."
		}
		failIf(siege.isAbandoned) { "That siege has ended!" }
		sendSiegeStatus(sender, siege.attackerNameFormatted, siege.defenderNameFormatted, siege.attackerPoints, siege.defenderPoints, siege.getRemainingTime(), "Dominion Territory", siege.region.world)
	}
/*
	// FRONTIER
	@Subcommand("abandonFrontier")
	@CommandCompletion("@frontierSieges")
	fun onAbandonFrontier(sender: Player, siege: FrontierNationSiege) {
		if (siege.isAttacker(sender.slPlayerId)) return FrontierNationSieges.attackerAbandonSiege(sender, siege)
		if (siege.isDefender(sender.slPlayerId)) return FrontierNationSieges.defenderAbandonSiege(sender, siege)
		fail { "You aren't a participant of this siege!" }
	}

	@Subcommand("frontierstatus")
	@CommandCompletion("@frontierSieges")
	fun onFrontierStatus(sender: Player, siege: FrontierNationSiege) {
		failIf(siege.isPreparationPeriod()) {
			val (_, hours, minutes, seconds) = getDurationBreakdown(siege.getActivePeriodStart() - System.currentTimeMillis())
			"That siege has not yet started! It will begin in $hours hours, $minutes minutes, and $seconds seconds."
		}
		failIf(siege.isAbandoned) { "That siege has ended!" }
		sendSiegeStatus(sender, siege.attackerNameFormatted, siege.defenderNameFormatted, siege.attackerPoints, siege.defenderPoints, siege.getRemainingTime(), "Frontier Territory", siege.region.name)
	}
*/
	private fun sendSiegeStatus(
		sender: Player,
		attackerNameFormatted: Component,
		defenderNameFormatted: Component,
		attackerPoints: Int,
		defenderPoints: Int,
		remainingTime: Duration,
		typeLabel: String,
		locationName: String
	) {
		val lineBreak = lineBreak(48)
		val totalWidth = lineBreak.minecraftLength + 8

		val totalPoints = (defenderPoints + attackerPoints).toDouble()
		val attackerWidth = ((attackerPoints.toDouble() / max(totalPoints, 1.0)) * SIEGE_INFO_WIDTH).roundToInt()
		val defenderWidth = ((defenderPoints.toDouble() / max(totalPoints, 1.0)) * SIEGE_INFO_WIDTH).roundToInt()

		val guiText = GuiText("", guiWidth = totalWidth, initialShiftDown = -1)

		guiText.add(text(repeatString("=", attackerWidth)), 0, GuiText.TextAlignment.LEFT)
		guiText.add(text(repeatString("=", defenderWidth)), 0, GuiText.TextAlignment.RIGHT)

		guiText.add(attackerNameFormatted, 1, GuiText.TextAlignment.LEFT)
		guiText.add(defenderNameFormatted, 1, GuiText.TextAlignment.RIGHT)

		guiText.add(text(attackerPoints), 2, GuiText.TextAlignment.LEFT)
		guiText.add(text(defenderPoints), 2, GuiText.TextAlignment.RIGHT)

		val remaining = remainingTime.seconds.seconds.toComponents { hours, minutes, seconds, _ ->
			"$hours Hour${if (hours == 1L) "" else "s"} $minutes Minute${if (minutes == 1) "" else "s"} $seconds Second${if (seconds == 1) "" else "s"}"
		}

		guiText.add(text("$remaining remaining", YELLOW), 3, GuiText.TextAlignment.CENTER)
		guiText.add(
			template(text("{0}'s siege of {1}'s $typeLabel {2}", YELLOW), attackerNameFormatted, defenderNameFormatted, locationName),
			-1, GuiText.TextAlignment.CENTER
		)

		sender.sendMessage(ofChildren(lineBreak, newline(), guiText.build(), newline(), newline(), newline(), newline(), newline(), lineBreak))
	}


	@Subcommand("rewards")
	fun onRewards(sender: Player) {
		val nationId = requireNationIn(sender)
		requireNationPermission(sender, nationId, NationRole.Permission.MONEY_WITHDRAW)

		val solarSiegeIds = SolarSiegeData.findProp(
			and(
				or(not(SolarSiegeData::availableRewards size(0)), SolarSiegeData::availableRewards eq null),
				or(SolarSiegeData::attacker eq nationId, SolarSiegeData::defender eq nationId)
			),
			SolarSiegeData::_id
		).toList()

		val gasDepotSiegeIds = GasDepotSiegeData.findByNation(nationId)
			.filter { it.availableRewards.isNotEmpty() }
			.map { it._id }
			.toList()

		failIf(solarSiegeIds.isEmpty() && gasDepotSiegeIds.isEmpty()) { "You don't have any rewards to collect!" }

		SiegeRewardsGui(sender, solarSiegeIds, gasDepotSiegeIds).openGui()
	}

	fun getSiegeRegionName(id: Oid<SolarSiegeData>): String = SolarSiegeData.findPropById(id, SolarSiegeData::zone)!!.let { Regions.get<RegionSolarSiegeZone>(it).name.replace(' ', '_') }
}
