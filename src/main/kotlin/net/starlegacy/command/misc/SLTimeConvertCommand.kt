package net.starlegacy.command.misc

import net.starlegacy.command.SLCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import net.starlegacy.util.msg
import org.bukkit.command.CommandSender
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@CommandAlias("sltimeconvert")
object SLTimeConvertCommand : SLCommand() {
    private val DAYS_IN_MONTHS: Array<Int> = arrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

    private fun getDaysInMonth(month: Int, year: Int): Int {
        // february changes on leap years
        if (month == 2) {
            return if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) 29 else 28
        }
        return DAYS_IN_MONTHS[month - 1]
    }

    private fun validateNumber(input: String, min: Int, max: Int, inputName: String): Int {
        val parsed = input.toIntOrNull() ?: fail { "$inputName $input is not a valid number" }
        failIf(parsed < min) { "$inputName must be at least $min" }
        failIf(parsed > max) { "$inputName must be at most $max" }
        return parsed
    }

    fun validateDate(date: String): ZonedDateTime {
        // allow either / or -
        val split = date.replace("/", "-").split("-")
        failIf(split.size != 3) { "Date must be in the format of month/day/year, e.g. 12/20/2018 (dashes also allowed)" }
        val year = validateNumber(split[2], Int.MIN_VALUE, Int.MAX_VALUE, "Year")
        val month = validateNumber(split[0], 1, 12, "Month")
        val daysInMonth = getDaysInMonth(month, year)
        val day = validateNumber(split[1], 1, daysInMonth, "Day")
        return ZonedDateTime.of(year, month, day, 0, 0, 0, 0, zone)
    }

    private val zone = ZoneId.of("America/New_York")

    private val REAL_EPOCH: ZonedDateTime = ZonedDateTime.of(2013, 1, 1, 0, 0, 0, 0, zone)
    private val SL_EPOCH: ZonedDateTime = ZonedDateTime.of(2300, 1, 1, 0, 0, 0, 0, zone)

    /**
     * @param realSeconds Epoch second of real time
     * @return Epoch second of SL time
     */
    private fun realToSL(realSeconds: Long): Long {
        return (realSeconds - REAL_EPOCH.toEpochSecond()) * 100 + SL_EPOCH.toEpochSecond()
    }

    /**
     * @param slSeconds Epoch second of SL time
     * @return Epoch second of real time
     */
    private fun slToReal(slSeconds: Long): Long {
        return (slSeconds - SL_EPOCH.toEpochSecond()) / 100 + REAL_EPOCH.toEpochSecond()
    }

    private fun toSeconds(zonedDateTime: ZonedDateTime): Long = zonedDateTime.toEpochSecond()

    private fun toDateTime(seconds: Long) = ZonedDateTime.ofInstant(Instant.ofEpochSecond(seconds), zone)

    private fun format(date: ZonedDateTime, includeTime: Boolean): String {
        return date.format(DateTimeFormatter.ofPattern(if (!includeTime) "MM/dd/yyyy" else "MM/dd/yyyy kk:mm"))
            .replace("24:", "00:") // shouldn't it be 00:00 at midnight? :P
    }

    @Subcommand("sl")
    @Description("Get approximate SL date from IRL date (EST timezone)")
    fun sl(sender: CommandSender, irlDate: String) {
        val realDate: ZonedDateTime = validateDate(irlDate)

        val realTimeMin: ZonedDateTime = realDate
        val realTimeMax: ZonedDateTime = realDate.withHour(23).withMinute(59)

        val slTimeMin: ZonedDateTime = toDateTime(realToSL(toSeconds(realTimeMin)))
        val slTimeMax: ZonedDateTime = toDateTime(realToSL(toSeconds(realTimeMax)))

        sender msg "&6${format(realTimeMin, false)}&7 in real time is between " +
                "&b${format(slTimeMin, false)}&7 and &b${format(slTimeMax, false)}&7" +
                " in SL time"
    }

    @Subcommand("sl")
    @Description("Get exact SL date from IRL date and time (EST timezone, military time)")
    fun sl(sender: CommandSender, irlDate: String, irlTime: String) {
        val realDate: ZonedDateTime = validateDate(irlDate)
        val timeSplit = irlTime.split(":")

        failIf(timeSplit.size != 2 || timeSplit[0].length != 2 || timeSplit[1].length != 2) { "Time must be in the format 22:00" }

        val hour = validateNumber(timeSplit[0], 0, 23, "Hour")
        val minute = validateNumber(timeSplit[1], 0, 59, "Minute")

        val realTime: ZonedDateTime = realDate.withHour(hour).withMinute(minute)

        val slTime: ZonedDateTime = toDateTime(realToSL(toSeconds(realTime)))

        sender msg "&6${format(realTime, true)}&7 in real time is " +
                "&b${format(slTime, false)}&7 in SL time"
    }

    @Subcommand("irl")
    @Description("Get exact IRL date and approximate IRL time (EST timezone) from SL date")
    fun irl(sender: CommandSender, slDate: String) {
        val date: ZonedDateTime = validateDate(slDate)
        val realTime: ZonedDateTime = toDateTime(slToReal(toSeconds(date)))
        sender msg "&b${format(date, false)}&7 in SL time is approximately " +
                "&6${format(realTime, true)}&7 in real time"
    }
}
