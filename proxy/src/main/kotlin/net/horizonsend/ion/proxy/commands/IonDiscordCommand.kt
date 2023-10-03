package net.horizonsend.ion.proxy.commands

import co.aikar.commands.InvalidCommandArgument
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.schema.nations.Nation

interface IonDiscordCommand {
	fun fail(message: () -> String): Nothing = throw InvalidCommandArgument(message())

	fun resolveNation(name: String): Oid<Nation> = NationCache.getByName(name)
		?: fail { "Nation $name not found" }
}
