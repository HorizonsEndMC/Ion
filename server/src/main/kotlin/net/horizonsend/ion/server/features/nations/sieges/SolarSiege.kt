package net.horizonsend.ion.server.features.nations.sieges

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.NationRelation
import net.horizonsend.ion.common.database.schema.nations.SolarSiegeData
import net.horizonsend.ion.common.database.schema.nations.SolarSiegeZone
import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.formatNationName
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.nations.region.types.RegionSolarSiegeZone
import net.horizonsend.ion.server.miscellaneous.utils.Discord
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.runnable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Bukkit
import java.util.Date
import java.util.concurrent.TimeUnit

class SolarSiege(
	val databaseId: Oid<SolarSiegeData>,
	val region: RegionSolarSiegeZone,
	val attacker: Oid<Nation>,
	attackerPoints: Int = 0,
	val defender: Oid<Nation>,
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

	fun saveSiegeData() = SolarSiegeData.updatePoints(databaseId, attackerPoints, defenderPoints)

	fun formatName(): Component {
		return template(
			text("{0}'s siege of {1}'s Solar Siege Zone in {2}", HE_MEDIUM_GRAY),
			formatNationName(attacker),
			region.nation?.let { formatNationName(it) } ?: text("None"),
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
		return declaredTime + TimeUnit.HOURS.toMillis(3)
	}

	fun getSiegeEnd(): Long {
		return getActivePeriodStart() + TimeUnit.MINUTES.toMillis(90)
	}

	fun endSiege() {
		handleEnd()
		if (isSuccess()) succeed() else fail()
	}

	private fun isSuccess(): Boolean {
		return attackerPoints > defenderPoints
	}

	fun succeed() {
		SolarSiegeZone.setNation(region.id, attacker)
	}

	fun fail() {

	}

	private fun scheduleStart() {
		val startupTask = runnable {
			taskIds.remove(taskId)

			Notify.chatAndEvents(template(text("{0} has begun.", HE_MEDIUM_GRAY), formatName()))
			Discord.sendEmbed(
				IonServer.discordSettings.eventsChannel, Embed(
				title = "Siege Start",
				description = "${formatName().plainText()} has begun. It will end <t:${TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(90))}:R>."
			))

			SolarSieges.setActive(this@SolarSiege)
		}

		Tasks.asyncAt(Date(getActivePeriodStart()), startupTask)

		taskIds.add(startupTask.taskId)
	}

	private fun scheduleEnd() {
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
			scheduleEnd()
		}

		if (isActivePeriod()) {

			SolarSieges.setActive(this)
			scheduleEnd()
		}
	}

	fun handleEnd() {
		for (taskId in taskIds) Bukkit.getScheduler().cancelTask(taskId)
		SolarSieges.removeActive(this)
		SolarSiegeData.markComplete(databaseId)
	}
}
