package net.horizonsend.ion.proxy.utils

import net.horizonsend.ion.common.utils.Mutes

object ProxyMutes : Mutes() {
	override fun runWhenInitialized(block: () -> Unit) {
		block.invoke()
	}
}
