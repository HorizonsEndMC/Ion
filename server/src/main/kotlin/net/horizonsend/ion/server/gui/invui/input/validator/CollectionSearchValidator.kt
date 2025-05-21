package net.horizonsend.ion.server.gui.invui.input.validator

import net.horizonsend.ion.common.utils.text.searchEntriesMultipleTerms
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.RED

class CollectionSearchValidator<T : Any>(val collection: Collection<T>, private val searchTermProvider: (T) -> Collection<String>) : InputValidator<T> {
	override fun isValid(input: String): ValidatorResult<T> {
		val search = searchEntriesMultipleTerms(input, collection, searchTermProvider)
		if (search.isEmpty()) return ValidatorResult.FailureResult(Component.text("No results found for input \"$input\".", RED))
		if (search.size == 1) return ValidatorResult.ValidatorSuccessSingleEntry(search.first())
		return ValidatorResult.ValidatorSuccessMultiEntry(search)
	}
}
