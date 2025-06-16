package net.horizonsend.ion.server.features.npcs.database.type

object DatabaseNPCTypes {
	private val typeMap: MutableMap<String, DatabaseNPCType<*>> = mutableMapOf()

	val SERVER_SHIP_DEALER = register(ServerShipDealerType)
//	val PLAYER_SHIP_DEALER = register(PlayerShipDealerType)

	fun <T : DatabaseNPCType<*>> register(type: T): T {
		typeMap[type.identifier] = type
		return type
	}

	fun getByIdentifier(identifier: String): DatabaseNPCType<*> {
		return typeMap[identifier] ?: error("NPC type $identifier not registered!")
	}
}
