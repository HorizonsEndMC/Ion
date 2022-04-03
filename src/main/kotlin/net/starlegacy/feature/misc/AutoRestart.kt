package net.starlegacy.feature.misc

import net.starlegacy.SLComponent

object AutoRestart : SLComponent() {
	var isRestart = true

	override fun supportsVanilla(): Boolean {
		return true
	}
}
