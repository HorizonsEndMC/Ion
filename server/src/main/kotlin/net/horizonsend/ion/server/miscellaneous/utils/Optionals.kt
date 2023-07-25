package net.horizonsend.ion.server.miscellaneous.utils

import java.util.Optional

fun <T> Optional<T>.orNull(): T? = orElse(null)

fun <T : Any> optional(t: T?): Optional<T> = Optional.ofNullable(t)
