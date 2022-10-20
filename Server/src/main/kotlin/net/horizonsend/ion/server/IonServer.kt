package net.horizonsend.ion.server

import co.aikar.commands.PaperCommandManager
import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.common.database.initializeDatabase
import net.starlegacy.legacyDisable
import net.starlegacy.legacyEnable
import org.bukkit.plugin.java.JavaPlugin

@Suppress("Unused")
class IonServer : JavaPlugin() {
	init { plugin = this }

	companion object {
		@JvmStatic lateinit var plugin: IonServer private set
	}

	override fun onEnable() {
		initializeDatabase(dataFolder) // Common Database

		val pluginManager = server.pluginManager
		val commandManager = PaperCommandManager(this)

		for (listener in listeners) pluginManager.registerEvents(listener, this) // Listeners

		commandManager.registerCommand(AchievementsCommand())
		commandManager.commandCompletions.registerStaticCompletion("achievements", Achievement.values().map { it.name })

		initializeCrafting()

		legacyEnable(commandManager)
	}

	override fun onDisable() = legacyDisable()
}