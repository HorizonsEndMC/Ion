package net.horizonsend.ion.common

interface Reloadable {
	fun reload() {
		onLoad()
		onReload()
	}

	fun load() = onLoad()

	fun onLoad() {}

	fun onReload() {}
}