package net.horizonsend.ion.server

import co.aikar.commands.PaperCommandManager
import net.horizonsend.ion.common.Connectivity
import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.common.loadConfiguration
import net.starlegacy.legacyDisable
import net.starlegacy.legacyEnable
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld
import org.bukkit.plugin.java.JavaPlugin

@Suppress("Unused")
class IonServer : JavaPlugin() {
	init {
		Ion = this
	}

	companion object {
		lateinit var Ion: IonServer private set
	}

	val configuration = loadConfiguration<ServerConfiguration>(dataFolder, "server.conf")

	override fun onEnable() {
		try {
			Connectivity.open(dataFolder)

			val pluginManager = server.pluginManager

			// Commands
			val commandManager = PaperCommandManager(this)

			@Suppress("Deprecation")
			commandManager.enableUnstableAPI("help")

			for (command in commands) commandManager.registerCommand(command)

			// TODO: This is messy, we should have individual classes which contain the completions.
			commandManager.commandCompletions.registerStaticCompletion("achievements", Achievement.values().map { it.name })

			// The listeners are defined in a separate file for the sake of keeping the main class clean.
			for (listener in listeners) pluginManager.registerEvents(listener, this)

			// Same deal as listeners.
			initializeCrafting()

			// Basically exists as a catch all for any weird state which could result in worlds already being loaded at this
			// such as reloading or other plugins doing things they probably shouldn't.
			for (world in server.worlds) IonWorld.register((world as CraftWorld).handle)

			legacyEnable(commandManager)
		} catch (exception: Exception) {
			slF4JLogger.error("An exception occurred during plugin startup! The server will now exit.", exception)
			Bukkit.shutdown()
		}
	}

	override fun onDisable() {
		IonWorld.unregisterAll()
		legacyDisable()
		Connectivity.close()
	}
}
