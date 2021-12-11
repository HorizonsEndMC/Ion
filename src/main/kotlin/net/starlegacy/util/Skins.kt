package net.starlegacy.util

import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import com.google.gson.Gson
import khttp.get
import khttp.responses.Response
import org.bukkit.Bukkit
import org.json.JSONException
import org.json.JSONObject
import java.util.UUID

object Skins {
    data class SkinData(val value: String, val signature: String) {
        companion object {
            fun fromBytes(bytes: ByteArray): SkinData = Gson().fromJson(ungzip(bytes), Skins.SkinData::class.java)
        }

        fun toBytes(): ByteArray = gzip(Gson().toJson(this))
    }

    operator fun get(id: UUID): Skins.SkinData? {
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
            val response: Response = get(url)
            if (response.jsonObject.has("error")) {
                return null
            }
            val textureObject: JSONObject = response.jsonObject.getJSONObject("data").getJSONObject("texture")
            return Skins.SkinData(textureObject.getString("value"), textureObject.getString("signature"))
        } catch (e: JSONException) {
            return null
        }
    }
}
