package net.horizonsend.ion.common.utils

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import litebans.api.Database
import net.horizonsend.ion.common.IonComponent
import java.util.UUID
import java.util.concurrent.TimeUnit

object Mutes: IonComponent() {
	var isLitebansEnabled: Boolean = false
	lateinit var liteBansDatabase: Database

	override fun onEnable() {
		liteBansDatabase = try {
			val db = Database.get()
			isLitebansEnabled = true
			db
		} catch (e: Exception) { throw Error("Litebans is not installed. Mutes will not be checked, but chat will still function.") }
	}

	val muteCache: LoadingCache<UUID, Boolean> = CacheBuilder.newBuilder()
		.expireAfterAccess(5, TimeUnit.MINUTES)
		.build(
			CacheLoader.from { uuid ->
				if (!isLitebansEnabled) return@from false
				log.warn("Could not check for mute, LiteBans not enabled!")
				playerIsMuted(uuid)
			}
		)

	val banCache: LoadingCache<UUID, Boolean> = CacheBuilder.newBuilder()
		.expireAfterAccess(5, TimeUnit.MINUTES)
		.build(
			CacheLoader.from { uuid ->
				if (!isLitebansEnabled) return@from false
				log.warn("Could not check for ban, LiteBans not enabled!")
				playerIsBanned(uuid)
			}
		)

	fun playerIsMuted(playerId: UUID): Boolean = runCatching { liteBansDatabase.isPlayerMuted(playerId, null) }.getOrDefault(false)
	fun playerIsBanned(playerId: UUID): Boolean = runCatching { liteBansDatabase.isPlayerBanned(playerId, null) }.getOrDefault(false)
}
