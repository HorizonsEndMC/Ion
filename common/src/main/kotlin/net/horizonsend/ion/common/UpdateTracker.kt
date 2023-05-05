package net.horizonsend.ion.common

import java.io.File

fun getUpdateMessage(dataFolder: File): String? {
	val versionFile = dataFolder.resolve("version")

	val version = try { versionFile.readText().trim() } catch (_: Exception) { null }
	val jarVersion = ::getUpdateMessage::class.java.getResourceAsStream("/gitHash")!!.reader().readText().trim()

	if (version == jarVersion) return null

	val message = when (version == null) {
		true -> {
			val commitUrl = "https://github.com/HorizonsEndMC/Ion/commit/$jarVersion"
			"is running $jarVersion, see the commit here: $commitUrl. The last version could not be determined."
		}
		false -> {
			val compareUrl = "https://github.com/HorizonsEndMC/Ion/compare/$version...$jarVersion"
			"was updated from $version to $jarVersion, see changes made here: $compareUrl"
		}
	}

	try { versionFile.writeText(jarVersion) } catch (_: Exception) {}

	return message
}
