package net.starlegacy.cache

import net.starlegacy.PLUGIN
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class ManualCache : Cache {
	internal inline val plugin get() = PLUGIN

	protected val log: Logger = LoggerFactory.getLogger(javaClass)

	abstract override fun load()
}