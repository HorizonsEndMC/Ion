package net.horizonsend.ion.proxy.utils

import net.horizonsend.ion.proxy.PLUGIN
import net.md_5.bungee.api.scheduler.ScheduledTask
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

object ProxyTask {
	inline fun sync(crossinline block: () -> Unit): ScheduledTask = PLUGIN.proxy.scheduler.schedule(
			PLUGIN,
			{ block() },
		0,
			TimeUnit.SECONDS
		)

	inline fun async(crossinline block: () -> Unit): ScheduledTask = PLUGIN.proxy.scheduler.runAsync(PLUGIN) { block() }

	fun namedThreadFactory(prefix: String) = object : ThreadFactory {
		private var counter: Int = 0

		override fun newThread(r: Runnable): Thread {
			return Thread(r, "$prefix-${counter++}")
		}
	}
}
