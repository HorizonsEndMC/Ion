package net.horizonsend.ion.proxy.utils

import net.horizonsend.ion.proxy.PLUGIN
import net.md_5.bungee.api.scheduler.ScheduledTask
import java.util.concurrent.TimeUnit

object ProxyTask {
	inline fun sync(crossinline block: () -> Unit): ScheduledTask = PLUGIN.proxy.scheduler.schedule(
			PLUGIN,
			{ block() },
			0,
			0,
			TimeUnit.SECONDS
		)
}
