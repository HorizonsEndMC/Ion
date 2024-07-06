package net.horizonsend.ion.proxy.configuration

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.UUID

@Serializable
data class TabConfiguration(
	val columns: Int = 3,
	val baseHeight: Int = 14,
	val usedIcon: HeadIcon = UrlSkin("https://assets.horizonsend.net/training_droid.png")
) {
	@Serializable
	sealed interface HeadIcon {
		fun get(): SkinData

		data class SkinData(
			val uuid: UUID,
			val texture: String,
			val signature: String
		)
	}

	@Serializable
	data class DefinedSkin(
		private val uuid: String,
		private val texture: String,
		private val signature: String
	) : HeadIcon {
		override fun get(): HeadIcon.SkinData {
			return HeadIcon.SkinData(UUID.fromString(uuid), texture, signature)
		}
	}

	@Serializable
	data class UrlSkin(val url: String) : HeadIcon {
		@Transient
		var skin: HeadIcon.SkinData? = null

		override fun get(): HeadIcon.SkinData {
			val stored = skin
			if (stored != null) return stored

			val connection: HttpURLConnection = URL("https://api.mineskin.org/generate/url")
				.openConnection()
				.apply {
					doOutput = true
					connect()
				} as HttpURLConnection

			val output = DataOutputStream(connection.outputStream)
			output.writeBytes("url=" + URLEncoder.encode(url, "UTF-8"))
			output.close()

			val reader = BufferedReader(InputStreamReader(connection.inputStream))
			val dataOutput = JsonParser.parseString(reader.readText()) as JsonObject

			val data = dataOutput["data"] as JsonObject

			val uuid = data["uuid"] as JsonElement

			val texture = data["texture"] as JsonObject
			val textureEncoded = texture["value"] as JsonElement
			val signature = texture["signature"] as JsonElement

			connection.disconnect()

			val skinData = HeadIcon.SkinData(UUID.fromString(uuid.asString), textureEncoded.asString, signature.asString)
			skin = skinData
			return skinData
		}
	}
}
