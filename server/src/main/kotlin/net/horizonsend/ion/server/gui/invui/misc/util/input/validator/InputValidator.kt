package net.horizonsend.ion.server.gui.invui.misc.util.input.validator

import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

fun interface InputValidator<T : Any> {
	fun isValid(input: String): ValidatorResult<T>

	fun validateAsync(input: String): Future<ValidatorResult<T>> {
		val future = CompletableFuture<ValidatorResult<T>>()

		Tasks.async {
			future.complete(isValid(input))
		}

		return future
	}

	companion object {
		fun alwaysTrue(): InputValidator<String> = InputValidator { ValidatorResult.ValidatorSuccessSingleEntry(it) }
	}
}
