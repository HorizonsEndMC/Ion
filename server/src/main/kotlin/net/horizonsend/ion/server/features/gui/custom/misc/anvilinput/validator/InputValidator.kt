package net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.validator

fun interface InputValidator {
	fun isValid(input: String): ValidatorResult
}
