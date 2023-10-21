package net.horizonsend.ion.server.miscellaneous.utils

import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.gson.Gson
import net.horizonsend.ion.server.IonServerComponent
import org.bukkit.Bukkit
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Optional
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

data class User(val data: Data)
data class Data(val texture: Texture)
data class Texture(val value: String, val signature: String)

object Skins : IonServerComponent() {
	data class SkinData(val uuid: UUID, val value: String, val signature: String) {
		companion object {
			fun fromBytes(bytes: ByteArray): SkinData = Gson().fromJson(ungzip(bytes), SkinData::class.java)
		}

		fun toBytes(): ByteArray = gzip(Gson().toJson(this))
	}

	private val urlSkinCache: LoadingCache<String, Optional<SkinData>> = CacheBuilder.newBuilder().build(
		CacheLoader.from { url: String -> getFromURL(url)?.let { Optional.of(it) } ?: Optional.empty() }
	)
	private val uuidSkinCache: LoadingCache<UUID, Optional<SkinData>> = CacheBuilder.newBuilder().build(
		CacheLoader.from { uuid: UUID -> getFromUUID(uuid)?.let { Optional.of(it) } ?: Optional.empty() }
	)

	operator fun get(url: String): SkinData? = urlSkinCache[url].getOrNull()
	operator fun get(uuid: UUID): SkinData? = uuidSkinCache[uuid].getOrNull()

	private fun getFromURL(url: String): SkinData? {
		return try {
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
			val dataOutput = JSONParser().parse(reader) as JSONObject

			println(dataOutput.toJSONString())
			val data = dataOutput["data"] as JSONObject

			val uuid = data["uuid"] as String

			val texture = data["texture"] as JSONObject
			val textureEncoded = texture["value"] as String
			val signature = texture["signature"] as String

			connection.disconnect()

			SkinData(UUID.fromString(uuid), textureEncoded, signature)
		} catch (e: Exception) {
			log.warn(e.message)
			e.printStackTrace()
			e.cause?.let {
				log.warn(it.message)
				it.printStackTrace()
			}
			null
		}
	}

	private fun getFromUUID(id: UUID): SkinData? {
		try {
			// use mojang thing is possible
			val profile: PlayerProfile = Bukkit.getPlayer(id)?.playerProfile
				?: Bukkit.createProfile(id).apply { complete(true) }

			if (profile.hasProperty("textures")) {
				val textureProperty: ProfileProperty = profile.properties.first { it.name == "textures" }
				return SkinData(id, textureProperty.value, textureProperty.signature!!)
			}
		} catch (e: Exception) {
			// mojang's limits can do weird things, move on to the next method!
		}

		val url = "https://api.mineskin.org/generate/user/$id"

		try {
			val response: User

			URL(url)
				.openConnection()
				.apply {
					doInput = true
					connect()

					response = ObjectMapper().readValue(getInputStream(), User::class.java)
				}

			return SkinData(id, response.data.texture.value, response.data.texture.value)
		} catch (e: IOException) {
			return null
		}
	}
}
