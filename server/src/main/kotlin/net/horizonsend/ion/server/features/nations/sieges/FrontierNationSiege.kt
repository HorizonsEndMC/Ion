package net.horizonsend.ion.server.features.nations.sieges

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.FrontierNationCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.FrontierNation
import net.horizonsend.ion.common.database.schema.nations.FrontierNationSiegeData
import net.horizonsend.ion.common.database.schema.nations.FrontierTerritory
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.formatFrontierNationName
import net.horizonsend.ion.common.utils.text.lineBreak
import net.horizonsend.ion.common.utils.text.minecraftLength
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.common.utils.text.repeatString
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.command.nations.SiegeCommand.SIEGE_INFO_WIDTH
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.chat.Discord
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.nations.region.types.RegionFrontierTerritory
import net.horizonsend.ion.server.features.nations.sieges.SolarSieges.config
import net.horizonsend.ion.server.features.progression.SLXP
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.runnable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import org.bukkit.Bukkit
import java.time.Duration
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.roundToInt

class FrontierNationSiege(
	val databaseId: Oid<FrontierNationSiegeData>,
	val region: RegionFrontierTerritory,
	val attacker: Oid<FrontierNation>,
	attackerPoints: Int = 0,
	val defender: Oid<FrontierNation>,
	defenderPoints: Int = 0,
	val declaredTime: Long,
) {
	val taskIds = mutableSetOf<Int>()

	var isAbandoned: Boolean = false
	var needsSave: Boolean = false; private set

	var attackerPoints: Int = attackerPoints
		@Synchronized get
		@Synchronized set(value) {
			field = value
			needsSave = true
		}

	var defenderPoints: Int = defenderPoints
		@Synchronized get
		@Synchronized set(value) {
			field = value
			needsSave = true
		}

	fun saveSiegeData() = FrontierNationSiegeData.updatePoints(databaseId, attackerPoints, defenderPoints)

	fun formatName(): Component {
		return template(
			text("{0}'s siege of {1}'s Solar Siege Zone in {2}", HE_MEDIUM_GRAY),
			attackerNameFormatted,
			defenderNameFormatted,
			region.world
		)
	}

	fun isAttacker(player: SLPlayerId): Boolean {
		val playerNation = PlayerCache[player].frontierNationOid ?: return false

		return attacker == playerNation
	}

	fun isDefender(player: SLPlayerId): Boolean {
		val playerNation = PlayerCache[player].frontierNationOid ?: return false

		return defender == playerNation
	}

	fun isActivePeriod(): Boolean {
		if (isAbandoned) return false

		val time = System.currentTimeMillis()
		return time >= getActivePeriodStart() && time < getSiegeEnd()
	}

	fun isPreparationPeriod(): Boolean {
		val time = System.currentTimeMillis()
		return time >= declaredTime && time < getActivePeriodStart()
	}

	fun getActivePeriodStart(): Long {
		return declaredTime + config.preparationWindowDuration.toDuration().toMillis()
	}

	fun getSiegeEnd(): Long {
		return getActivePeriodStart() + config.activeWindowDuration.toDuration().toMillis()
	}

	fun getRemainingTime(): Duration = Duration.ofMillis(getSiegeEnd() - System.currentTimeMillis())

	/**
	 * @param disableEarlyCheck: Disables premature ending check which triggers abandon message
	 **/
	fun endSiege(disableEarlyCheck: Boolean = false) {
		removeActive()
		if (isSuccess()) succeed() else fail(disableEarlyCheck)
	}

	private fun isSuccess(): Boolean {
		return attackerPoints > defenderPoints
	}

	fun succeed() {
		FrontierTerritory.setFrontierNation(region.id, null)

		val attackerName = FrontierNationCache[attacker].name
		val defenderName = FrontierNationCache[defender].name

		Discord.sendEmbed(
			ConfigurationFiles.discordSettings().eventsChannel,
			Embed(
				title = "Frontier Siege Success",
				description = "$attackerName's siege of $defenderName's Frontier Nation in ${region.world} has succeeded.",
				fields = listOf(
					Embed.Field("$attackerName's Points,", "$attackerPoints", inline = true),
					Embed.Field("$defenderName's Points,", "$defenderPoints", inline = true),
				)
			)
		)

		val totalWidth = lineBreak(48).minecraftLength + 8

		val totalPoints = (defenderPoints + attackerPoints).toDouble()
		val attackerWidth = ((attackerPoints.toDouble() / max(totalPoints, 1.0)) * SIEGE_INFO_WIDTH).roundToInt()
		val defenderWidth = ((defenderPoints.toDouble() / max(totalPoints, 1.0)) * SIEGE_INFO_WIDTH).roundToInt()

		val attackerColor = FrontierNationCache[attacker].textColor
		val defenderColor = FrontierNationCache[defender].textColor

		val guiText = GuiText("", guiWidth = totalWidth, initialShiftDown = -1)

		guiText.add(text(repeatString("=", attackerWidth), attackerColor), -1, GuiText.TextAlignment.LEFT)
		guiText.add(text(repeatString("=", defenderWidth), defenderColor), -1, GuiText.TextAlignment.RIGHT)

		guiText.add(attackerNameFormatted, 0, GuiText.TextAlignment.LEFT)
		guiText.add(defenderNameFormatted, 0, GuiText.TextAlignment.RIGHT)

		guiText.add(text(attackerPoints, attackerColor), 1, GuiText.TextAlignment.LEFT)
		guiText.add(text(defenderPoints, defenderColor), 1, GuiText.TextAlignment.RIGHT)

		val headerLine = template(text("{0}'s siege of {1}'s Frontier Nation {2} has suceeded", YELLOW), attackerNameFormatted, defenderNameFormatted, region.name)

		val globalMessage = ofChildren(
			headerLine, newline(),
			guiText.build(),
			newline(),
			newline(),
			newline(),
		)

		Notify.allOnline(globalMessage)
		FrontierNation.delete(defender)
		FrontierNation.updatePoints(attacker,20)
	}

	fun fail(disableEarlyCheck: Boolean = false) {
		if (isPreparationPeriod() && !disableEarlyCheck) {
			Notify.chatAndEvents(template(
				text("{0} has abandoned their upcoming siege of {1}'s Frontier Nation in {2}"),
				attackerNameFormatted,
				defenderNameFormatted,
				region.world
			))
			return
		}

		val attackerName = FrontierNationCache[attacker].name
		val defenderName = FrontierNationCache[defender].name

		Discord.sendEmbed(
			ConfigurationFiles.discordSettings().eventsChannel,
			Embed(
				title = "Frontier Siege Failure",
				description = "$attackerName's siege of $defenderName's Frontier Territory in ${region.world} has failed.",
				fields = listOf(
					Embed.Field("$attackerName's Points,", "$attackerPoints", inline = true),
					Embed.Field("$defenderName's Points,", "$defenderPoints", inline = true),
				)
			)
		)

		val totalWidth = lineBreak(48).minecraftLength + 8

		val totalPoints = (defenderPoints + attackerPoints).toDouble()
		val attackerWidth = ((attackerPoints.toDouble() / max(totalPoints, 1.0)) * SIEGE_INFO_WIDTH).roundToInt()
		val defenderWidth = ((defenderPoints.toDouble() / max(totalPoints, 1.0)) * SIEGE_INFO_WIDTH).roundToInt()

		val attackerColor = FrontierNationCache[attacker].textColor
		val defenderColor = FrontierNationCache[defender].textColor

		val guiText = GuiText("", guiWidth = totalWidth, initialShiftDown = -1)

		guiText.add(text(repeatString("=", attackerWidth), attackerColor), -1, GuiText.TextAlignment.LEFT)
		guiText.add(text(repeatString("=", defenderWidth), defenderColor), -1, GuiText.TextAlignment.RIGHT)

		guiText.add(attackerNameFormatted, 0, GuiText.TextAlignment.LEFT)
		guiText.add(defenderNameFormatted, 0, GuiText.TextAlignment.RIGHT)

		guiText.add(text(attackerPoints, attackerColor), 1, GuiText.TextAlignment.LEFT)
		guiText.add(text(defenderPoints, defenderColor), 1, GuiText.TextAlignment.RIGHT)

		val headerLine = template(text("{0}'s siege of {1}'s Frontier Nation {2} has failed", YELLOW), attackerNameFormatted, defenderNameFormatted, region.name)

		val globalMessage = ofChildren(
			headerLine, newline(),
			guiText.build(),
			newline(),
			newline(),
			newline(),
		)

		Notify.allOnline(globalMessage)

		for (player in FrontierNation.getMembers(defender)) SLXP.addPowerAsync(player.uuid, 40)
	}

	private fun scheduleStart() {
		val startupTask = runnable {
			taskIds.remove(taskId)

			Notify.chatAndGlobal(template(text("{0} has begun.", HE_MEDIUM_GRAY), formatName()))
			Discord.sendEmbed(
				ConfigurationFiles.discordSettings().eventsChannel, Embed(
				title = "Frontier Siege Start",
				description = "${formatName().plainText()} has begun. It will end <t:${TimeUnit.MILLISECONDS.toSeconds(getSiegeEnd())}:R>."
			))

			FrontierNationSieges.setActive(this@FrontierNationSiege)
			scheduleEnd()
		}

		Tasks.asyncAt(Date(getActivePeriodStart()), startupTask)

		taskIds.add(startupTask.taskId)
	}

	fun scheduleEnd() {
		val endTask = runnable {
			taskIds.remove(taskId)

			endSiege()
		}

		Tasks.asyncAt(Date(getSiegeEnd()), endTask)

		taskIds.add(endTask.taskId)
	}

	fun scheduleTasks() {
		if (isPreparationPeriod()) {
			FrontierNationSieges.setPreparing(this)
			scheduleStart()
		}

		if (isActivePeriod()) {
			scheduleEnd()
		}
	}

	fun removeActive() {
		for (taskId in taskIds) Bukkit.getScheduler().cancelTask(taskId)
		FrontierNationSieges.removeActive(this)
		FrontierNationSiegeData.markComplete(databaseId)
	}

	val defenderNameFormatted get() = formatFrontierNationName(defender)
	val attackerNameFormatted get() = formatFrontierNationName(attacker)

}
