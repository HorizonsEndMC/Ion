package net.starlegacy.util

import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import org.bukkit.Bukkit
import java.io.IOException
import java.net.URL
import java.util.UUID

data class User(val data: Data)
data class Data(val texture: Texture)
data class Texture(val value: String, val signature: String)

object Skins {
	data class SkinData(val value: String, val signature: String) {
		companion object {
			fun fromBytes(bytes: ByteArray): SkinData = Gson().fromJson(ungzip(bytes), SkinData::class.java)
		}

		fun toBytes(): ByteArray = gzip(Gson().toJson(this))
	}

	operator fun get(id: UUID): SkinData? {
		try {
			// use mojang thing is possible
			val profile: PlayerProfile = Bukkit.getPlayer(id)?.playerProfile
				?: Bukkit.createProfile(id).apply { complete(true) }

			if (profile.hasProperty("textures")) {
				val textureProperty: ProfileProperty = profile.properties.first { it.name == "textures" }
				return SkinData(textureProperty.value, textureProperty.signature!!)
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

			return SkinData(response.data.texture.value, response.data.texture.value)
		} catch (e: IOException) {
			return null
		}
	}
}
