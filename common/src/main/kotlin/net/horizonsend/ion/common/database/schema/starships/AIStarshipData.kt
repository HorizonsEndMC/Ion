package net.horizonsend.ion.common.database.schema.starships

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.StarshipTypeDB

data class AIStarshipData(
	override val _id: Oid<AIStarshipData>,

	override var starshipType: StarshipTypeDB,
	override var serverName: String?,
	override var levelName: String,
	override var blockKey: Long,
	override var name: String? = null,
	override var containedChunks: Set<Long>? = null,
	override var lastUsed: Long = System.currentTimeMillis(),
	override var isLockEnabled: Boolean = false,
	) : StarshipData {
	companion object : StarshipDataCompanion<AIStarshipData>(
		AIStarshipData::class,
		AIStarshipData::serverName,
		AIStarshipData::levelName,
		AIStarshipData::blockKey,
		AIStarshipData::containedChunks,
		AIStarshipData::starshipType,
		AIStarshipData::isLockEnabled,
		AIStarshipData::name
	)

	@Suppress("UNCHECKED_CAST")
	override fun companion(): StarshipDataCompanion<StarshipData> = Companion as StarshipDataCompanion<StarshipData>
}
