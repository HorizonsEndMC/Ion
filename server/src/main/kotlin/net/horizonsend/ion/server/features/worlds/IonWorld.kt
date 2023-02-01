package net.horizonsend.ion.server.features.worlds

import net.minecraft.server.level.ServerLevel

/** A data which Ion needs on a per-world basis. */
class IonWorld private constructor(
	val serverLevel: ServerLevel
) {
	companion object {
		private val ionWorlds = mutableMapOf<ServerLevel, IonWorld>()

		operator fun get(serverLevel: ServerLevel): IonWorld = ionWorlds[serverLevel]!!

		fun register(serverLevel: ServerLevel) {
			if (ionWorlds.contains(serverLevel)) {
				throw IllegalStateException("Attempted to register server level which is already registered!")
			}

			ionWorlds[serverLevel] = IonWorld(serverLevel)
		}

		fun unregister(serverLevel: ServerLevel) {
			ionWorlds.remove(serverLevel)
		}

		fun unregisterAll() {
			ionWorlds.clear()
		}
	}
}
