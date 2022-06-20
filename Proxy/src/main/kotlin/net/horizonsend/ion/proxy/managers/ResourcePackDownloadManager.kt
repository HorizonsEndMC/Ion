package net.horizonsend.ion.proxy.managers

import java.net.URL

object ResourcePackDownloadManager {
	init { update() }

	lateinit var resourcePackTag: String
		private set

	fun update() {
		// Laziest JSON "Parsing" Ever
		resourcePackTag = URL("https://api.github.com/repos/HorizonsEndMC/ResourcePack/releases/latest")
			.readText()
			.substringAfter("\",\"tag_name\":\"")
			.substringBefore("\",")
	}
}