package net.horizonsend.ion.common.utils.input

import net.horizonsend.ion.common.utils.text.formatException
import net.horizonsend.ion.common.utils.text.ofChildren
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

class FutureInputResult : Future<InputResult>, PotentiallyFutureResult {
	private val future = CompletableFuture<InputResult>()

	fun complete(result: InputResult) {
		future.complete(result)
	}

	override fun sendReason(audience: Audience) {
		sendWhenComplete(audience)
	}

	override fun withResult(consumer: Consumer<InputResult>) {
		future.whenComplete { result: InputResult?, exception: Throwable? ->
			if (exception != null) {
				consumer.accept(
					InputResult.FailureReason(
						listOf(
							ofChildren(
								text("Sorry, there was an error getting the result. Please forward this to staff:", RED),
								formatException(exception)
							)
						)
					)
				)

				return@whenComplete
			}

			result?.let(consumer::accept)
		}
	}

	/**
	 * Halts the thread until the future is complete
	 **/
	fun wait() = future.get()

	fun sendWhenComplete(audience: Audience) {
		withResult { it.sendReason(audience) }
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
