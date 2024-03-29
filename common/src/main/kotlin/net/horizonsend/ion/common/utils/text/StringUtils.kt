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
