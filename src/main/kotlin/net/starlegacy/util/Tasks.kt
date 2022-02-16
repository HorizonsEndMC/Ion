package net.starlegacy.util

import co.aikar.timings.Timing
import java.time.ZonedDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.ThreadFactory
import net.starlegacy.PLUGIN
import net.starlegacy.StarLegacy
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

object Tasks {
	fun checkMainThread() = check(Bukkit.isPrimaryThread()) { "Attempted to call non-thread-safe method async!" }

	fun async(block: () -> Unit) {; asyncTask(block); }
	fun asyncTask(block: () -> Unit): BukkitTask = Bukkit.getScheduler().runTaskAsynchronously(PLUGIN, block)

	fun asyncDelay(delay: Long, block: () -> Unit) {; asyncDelayTask(delay, block); }
	fun asyncDelayTask(delay: Long, block: () -> Unit): BukkitTask =
		Bukkit.getScheduler().runTaskLaterAsynchronously(PLUGIN, block, delay)

	fun asyncRepeat(delay: Long, interval: Long, block: () -> Unit) {; asyncRepeatTask(delay, interval, block); }
	fun asyncRepeatTask(delay: Long, interval: Long, block: () -> Unit): BukkitTask =
		Bukkit.getScheduler().runTaskTimerAsynchronously(PLUGIN, block, delay, interval)

	inline fun sync(crossinline block: () -> Unit) {; syncTask(block); }
	inline fun syncTask(crossinline block: () -> Unit): BukkitTask {
		return Bukkit.getScheduler().runTask(StarLegacy.PLUGIN, Runnable { block() })
	}

	inline fun syncDelay(delay: Long, crossinline block: () -> Unit) {; syncDelayTask(delay, block); }
	inline fun syncDelayTask(delay: Long, crossinline block: () -> Unit): BukkitTask {
		return Bukkit.getScheduler().runTaskLater(StarLegacy.PLUGIN, Runnable { block() }, delay)
	}

	inline fun syncRepeat(delay: Long, interval: Long, crossinline block: () -> Unit) {; syncRepeatTask(
		delay,
		interval,
		block
	); }

	inline fun syncRepeatTask(delay: Long, interval: Long, crossinline block: () -> Unit): BukkitTask =
		Bukkit.getScheduler().runTaskTimer(StarLegacy.PLUGIN, Runnable { block() }, delay, interval)

	fun syncTimed(timing: Timing, block: () -> Unit): Unit = sync { timing.time(block) }

	/**
	 * @param hour Hour of day, 0-23
	 */
	fun asyncAtHour(hour: Int, block: () -> Unit) {
		require(hour in 0..23)

		val now: ZonedDateTime = ZonedDateTime.now()

		var time: ZonedDateTime = now.withHour(hour)

		if (time.isBefore(now) || time.isEqual(now)) {
			time = now.plusDays(1).withHour(hour)
		}

		val delay = (time.toEpochSecond() * 1000L) - System.currentTimeMillis()
		check(delay > 0)

		println("SCHEDULED TASK FOR $time, SUPPLIED HOUR OF DAY $hour, ACTUAL HOUR OF DAY ${time.hour}")

		asyncDelay(delay / 50L, block)
	}

	fun bukkitRunnable(block: BukkitRunnable.() -> Unit): BukkitRunnable = object : BukkitRunnable() {
		override fun run(): Unit = block()
	}

	fun asyncBukkitRunnable(block: BukkitRunnable.() -> Unit): BukkitTask = object : BukkitRunnable() {
		override fun run(): Unit = block()
	}.runTaskAsynchronously(PLUGIN)

	fun <T> getSync(block: () -> T): Future<T> = if (Bukkit.isPrimaryThread()) {
		CompletableFuture.completedFuture(block())
	} else {
		Bukkit.getScheduler().callSyncMethod(PLUGIN, block)
	}

	fun <T> getSyncBlocking(block: () -> T): T = getSync(block).get()

	fun syncBlocking(block: () -> Unit) = getSyncBlocking(block)

	fun namedThreadFactory(prefix: String) = object : ThreadFactory {
		private var counter: Int = 0

		override fun newThread(r: Runnable): Thread {
			return Thread(r, "$prefix-${counter++}")
		}
	}
}
