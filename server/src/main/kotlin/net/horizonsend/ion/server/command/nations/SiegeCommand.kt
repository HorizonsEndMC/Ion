package net.horizonsend.ion.server.command.nations

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.schema.nations.NationRole
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.text.lineBreak
import net.horizonsend.ion.common.utils.text.minecraftLength
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.repeatString
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionCapturableStation
import net.horizonsend.ion.server.features.nations.region.types.RegionSolarSiegeZone
import net.horizonsend.ion.server.features.nations.sieges.SolarSiege
import net.horizonsend.ion.server.features.nations.sieges.SolarSieges
import net.horizonsend.ion.server.features.nations.sieges.StationSieges
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import org.bukkit.entity.Player
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

@CommandAlias("siege")
object SiegeCommand : SLCommand() {
	@Default
	fun execute(sender: Player) {
		tellPlayerCurrentlySiegableStations(sender)
		ensurePilotingStarship(sender)
		beginSiege(sender)
	}

	private fun tellPlayerCurrentlySiegableStations(sender: Player) {
		val currentStationNames = StationSieges.getStationsNow().joinToString {
			val nationName = it.nation?.let(NationCache::get)?.name
			val stationName = it.name
			val world = it.world
			val x = it.x
			val z = it.z
			"<dark_gray>[<aqua>$nationName<gray>'s <aqua>$stationName <gray>in <yellow>$world <gray>(<yellow>$x<gray>, <yellow>$z<gray>)<dark_gray>]"
		}

		sender.sendRichMessage("<gray>Current Stations: $currentStationNames")
	}

	private fun ensurePilotingStarship(sender: Player) {
		getStarshipPiloting(sender)
	}

	private fun beginSiege(sender: Player) {
		if (!NationRole.hasPermission(sender.slPlayerId, NationRole.Permission.START_NATION_SIEGE)) {
			sender.userError("Your nation prevents you from starting station sieges!")
			return
		}

		if (Regions.findFirstOf<RegionCapturableStation>(sender.location) != null) StationSieges.beginSiege(sender)
		if (Regions.findFirstOf<RegionSolarSiegeZone>(sender.location) != null) SolarSieges.initSiege(sender)
	}

	override fun onEnable(manager: PaperCommandManager) {
		manager.commandCompletions.registerAsyncCompletion("solarSieges") {
			return@registerAsyncCompletion SolarSieges.getAllSieges().map { it.region.name.replace(' ', '_') }
		}
		manager.commandContexts.registerContext(SolarSiege::class.java) { c ->
			val name = c.popFirstArg()
			SolarSieges.getAllSieges().firstOrNull { it.region.name.replace(' ', '_').equals(name, ignoreCase = true) } ?: throw InvalidCommandArgument("$name not found!")
		}
		manager.commandCompletions.setDefaultCompletion("solarSieges", SolarSiege::class.java)
	}

	@Subcommand("abandon")
	@CommandCompletion("@solarSieges")
	fun onAbandon(sender: Player, siege: SolarSiege) {
		if (siege.isAttacker(sender.slPlayerId)) return SolarSieges.attackerAbandonSiege(sender, siege)
		if (siege.isDefender(sender.slPlayerId)) return SolarSieges.defenderAbandonSiege(sender, siege)
		fail { "You aren't a participant of this siege!" }
	}

	private const val WIDTH = 48

	@Subcommand("status")
	@CommandCompletion("@solarSieges")
	fun onStatus(sender: Player, siege: SolarSiege) {
		failIf(siege.isPreparationPeriod()) { "That siege has not yet started!" }
		failIf(siege.isAbandoned) { "That siege has ended!" }

		val lineBreak = lineBreak(48)
		val totalWidth = lineBreak.minecraftLength + 8

		val totalPoints = (siege.defenderPoints + siege.attackerPoints).toDouble()
		val attackerWidth = ((siege.attackerPoints.toDouble() / max(totalPoints, 1.0)) * WIDTH).roundToInt()
		val defenderWidth = ((siege.defenderPoints.toDouble() / max(totalPoints, 1.0)) * WIDTH).roundToInt()

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
		guiText.add(template(text("{0}'s siege of {1}'s Solar Siege Zone {2}", YELLOW), siege.defenderNameFormatted, siege.attackerNameFormatted, siege.region.name), -1, GuiText.TextAlignment.CENTER)

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
}
