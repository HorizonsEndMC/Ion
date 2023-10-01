package net.horizonsend.ion.common.utils

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import litebans.api.Database
import java.util.UUID
import java.util.concurrent.TimeUnit

val liteBansDatabase = Database.get()

val muteCache: LoadingCache<UUID, Boolean> = CacheBuilder.newBuilder()
	.expireAfterAccess(5, TimeUnit.MINUTES)
	.build(
		CacheLoader.from { uuid ->
			playerIsMuted(uuid)
		}
	)

val banCache: LoadingCache<UUID, Boolean> = CacheBuilder.newBuilder()
	.expireAfterAccess(5, TimeUnit.MINUTES)
	.build(
		CacheLoader.from { uuid ->
			playerIsBanned(uuid)
		}
	)

fun playerIsMuted(playerId: UUID): Boolean = runCatching { liteBansDatabase.isPlayerMuted(playerId, null) }.getOrDefault(false)
fun playerIsBanned(playerId: UUID): Boolean = runCatching { liteBansDatabase.isPlayerBanned(playerId, null) }.getOrDefault(false)
