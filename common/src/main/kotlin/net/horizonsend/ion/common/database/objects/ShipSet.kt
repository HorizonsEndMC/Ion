package net.horizonsend.ion.common.database.objects

import net.horizonsend.ion.common.database.collections.PlayerData

data class ShipSet(
	var setName: String,
	var shipName: String?,
	var flyableblocks: MutableSet<String>,
	var type: String,
	var allowedPilots: MutableSet<PlayerData>
)
