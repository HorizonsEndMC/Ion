package net.horizonsend.ion.discord.utils

import net.horizonsend.ion.common.IonComponent
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

object IonDiscordScheduler : IonComponent() {
	lateinit var thread: ScheduledExecutorService

	override fun onEnable() {
		thread = Executors.newSingleThreadScheduledExecutor(namedThreadFactory("ion-scheduler"))
	}

	/**  */
	inline fun run(crossinline function: () -> Unit) {
		thread.submit { function.invoke() }
	}

	/** Schedule the task to be run async after the specific delay in milliseconds */
	inline fun asyncDelay(delay: Long, crossinline function: () -> Unit) {
		thread.schedule({ function.invoke() }, delay, TimeUnit.MILLISECONDS)
	}

	/** Schedule the task to be run at the specified interval after the delay in milliseconds */
	inline fun asyncRepeat(delay: Long, repeat: Long, crossinline function: () -> Unit) {
		thread.scheduleAtFixedRate({ function.invoke() }, delay, repeat, TimeUnit.MILLISECONDS)
	}

	fun namedThreadFactory(prefix: String) = object : ThreadFactory {
		private var counter: Int = 0

		override fun newThread(r: Runnable): Thread {
			return Thread(r, "$prefix-${counter++}")
		}
	}
}
