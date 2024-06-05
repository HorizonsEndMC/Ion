package net.horizonsend.ion.server.features.custom.items.mods

object ToolModRegistry {
	val mods: MutableMap<String, ToolModification> = mutableMapOf()

	fun <T: ToolModification> registerMod(mod: T): T {
		mods[mod.identifier] = mod
		return mod
	}

	operator fun get(identifier: String): ToolModification = mods[identifier]!!
}
