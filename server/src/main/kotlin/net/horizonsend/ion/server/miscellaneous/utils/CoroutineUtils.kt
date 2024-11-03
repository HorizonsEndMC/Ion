package net.horizonsend.ion.server.miscellaneous.utils

import kotlinx.coroutines.Deferred

suspend fun <K, V> Map<K, Deferred<V>>.awaitAllValues(): Map<K, V> = if (isEmpty()) mapOf() else mapValues { (_, v) -> v.await() }
