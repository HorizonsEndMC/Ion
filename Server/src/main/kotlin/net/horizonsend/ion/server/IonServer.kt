package net.horizonsend.ion.server

import co.aikar.commands.PaperCommandManager
import net.horizonsend.ion.common.database.closeDatabase
import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.common.database.openDatabase
import net.starlegacy.legacyDisable
import net.starlegacy.legacyEnable
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld
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

		// Currently Ion needs to be loaded POSTWORLD due to older Star Legacy code, this means some worlds will already
		// be loaded by the time we get to plugin enable. In the future we will change to load on STARTUP, but for the
		// time being we need to check for worlds on start up. This additionally serves to allow Ion to handle reloads.
		for (world in server.worlds) IonWorld.register((world as CraftWorld).handle)

		legacyEnable(commandManager)
	}

	override fun onDisable() {
		IonWorld.unregisterAll()
		closeDatabase()
		legacyDisable()
	}
}