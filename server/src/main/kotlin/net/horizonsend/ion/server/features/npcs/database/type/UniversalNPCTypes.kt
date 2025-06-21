package net.horizonsend.ion.server.features.npcs.database.type

object UniversalNPCTypes {
	private val typeMap: MutableMap<String, UniversalNPCType<*>> = mutableMapOf()

	val SERVER_SHIP_DEALER = register(ServerShipDealerType)
	val PLAYER_SHIP_DEALER = register(PlayerShipDealerType)

	fun <T : UniversalNPCType<*>> register(type: T): T {
		typeMap[type.identifier] = type
		return type
	}

	fun getByIdentifier(identifier: String): UniversalNPCType<*> {
		return typeMap[identifier] ?: error("NPC type $identifier not registered!")
	}

	fun all() = typeMap.toMap()
	fun allKeys() = typeMap.keys
}
