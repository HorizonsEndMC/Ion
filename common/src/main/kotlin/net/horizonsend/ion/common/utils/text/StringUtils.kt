package net.horizonsend.ion.common.utils.text

import java.util.Random
import kotlin.streams.asSequence

fun repeatString(string: String, count: Int): String {
	val builder = StringBuilder()

	for (x in 0 until count) {
		builder.append(string)
	}

	return builder.toString()
}

val alphaNumericChars : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
val vowels : CharArray = "aeiouAEIOU".toCharArray()

fun Char.isVowel() = vowels.contains(this)

fun randomString(length: Long, inputRandom: Random? = null): String {
	val random = inputRandom ?: Random()

	return random
		.ints(length, 0, alphaNumericChars.size)
		.asSequence()
		.map(alphaNumericChars::get)
		.joinToString("")
}

fun String.isAlphanumeric(includeSpaces: Boolean = false): Boolean {
	if (includeSpaces) return matches("^[a-zA-Z0-9 ]*$".toRegex())

	return matches("^[a-zA-Z0-9]*$".toRegex())
}

fun String.subStringBetween(start: Char, end: Char) = substringAfter(start).substringBefore(end)
fun String.subStringBetween(start: String, end: String) = substringAfter(start).substringBefore(end)

fun <T> searchEntriesSingleTerm(textInput: String, entries: Collection<T>, stringProvieder: (T) -> String): List<T> =
	searchEntriesMultipleTerms(textInput, entries) { entry: T -> listOf(stringProvieder(entry)) }

fun <T> searchEntriesMultipleTerms(textInput: String, entries: Collection<T>, searchTermProvider: (T) -> Collection<String>): List<T> {
	val split = textInput.split(' ')

	val searchMap = mutableMapOf<String, MutableList<T>>()

	for (entry in entries) {
		val searchTerms = searchTermProvider.invoke(entry)

		for (term in searchTerms) {
			searchMap.getOrPut(term) { mutableListOf() }.add(entry)
		}
	}

	val matchingKeys = searchMap.keys.filter { searchKey ->
		split.all { splitInput -> searchKey.contains(splitInput, ignoreCase = true) }
	}

	return matchingKeys.flatMap { searchMap.getOrDefault(it, mutableListOf()) }.distinct()
}
