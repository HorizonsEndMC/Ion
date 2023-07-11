package net.horizonsend.ion.common.database.cache

import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class ManualCache : Cache {
	private val mutex = Any()
	protected fun synced(block: () -> Unit): Unit = synchronized(mutex, block)

	protected val log: Logger = LoggerFactory.getLogger(javaClass)

	abstract override fun load()
}
