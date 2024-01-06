package net.horizonsend.ion.proxy.wrappers

import net.horizonsend.ion.proxy.IonProxy
import net.md_5.bungee.api.scheduler.TaskScheduler
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

class WrappedScheduler(private val proxy: IonProxy, private val inner: TaskScheduler) {
	fun delay(delay: Long, timeUnit: TimeUnit, block: () -> Unit) = inner.schedule(proxy, block, delay, timeUnit)

	fun repeat(delay: Long, repeat: Long, timeUnit: TimeUnit, block: () -> Unit) {
		inner.schedule(proxy, block, delay, repeat, timeUnit)
	}

	fun repeat(delay: Long, repeat: Long, timeUnit: TimeUnit, runnable: Runnable) {
		inner.schedule(proxy, runnable, delay, repeat, timeUnit)
	}

	fun async(runnable: Runnable) {
		inner.runAsync(proxy, runnable)
	}

	fun async(block: () -> Unit) {
		inner.runAsync(proxy, block)
	}

	companion object {
		fun namedThreadFactory(prefix: String) = object : ThreadFactory {
			private var counter: Int = 0

			override fun newThread(r: Runnable): Thread {
				return Thread(r, "$prefix-${counter++}")
			}
		}
	}
}
