package net.starlegacy.util

import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

fun Double.roundToHundredth(): Double = times(100.0).roundToInt().toDouble().div(100.0)

@Suppress("NOTHING_TO_INLINE")
inline fun Int.squared(): Int = this * this

@Suppress("NOTHING_TO_INLINE")
inline fun Double.squared(): Double = this * this

@Suppress("NOTHING_TO_INLINE")
inline fun Float.squared(): Float = this * this

/**
 * Formats a number to US-locale style.
 */
fun Number.toText(): String = NumberFormat.getNumberInstance(Locale.US).format(this)

/**
 * Formats the number into credit format, so it is rounded to the nearest hundredth,
 * commas are placed every 3 digits to the left of the decimal point,
 * and "C" is placed at the beginning of the string.
 */
fun Number.toCreditsString(): String = "C${toDouble().roundToHundredth().toText()}"

fun randomDouble(min: Double, max: Double) = ThreadLocalRandom.current().nextDouble(min, max)

fun randomFloat() = ThreadLocalRandom.current().nextFloat()

/**
 * @param min Minimum (inclusive)
 * @param max Maximum (exclusive)
 */
fun randomInt(min: Int, max: Int) = ThreadLocalRandom.current().nextInt(min, max)

/**
 * @param min Minimum (inclusive)
 * @param max Maximum (inclusive)
 */
fun randomRange(min: Int, max: Int) = randomInt(min, max + 1)

@Suppress("NOTHING_TO_INLINE")
inline fun Number.d(): Double = this.toDouble()

@Suppress("NOTHING_TO_INLINE")
inline fun Number.i(): Int = this.toInt()

fun getDurationBreakdown(input: Long): String {
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
