package net.horizonsend.ion.server

import co.aikar.commands.PaperCommandManager
import net.horizonsend.ion.common.database.closeDatabase
import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.common.database.openDatabase
import net.horizonsend.ion.common.loadConfiguration
import net.starlegacy.database.schema.starships.PlayerStarshipData
import net.starlegacy.legacyDisable
import net.starlegacy.legacyEnable
import net.starlegacy.util.blockKeyX
import net.starlegacy.util.blockKeyY
import net.starlegacy.util.blockKeyZ
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld
import org.bukkit.plugin.java.JavaPlugin
import org.litote.kmongo.eq

@Suppress("Unused")
class IonServer : JavaPlugin() {
	init { Ion = this }

	companion object {
		lateinit var Ion: IonServer private set
	}

	val configuration = loadConfiguration<ServerConfiguration>(dataFolder, "server.conf")

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

		// Missing Ship Purge
		var shipsRemoved = 0
		var chunksRemaining = 0

		for (world in server.worlds) {
			for (starship in PlayerStarshipData.find(PlayerStarshipData::levelName eq world.name)) {
				val gX = blockKeyX(starship.blockKey)
				val gY = blockKeyY(starship.blockKey)
				val gZ = blockKeyZ(starship.blockKey)

				val location = Location(world, gX.toDouble(), gY.toDouble(), gZ.toDouble())

				world.getChunkAtAsync(location, false) { chunk ->
					val cX = gX.rem(16)
					val cZ = gZ.rem(16)

					try {
						if (chunk.getBlock(cX, gY, cZ).type != Material.JUKEBOX) {
							println("Removed missing ${starship.starshipType} at $gX, $gY, $gZ @ ${world.name}.")
							PlayerStarshipData.remove(starship._id)
							shipsRemoved++
						}
					} catch (e: Exception) {
						println("Removed corrupt ${starship.starshipType} at $gX, $gY, $gZ @ ${world.name}.")
						PlayerStarshipData.remove(starship._id)
						shipsRemoved++
					}

					chunk.isForceLoaded = false
					chunksRemaining--

					if (chunksRemaining == 0) {
						println("$shipsRemoved missing / corrupted ships were removed.")
					}
				}

				chunksRemaining++
			}
		}
	}

	override fun onDisable() {
		IonWorld.unregisterAll()
		closeDatabase()
		legacyDisable()
	}
}