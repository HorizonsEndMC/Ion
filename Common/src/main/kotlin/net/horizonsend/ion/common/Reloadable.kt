package net.horizonsend.ion.common

abstract class Reloadable {
	private var hasLoaded = false

	fun load() {
		onLoad()
		if (hasLoaded) onReload()
		hasLoaded = true
	}

	fun unload() = onUnload()

	protected open fun onLoad() {}
	protected open fun onReload() {}
	protected open fun onUnload() {}
}