package net.horizonsend.ion.proxy.wrappers

import net.horizonsend.ion.proxy.IonProxy
import net.md_5.bungee.api.scheduler.TaskScheduler
import java.util.concurrent.TimeUnit

class WrappedScheduler(private val proxy: IonProxy, private val inner: TaskScheduler) {
	fun delay(delay: Long, timeUnit: TimeUnit, block: () -> Unit) = inner.schedule(proxy, block, delay, timeUnit)

	fun repeat(delay: Long, repeat: Long, timeUnit: TimeUnit, block: () -> Unit) {
		inner.schedule(proxy, block, delay, repeat, timeUnit)
	}
	fun repeat(delay: Long, repeat: Long, timeUnit: TimeUnit, runnable: Runnable) {
		inner.schedule(proxy, runnable, delay, repeat, timeUnit)
	}
}
