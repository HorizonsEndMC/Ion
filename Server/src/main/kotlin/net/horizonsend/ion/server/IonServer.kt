package net.horizonsend.ion.server

import co.aikar.commands.PaperCommandManager
import net.horizonsend.ion.common.database.closeDatabase
import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.common.database.openDatabase
import net.starlegacy.legacyDisable
import net.starlegacy.legacyEnable
import org.bukkit.plugin.java.JavaPlugin

@Suppress("Unused")
class IonServer : JavaPlugin() {
	init { Ion = this }

	companion object {
		lateinit var Ion: IonServer private set
	}

	override fun onEnable() {
		openDatabase(dataFolder)

		val pluginManager = server.pluginManager

		// Commands
		val commandManager = PaperCommandManager(this)

		val commands = arrayOf(
			AchievementsCommand()
		)

		for (command in commands) commandManager.registerCommand(command)

		// TODO: This is messy, we should have individual classes which contain the completions.
		commandManager.commandCompletions.registerStaticCompletion("achievements", Achievement.values().map { it.name })

		// The listeners are defined in a separate file for the sake of keeping the main class clean.
		for (listener in listeners) pluginManager.registerEvents(listener, this)

		// Same deal as listeners.
		initializeCrafting()

		legacyEnable(commandManager)
	}

	override fun onDisable() {
		closeDatabase()
		legacyDisable()
	}
}