package net.horizonsend.ion.proxy.managers

import java.net.URL

object ResourcePackDownloadManager {
	lateinit var resourcePackTag: String
		private set

	fun update() {
		// Laziest JSON "Parsing" Ever
		resourcePackTag = URL("https://api.github.com/repos/HorizonsEndMC/ResourcePack/releases/latest")
			.readText()
			.substringAfter("\",\n\t\"tag_name\": \"")
			.substringBefore("\",\n\t")
	}
}