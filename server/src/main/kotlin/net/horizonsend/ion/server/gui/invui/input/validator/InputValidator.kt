package net.horizonsend.ion.server.gui.invui.input.validator

fun interface InputValidator<T : Any> {
	fun isValid(input: String): ValidatorResult<T>
}
