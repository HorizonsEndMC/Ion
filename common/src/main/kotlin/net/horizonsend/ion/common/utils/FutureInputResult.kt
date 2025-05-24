package net.horizonsend.ion.common.utils

import net.horizonsend.ion.common.utils.text.formatException
import net.horizonsend.ion.common.utils.text.ofChildren
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class FutureInputResult : Future<InputResult>, InputResult {
	private val future = CompletableFuture<InputResult>()

	fun complete(result: InputResult) {
		future.complete(result)
	}

	override fun sendReason(audience: Audience) {
		sendWhenComplete(audience)
	}

	override fun getReason(): List<Component>? {
		if (future.isDone) return future.get().getReason()
		return null
	}

	override fun isSuccess(): Boolean {
		if (future.isDone) return future.isDone
		return true
	}

	/**
	 * Halts the thread until the future is complete
	 **/
	fun wait() = future.get()

	fun sendWhenComplete(audience: Audience) {
		future.whenComplete { result: InputResult?, exception: Throwable? ->
			if (exception != null) {
				audience.sendMessage(ofChildren(
					text("Sorry, there was an error getting the result. Please forward this to staff:"),
					formatException(exception)
				))
			}

			result?.sendReason(audience)
		}
	}

	override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
		return future.cancel(mayInterruptIfRunning)
	}

	override fun isCancelled(): Boolean {
		return future.isCancelled
	}

	override fun isDone(): Boolean {
		return future.isDone
	}

	override fun get(): InputResult {
		return future.get()
	}

	override fun get(timeout: Long, unit: TimeUnit): InputResult {
		return future.get(timeout, unit)
	}
}
