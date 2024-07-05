package net.horizonsend.ion.proxy.wrappers

import com.velocitypowered.api.scheduler.Scheduler
import net.horizonsend.ion.proxy.IonProxy
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

class WrappedScheduler(private val proxy: IonProxy, private val inner: Scheduler) {
	fun delay(delay: Long, timeUnit: TimeUnit, block: () -> Unit) = inner
		.buildTask(proxy, block)
		.delay(delay, timeUnit)
		.schedule()

	fun repeat(delay: Long, repeat: Long, timeUnit: TimeUnit, block: () -> Unit) = inner
		.buildTask(proxy, block)
		.delay(delay, timeUnit)
		.repeat(repeat, timeUnit)
		.schedule()

	fun async(block: () -> Unit) = inner
		.buildTask(proxy, block)
		.schedule()

	companion object {
		fun namedThreadFactory(prefix: String) = object : ThreadFactory {
			private var counter: Int = 0

			override fun newThread(r: Runnable): Thread {
				return Thread(r, "$prefix-${counter++}")
			}
		}
	}
}
