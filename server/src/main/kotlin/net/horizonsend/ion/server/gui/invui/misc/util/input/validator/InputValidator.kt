package net.horizonsend.ion.server.gui.invui.misc.util.input.validator

fun interface InputValidator<T : Any> {
	fun isValid(input: String): ValidatorResult<T>
}
