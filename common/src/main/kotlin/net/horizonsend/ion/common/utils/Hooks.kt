package net.horizonsend.ion.common.utils

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import litebans.api.Database
import net.horizonsend.ion.common.IonComponent
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

abstract class Mutes: IonComponent() {
	var isLitebansEnabled: Boolean = false
	lateinit var liteBansDatabase: Database

	abstract fun runWhenInitialized(block: () -> Unit)

	override fun onEnable() = runWhenInitialized {
		liteBansDatabase = try {
			val db = Database.get()
			isLitebansEnabled = true
			db
		} catch (e: Exception) { throw Error("Litebans is not installed. Mutes will not be checked, but chat will still function.", e) }
	}

	private val muteCache: LoadingCache<UUID, Boolean> = CacheBuilder.newBuilder()
		.expireAfterAccess(5, TimeUnit.SECONDS)
		.build(
			CacheLoader.from { uuid ->
				if (!isLitebansEnabled) {
					log.warn("Could not check for mute, LiteBans not enabled!")
					return@from false
				}

				playerIsMuted(uuid)
			}
		)

	private val banCache: LoadingCache<UUID, Boolean> = CacheBuilder.newBuilder()
		.expireAfterAccess(5, TimeUnit.MINUTES)
		.build(
			CacheLoader.from { uuid ->
				if (!isLitebansEnabled) {
					log.warn("Could not check for ban, LiteBans not enabled!")
					return@from false
				}

				playerIsBanned(uuid)
			}
		)

	fun checkMute(playerId: UUID): CompletableFuture<Boolean> {
		val presentValue = muteCache.getIfPresent(playerId)
		if (presentValue != null) return CompletableFuture.completedFuture(presentValue)
		return CompletableFuture.supplyAsync { muteCache[playerId] }
	}

	fun checkBan(playerId: UUID): CompletableFuture<Boolean> {
		val presentValue = banCache.getIfPresent(playerId)
		if (presentValue != null) return CompletableFuture.completedFuture(presentValue)
		return CompletableFuture.supplyAsync { banCache[playerId] }
	}

	private fun playerIsMuted(playerId: UUID): Boolean = liteBansDatabase.isPlayerMuted(playerId, null)
	private fun playerIsBanned(playerId: UUID): Boolean = liteBansDatabase.isPlayerBanned(playerId, null)
}
