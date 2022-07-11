package net.horizonsend.ion.proxy.managers

import java.util.UUID

object LinkManager {
	private val linkCodes = mutableMapOf<String, LinkCodeData>()

	private class LinkCodeData(val minecraftUUID: UUID, val issueTime: Long)

	fun createLinkCode(minecraftUUID: UUID): String {
		expireLinkCodes()

		val linkCode = (1..6).joinToString("") { "${('A'..'Z').random()}" } // Cursed
		linkCodes[linkCode] = LinkCodeData(minecraftUUID, System.currentTimeMillis())

		return linkCode
	}

	fun validateLinkCode(linkCode: String): UUID? {
		expireLinkCodes()

		return linkCodes.remove(linkCode)?.minecraftUUID
	}

	private fun expireLinkCodes() {
		// Also cursed
		linkCodes.filterValues {
			it.issueTime + 1000 * 60 * 5 < System.currentTimeMillis()
		}.keys.forEach {
			linkCodes.remove(it)
		}
	}
}