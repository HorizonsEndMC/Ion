package net.starlegacy.cache

import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class ManualCache : Cache {
	protected val log: Logger = LoggerFactory.getLogger(javaClass)

	abstract override fun load()
}