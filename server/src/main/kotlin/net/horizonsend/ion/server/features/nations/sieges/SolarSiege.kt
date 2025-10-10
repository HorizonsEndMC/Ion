package net.horizonsend.ion.server.features.nations.sieges

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.NationRelation
import net.horizonsend.ion.common.database.schema.nations.SolarSiegeData
import net.horizonsend.ion.common.database.schema.nations.SolarSiegeZone
import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.formatNationName
import net.horizonsend.ion.common.utils.text.lineBreak
import net.horizonsend.ion.common.utils.text.minecraftLength
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.common.utils.text.repeatString
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.command.nations.SiegeCommand.SIEGE_INFO_WIDTH
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.chat.Discord
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.nations.region.types.RegionSolarSiegeZone
import net.horizonsend.ion.server.features.nations.sieges.SolarSieges.config
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.horizonsend.ion.server.miscellaneous.utils.runnable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import org.bukkit.Bukkit
import org.litote.kmongo.setValue
import java.time.Duration
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.roundToInt

class SolarSiege(
	val databaseId: Oid<SolarSiegeData>,
	val region: RegionSolarSiegeZone,
	val attacker: Oid<Nation>,
	attackerPoints: Int = 0,
	val defender: Oid<Nation>,
	defenderPoints: Int = 0,
	val declaredTime: Long,
	val availableRewards: MutableMap<String, Int> = mutableMapOf()
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

	fun saveSiegeData() = SolarSiegeData.updatePoints(databaseId, attackerPoints, defenderPoints)

	fun formatName(): Component {
		return template(
			text("{0}'s siege of {1}'s Solar Siege Zone in {2}", HE_MEDIUM_GRAY),
			attackerNameFormatted,
			defenderNameFormatted,
			region.world
		)
	}

	fun isAttacker(player: SLPlayerId): Boolean {
		val playerNation = PlayerCache[player].nationOid ?: return false

		return RelationCache[attacker, playerNation] >= NationRelation.Level.ALLY
	}

	fun isDefender(player: SLPlayerId): Boolean {
		val playerNation = PlayerCache[player].nationOid ?: return false

		return RelationCache[defender, playerNation] >= NationRelation.Level.ALLY
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
		SolarSiegeZone.setNation(region.id, attacker)

		disperseMaterialRewards()

		val attackerName = NationCache[attacker].name
		val defenderName = NationCache[defender].name

		Discord.sendEmbed(
			ConfigurationFiles.discordSettings().eventsChannel,
			Embed(
				title = "Siege Success",
				description = "$attackerName's siege of $defenderName's Solar Siege Zone in ${region.world} has succeeded.",
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

		val attackerColor = NationCache[attacker].textColor
		val defenderColor = NationCache[defender].textColor

		val guiText = GuiText("", guiWidth = totalWidth, initialShiftDown = -1)

		guiText.add(text(repeatString("=", attackerWidth), attackerColor), -1, GuiText.TextAlignment.LEFT)
		guiText.add(text(repeatString("=", defenderWidth), defenderColor), -1, GuiText.TextAlignment.RIGHT)

		guiText.add(attackerNameFormatted, 0, GuiText.TextAlignment.LEFT)
		guiText.add(defenderNameFormatted, 0, GuiText.TextAlignment.RIGHT)

		guiText.add(text(attackerPoints, attackerColor), 1, GuiText.TextAlignment.LEFT)
		guiText.add(text(defenderPoints, defenderColor), 1, GuiText.TextAlignment.RIGHT)

		val headerLine = template(text("{0}'s siege of {1}'s Solar Siege Zone {2} has suceeded", YELLOW), attackerNameFormatted, defenderNameFormatted, region.name)

		val globalMessage = ofChildren(
			headerLine, newline(),
			guiText.build(),
			newline(),
			newline(),
			newline(),
		)

		Notify.allOnline(globalMessage)
	}

	fun fail(disableEarlyCheck: Boolean = false) {
		if (isPreparationPeriod() && !disableEarlyCheck) {
			Notify.chatAndEvents(template(
				text("{0} has abandoned their upcoming siege of {1}'s Solar Siege Zone in {2}"),
				attackerNameFormatted,
				defenderNameFormatted,
				region.world
			))

			return
		}

//		Notify.chatAndGlobal()

		val attackerName = NationCache[attacker].name
		val defenderName = NationCache[defender].name

		Discord.sendEmbed(
			ConfigurationFiles.discordSettings().eventsChannel,
			Embed(
				title = "Siege Failure",
				description = "$attackerName's siege of $defenderName's Solar Siege Zone in ${region.world} has failed.",
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

		val attackerColor = NationCache[attacker].textColor
		val defenderColor = NationCache[defender].textColor

		val guiText = GuiText("", guiWidth = totalWidth, initialShiftDown = -1)

		guiText.add(text(repeatString("=", attackerWidth), attackerColor), -1, GuiText.TextAlignment.LEFT)
		guiText.add(text(repeatString("=", defenderWidth), defenderColor), -1, GuiText.TextAlignment.RIGHT)

		guiText.add(attackerNameFormatted, 0, GuiText.TextAlignment.LEFT)
		guiText.add(defenderNameFormatted, 0, GuiText.TextAlignment.RIGHT)

		guiText.add(text(attackerPoints, attackerColor), 1, GuiText.TextAlignment.LEFT)
		guiText.add(text(defenderPoints, defenderColor), 1, GuiText.TextAlignment.RIGHT)

		val headerLine = template(text("{0}'s siege of {1}'s Solar Siege Zone {2} has failed", YELLOW), attackerNameFormatted, defenderNameFormatted, region.name)

		val globalMessage = ofChildren(
			headerLine, newline(),
			guiText.build(),
			newline(),
			newline(),
			newline(),
		)

		Notify.allOnline(globalMessage)
	}

	private fun scheduleStart() {
		val startupTask = runnable {
			taskIds.remove(taskId)

			Notify.chatAndGlobal(template(text("{0} has begun.", HE_MEDIUM_GRAY), formatName()))
			Discord.sendEmbed(
				ConfigurationFiles.discordSettings().eventsChannel, Embed(
				title = "Siege Start",
				description = "${formatName().plainText()} has begun. It will end <t:${TimeUnit.MILLISECONDS.toSeconds(getSiegeEnd())}:R>."
			))

			SolarSieges.setActive(this@SolarSiege)
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
			SolarSieges.setPreparing(this)
			scheduleStart()
		}

		if (isActivePeriod()) {
			scheduleEnd()
		}
	}

	fun removeActive() {
		for (taskId in taskIds) Bukkit.getScheduler().cancelTask(taskId)
		SolarSieges.removeActive(this)
		SolarSiegeData.markComplete(databaseId)
	}

	val defenderNameFormatted get() = formatNationName(defender)
	val attackerNameFormatted get() = formatNationName(attacker)

	fun disperseMaterialRewards() {
		val rewards = SolarSiegeRewards.generateRewards(attackerPoints)
		availableRewards.putAll(rewards)
		SolarSiegeData.updateById(databaseId, setValue(SolarSiegeData::availableRewards, availableRewards))

		Tasks.async {
			val message = text()
				.append(template(text("For winning your siege of {0} against {1}, your nation has won:", HE_MEDIUM_GRAY), region.name, defenderNameFormatted))

			for ((material, amount) in availableRewards) {
				val rewardName = fromItemString(material).displayNameComponent
				message.append(newline(), ofChildren(rewardName, Component.text(": ", HEColorScheme.HE_DARK_GRAY), Component.text(amount, HEColorScheme.HE_LIGHT_GRAY)))
			}

			Notify.nationCrossServer(attacker, message.build())
		}
	}
}
