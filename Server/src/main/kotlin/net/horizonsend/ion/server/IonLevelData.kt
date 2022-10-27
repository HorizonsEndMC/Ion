package net.horizonsend.ion.server

import net.minecraft.server.level.ServerLevel

/** A data which Ion needs on a per-level basis. */
class IonLevelData private constructor(
	val serverLevel: ServerLevel
) {
	companion object {
		private val ionWorlds = mutableMapOf<ServerLevel, IonLevelData>()

		operator fun get(serverLevel: ServerLevel): IonLevelData = ionWorlds[serverLevel]!!

		fun register(serverLevel: ServerLevel) {
			if (ionWorlds.contains(serverLevel)) {
				throw IllegalStateException("Attempted to register server level which is already registered!")
			}

			ionWorlds[serverLevel] = IonLevelData(serverLevel)
		}

		fun unregister(serverLevel: ServerLevel) {
			ionWorlds.remove(serverLevel)
		}

		fun unregisterAll() {
			ionWorlds.clear()
		}
	}
}