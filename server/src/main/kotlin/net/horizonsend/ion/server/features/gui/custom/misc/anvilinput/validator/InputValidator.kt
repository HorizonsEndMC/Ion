package net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.validator

fun interface InputValidator<T : Any> {
	fun isValid(input: String): ValidatorResult<T>
}
