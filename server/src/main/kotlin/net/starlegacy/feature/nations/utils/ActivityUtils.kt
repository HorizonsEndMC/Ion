package net.starlegacy.feature.nations.utils

import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.uuid
import net.starlegacy.feature.nations.NATIONS_BALANCE
import org.bukkit.Bukkit
import org.bukkit.ChatColor.GRAY
import org.bukkit.ChatColor.GREEN
import org.bukkit.ChatColor.RED
import java.util.Date
import java.util.concurrent.TimeUnit

val INACTIVE_BEFORE_TIME
	get() = Date(
		System.currentTimeMillis() - TimeUnit.DAYS.toMillis(NATIONS_BALANCE.settlement.inactivityDays.toLong())
	)

val ACTIVE_AFTER_TIME
	get() = Date(
		System.currentTimeMillis() - TimeUnit.DAYS.toMillis(NATIONS_BALANCE.settlement.activityDays.toLong())
	)

fun isActive(date: Date): Boolean = !date.before(ACTIVE_AFTER_TIME)

fun isSemiActive(date: Date): Boolean = !isActive(date) && !date.before(INACTIVE_BEFORE_TIME)

fun isInactive(date: Date): Boolean = date.before(INACTIVE_BEFORE_TIME)

fun getInactiveTimeText(id: SLPlayerId, lastSeen: Date): String {
	val time: Long = System.currentTimeMillis() - lastSeen.time

	val prefix: String = when {
		Bukkit.getPlayer(id.uuid) != null -> "${GREEN}Online"
		else -> "${RED}Offline"
	}

	return "$prefix$GRAY for ${getDurationBreakdown(time)}"
}

// https://stackoverflow.com/questions/625433/how-to-convert-milliseconds-to-x-mins-x-seconds-in-java
/**
 * Convert a millisecond duration to a string format
 *
 * @param input A duration to convert to a string form
 * @return A string of the form "X Days Y Hours Z Minutes A Seconds".
 */
private fun getDurationBreakdown(input: Long): String {
	var millis: Long = input

	if (millis < 0) {
		throw IllegalArgumentException("Duration must be greater than zero!")
	}

	val days: Long = TimeUnit.MILLISECONDS.toDays(millis)
	millis -= TimeUnit.DAYS.toMillis(days)

	val hours: Long = TimeUnit.MILLISECONDS.toHours(millis)
	millis -= TimeUnit.HOURS.toMillis(hours)

	val minutes: Long = TimeUnit.MILLISECONDS.toMinutes(millis)
	millis -= TimeUnit.MINUTES.toMillis(minutes)

	val seconds: Long = TimeUnit.MILLISECONDS.toSeconds(millis)

	return "$days Days $hours Hours $minutes Minutes $seconds Seconds"
}
