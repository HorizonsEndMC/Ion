package net.horizonsend.ion.server.miscellaneous.utils

import net.horizonsend.ion.server.IonServer
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.time.ZonedDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.ThreadFactory

object Tasks {
	fun checkMainThread() = check(Bukkit.isPrimaryThread()) { "Attempted to call non-thread-safe method async!" }

	fun asyncOrImmediate(immediate: Boolean = false, block: () -> Unit) = if (immediate) block() else async(block)
	fun syncOrImmediate(immediate: Boolean = false, block: () -> Unit) = if (immediate) block() else sync(block)

	fun async(block: () -> Unit) {
		Bukkit.getScheduler().runTaskAsynchronously(IonServer, block)
	}

	fun asyncDelay(delay: Long, block: () -> Unit) {
		Bukkit.getScheduler().runTaskLaterAsynchronously(IonServer, block, delay)
	}

	fun asyncRepeat(delay: Long, interval: Long, block: () -> Unit) {
		Bukkit.getScheduler().runTaskTimerAsynchronously(IonServer, block, delay, interval)
	}

	inline fun sync(crossinline block: () -> Unit) { ; syncTask(block); }
	inline fun syncTask(crossinline block: () -> Unit): BukkitTask {
		return Bukkit.getScheduler().runTask(IonServer, Runnable { block() })
	}

	inline fun syncDelay(delay: Long, crossinline block: () -> Unit) { ; syncDelayTask(delay, block); }
	inline fun syncDelayTask(delay: Long, crossinline block: () -> Unit): BukkitTask {
		return Bukkit.getScheduler().runTaskLater(IonServer, Runnable { block() }, delay)
	}

	inline fun syncRepeat(delay: Long, interval: Long, crossinline block: () -> Unit) =
		syncRepeatTask(delay, interval, block)

	inline fun syncRepeatTask(delay: Long, interval: Long, crossinline block: () -> Unit): BukkitTask =
		Bukkit.getScheduler().runTaskTimer(IonServer, Runnable { block() }, delay, interval)

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

		IonServer.slF4JLogger.info("SCHEDULED TASK FOR $time, SUPPLIED HOUR OF DAY $hour, ACTUAL HOUR OF DAY ${time.hour}")

		asyncDelay(delay / 50L, block)
	}

	fun bukkitRunnable(block: BukkitRunnable.() -> Unit): BukkitRunnable = object : BukkitRunnable() {
		override fun run(): Unit = block()
	}

	fun <T> getSync(block: () -> T): Future<T> = if (Bukkit.isPrimaryThread()) {
		CompletableFuture.completedFuture(block())
	} else {
		Bukkit.getScheduler().callSyncMethod(IonServer, block)
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
